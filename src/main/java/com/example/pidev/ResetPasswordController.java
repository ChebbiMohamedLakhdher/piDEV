package com.example.pidev;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

public class ResetPasswordController {
    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private Button requestTokenButton;
    @FXML private Button resetPasswordButton;
    @FXML private Label messageLabel;

    // Email configuration - replace with your SMTP details
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "your-email@gmail.com";
    private static final String SMTP_PASSWORD = "your-app-password"; // Use app-specific password
    private static final String EMAIL_FROM = "your-email@gmail.com";
    private static final String EMAIL_SUBJECT = "Password Reset Request";

    @FXML
    private void initialize() {
        tokenField.setDisable(true);
        newPasswordField.setDisable(true);
        resetPasswordButton.setDisable(true);
    }

    @FXML
    private void handleRequestToken(ActionEvent event) {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if email exists
            if (!emailExists(conn, email)) {
                showError("If this email exists, a reset link has been sent");
                return; // Don't reveal if email exists
            }

            // Generate token and expiry (24 hours from now)
            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusHours(24);

            // Update user record with token
            updateResetToken(conn, email, token, expiry);

            // Send email with reset link
            sendResetEmail(email, token);

            showSuccess("Reset instructions sent to your email");
            enablePasswordResetFields();

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (MessagingException e) {
            showError("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText().trim();
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (!validateResetInputs(email, token, newPassword)) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Verify token is valid and not expired
            if (!isValidToken(conn, email, token)) {
                showError("Invalid or expired token");
                return;
            }

            // Hash the new password
            String hashedPassword = hashPassword(newPassword);

            // Update password and clear token
            updatePassword(conn, email, hashedPassword);

            showSuccess("Password reset successfully!");
            disablePasswordResetFields();

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============== Helper Methods ==============

    private boolean emailExists(Connection conn, String email) throws SQLException {
        String sql = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void updateResetToken(Connection conn, String email, String token, LocalDateTime expiry) throws SQLException {
        String sql = "UPDATE user SET reset_token = ?, token_expiry = ? WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, token);
            stmt.setTimestamp(2, Timestamp.valueOf(expiry));
            stmt.setString(3, email);
            stmt.executeUpdate();
        }
    }

    private void sendResetEmail(String recipientEmail, String token) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(EMAIL_SUBJECT);

        String resetLink = "http://your-app.com/reset?email=" + recipientEmail + "&token=" + token;
        String emailBody = "You requested a password reset. Please use the following token:\n\n" +
                "Token: " + token + "\n\n" +
                "Or click this link: " + resetLink + "\n\n" +
                "This token will expire in 24 hours.\n\n" +
                "If you didn't request this, please ignore this email.";

        message.setText(emailBody);

        Transport.send(message);
    }

    private boolean isValidToken(Connection conn, String email, String token) throws SQLException {
        String sql = "SELECT id FROM user WHERE email = ? AND reset_token = ? AND token_expiry > NOW()";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, token);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void updatePassword(Connection conn, String email, String hashedPassword) throws SQLException {
        String sql = "UPDATE user SET password = ?, reset_token = NULL, token_expiry = NULL WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean validateResetInputs(String email, String token, String password) {
        if (email.isEmpty() || token.isEmpty() || password.isEmpty()) {
            showError("All fields are required");
            return false;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters");
            return false;
        }

        return true;
    }

    private void enablePasswordResetFields() {
        tokenField.setDisable(false);
        newPasswordField.setDisable(false);
        resetPasswordButton.setDisable(false);
    }

    private void disablePasswordResetFields() {
        tokenField.setDisable(true);
        newPasswordField.setDisable(true);
        resetPasswordButton.setDisable(true);
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }
}