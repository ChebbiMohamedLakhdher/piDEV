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

            // Get selected role in database format
            String roleDisplayName = roleComboBox.getValue();
            String roleValue = convertRoleToDatabaseFormat(roleDisplayName);

            // Insert new user with role and plain text password
            String insertUserQuery = "INSERT INTO user (name, email, password, phonenumber, address, roles) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStatement = conn.prepareStatement(insertUserQuery);
            insertStatement.setString(1, nameField.getText());
            insertStatement.setString(2, emailField.getText());
            insertStatement.setString(3, passwordField.getText()); // Storing plain text password
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

    private String convertRoleToDatabaseFormat(String displayName) {
        switch(displayName) {
            case "Client":
                return "[\"ROLE_CLIENT\"]";
            case "Lawyer":
                return "[\"ROLE_LAWYER\"]";
            default:
                return "[\"ROLE_CLIENT\"]";
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
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleLoginLink() {
        loadLoginScene();
    }

    private boolean validateInputs() {
        errorLabel.setText("");

        if (nameField.getText().trim().isEmpty()) {
            errorLabel.setText("Name cannot be empty");
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.compile(emailRegex).matcher(emailField.getText()).matches()) {
            errorLabel.setText("Invalid email format");
            return false;
        }

        if (passwordField.getText().length() < 8) {
            errorLabel.setText("Password must be at least 8 characters");
            return false;
        }

        if (phoneField.getText().length() < 8) {
            errorLabel.setText("Phone number must be at least 8 digits");
            return false;
        }

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