package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.utils.MyDataBase;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class LoginController implements Initializable {
    @FXML private Label loginMessageLabel;
    @FXML private ImageView brandingImageView;
    @FXML private TextField emailTextField;
    @FXML private PasswordField enterPasswordField;
    @FXML private Button signupButton;

    // Password hashing constants
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            File brandingFile = new File("images/LegalLink-NoBg.png");
            Image brandingImage = new Image(brandingFile.toURI().toString());
            brandingImageView.setImage(brandingImage);
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }

        // Debug: Check if dashboard file exists
        debugFileLocation("HomePage.fxml");
    }

    private void debugFileLocation(String filename) {
        try {
            System.out.println("\n=== FILE LOCATION DEBUG ===");
            URL url = getClass().getResource(filename);
            if (url == null) {
                System.err.println("ERROR: " + filename + " not found in:");
                System.err.println("Class location: " + getClass().getResource("."));

                // Try alternative paths
                System.out.println("Trying absolute path...");
                url = getClass().getResource("/tn/esprit/views/" + filename);
                if (url != null) {
                    System.out.println("Found at: " + url);
                } else {
                    System.err.println("Still not found at absolute path");
                }
            } else {
                System.out.println(filename + " found at: " + url);
            }
            System.out.println("=========================\n");
        } catch (Exception e) {
            System.err.println("Debug error: " + e.getMessage());
        }
    }

    @FXML
    private void loginButtonOnAction(javafx.event.ActionEvent event) {
        loginMessageLabel.setText(""); // Clear previous messages
        if (emailTextField.getText().isBlank() || enterPasswordField.getText().isBlank()) {
            loginMessageLabel.setText("Please enter email and password");
            return;
        }

        validateLogin();
    }

    private void validateLogin() {
        String verifyLogin = "SELECT password, roles FROM user WHERE email = ?";

        try (Connection connectDB = MyDataBase.getInstance().getCnx();
             PreparedStatement statement = connectDB.prepareStatement(verifyLogin)) {

            statement.setString(1, emailTextField.getText().trim());

            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    String storedHash = queryResult.getString("password");
                    String roles = queryResult.getString("roles");

                    if (storedHash != null && verifyPassword(enterPasswordField.getText(), storedHash)) {
                        if (roles != null && roles.contains("ROLE_ADMIN")) {
                            loadDashboard("/AdminDashboard.fxml", "Admin Page");
                        } else {
                            loginMessageLabel.setText("Login Successful");
                            loadDashboard("/HomePage.fxml", "Home Page");
                        }
                    } else {
                        loginMessageLabel.setText("Invalid credentials");
                    }
                } else {
                    loginMessageLabel.setText("User not found");
                }
            }
        } catch (SQLException e) {
            loginMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            loginMessageLabel.setText("System error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Password verification method
    private boolean verifyPassword(String password, String storedHash) {
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

    private void loadDashboard(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage currentStage = (Stage) emailTextField.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle(title);
            currentStage.show();
        } catch (Exception e) {
            String errorMsg = "Failed to load dashboard: " + e.getMessage();
            loginMessageLabel.setText(errorMsg);
            System.err.println(errorMsg);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignupButton(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/signup.fxml"));
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Create New Account");
            stage.show();
        } catch (Exception e) {
            loginMessageLabel.setText("Error loading signup form: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForgotPassword(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/reset_password.fxml"));
            Stage stage = (Stage) emailTextField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Reset Password");
            stage.show();
        } catch (Exception e) {
            loginMessageLabel.setText("Error loading reset password form");
            e.printStackTrace();
        }
    }
}