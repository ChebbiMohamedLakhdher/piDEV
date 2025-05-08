package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class SignUpController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private Label errorLabel;
    @FXML private Button signupButton;
    @FXML private Button clearButton;
    @FXML private Hyperlink loginLink;
    @FXML private ComboBox<String> roleComboBox;

    // Password hashing constants
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    @FXML
    private void initialize() {
        // Phone number input validation (digits only)
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                phoneField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Setup role ComboBox with user-friendly display names
        ObservableList<String> roles = FXCollections.observableArrayList(
                "Client",
                "Lawyer"
        );
        roleComboBox.setItems(roles);
        roleComboBox.getSelectionModel().selectFirst(); // Default to Client
    }

    @FXML
    private void handleSignup() {
        if (!validateInputs()) return;

        try {
            Connection conn = MyDataBase.getInstance().getCnx();

            // Check if email exists
            String checkUserQuery = "SELECT count(1) FROM user WHERE email = ?";
            PreparedStatement checkStatement = conn.prepareStatement(checkUserQuery);
            checkStatement.setString(1, emailField.getText());
            ResultSet queryResult = checkStatement.executeQuery();

            if (queryResult.next() && queryResult.getInt(1) == 1) {
                errorLabel.setText("Email already registered!");
                return;
            }

            // Hash the password before storing it
            String hashedPassword = hashPassword(passwordField.getText());

            // Get selected role in database format
            String roleDisplayName = roleComboBox.getValue();
            String roleValue = convertRoleToDatabaseFormat(roleDisplayName);

            // Insert new user with role and hashed password
            String insertUserQuery = "INSERT INTO user (name, email, password, phonenumber, address, roles) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = conn.prepareStatement(insertUserQuery);
            insertStatement.setString(1, nameField.getText());
            insertStatement.setString(2, emailField.getText());
            insertStatement.setString(3, hashedPassword);
            insertStatement.setString(4, phoneField.getText());
            insertStatement.setString(5, addressField.getText());
            insertStatement.setString(6, roleValue);

            int rowsAffected = insertStatement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(AlertType.INFORMATION, "Registration Successful",
                        "Account created successfully!\nRole: " + roleDisplayName + "\nYou can now login.");
                loadLoginScene();
            }

        } catch (Exception e) {
            errorLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Password hashing method
    String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(salt) + ":" +
                    Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Password verification method (for login)
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            String[] parts = storedHash.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedPassword = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATIONS,
                    KEY_LENGTH
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] testHash = factory.generateSecret(spec).getEncoded();

            return slowEquals(storedPassword, testHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error verifying password", e);
        }
    }

    // Constant-time comparison to prevent timing attacks
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    private String convertRoleToDatabaseFormat(String displayName) {
        switch(displayName) {
            case "Client":
                return "[\"ROLE_CLIENT\"]";
            case "Lawyer":
                return "[\"ROLE_LAWYER\"]"; // Assuming lawyers should have admin privileges
            default:
                return "[\"ROLE_CLIENT\"]"; // Default fallback
        }
    }

    @FXML
    private void handleClear() {
        nameField.clear();
        emailField.clear();
        passwordField.clear();
        phoneField.clear();
        addressField.clear();
        errorLabel.setText("");
        roleComboBox.getSelectionModel().selectFirst(); // Reset to Client
    }

    @FXML
    private void handleLoginLink() {
        loadLoginScene();
    }

    private boolean validateInputs() {
        errorLabel.setText("");

        // Name validation
        if (nameField.getText().trim().isEmpty()) {
            errorLabel.setText("Name cannot be empty");
            return false;
        }

        // Email validation
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.compile(emailRegex).matcher(emailField.getText()).matches()) {
            errorLabel.setText("Invalid email format");
            return false;
        }

        // Password validation
        if (passwordField.getText().length() < 8) {
            errorLabel.setText("Password must be at least 8 characters");
            return false;
        }

        // Phone validation
        if (phoneField.getText().length() < 8) {
            errorLabel.setText("Phone number must be at least 8 digits");
            return false;
        }

        // Address validation
        if (addressField.getText().trim().isEmpty()) {
            errorLabel.setText("Address cannot be empty");
            return false;
        }

        return true;
    }

    private void loadLoginScene() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            errorLabel.setText("Error loading login page");
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleLoginLink(javafx.event.ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading signup form");
        }
    }
}