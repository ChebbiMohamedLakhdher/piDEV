package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.Stage;
import tn.esprit.utils.MyDataBase;

public class ResetPasswordController {
    private static final Logger logger = Logger.getLogger(ResetPasswordController.class.getName());

    // FXML components
    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private Button requestTokenButton;
    @FXML private Button resetPasswordButton;
    @FXML private Label messageLabel;

    // Email Configuration (using your ESPRIT email as sender)
    private static final String SMTP_HOST = "smtp.office365.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USERNAME = "MohamedLakhdher.Chebbi@esprit.tn";
    private static final String SMTP_PASSWORD = "Fuckoffsobad2031";
    private static final String EMAIL_FROM = "MohamedLakhdher.Chebbi@esprit.tn";
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

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            if (emailExists(conn, email)) {
                String token = UUID.randomUUID().toString();
                LocalDateTime expiry = LocalDateTime.now().plusHours(24);

                updateResetToken(conn, email, token, expiry);
                sendResetEmail(email, token);
            }

            showSuccess("If this email exists in our system, reset instructions have been sent");
            enablePasswordResetFields();

        } catch (SQLException e) {
            showError("Database error. Please try again later.");
            logger.log(Level.SEVERE, "Database error in handleRequestToken", e);
        } catch (MessagingException e) {
            showError("Failed to send email. Please contact support.");
            logger.log(Level.SEVERE, "Email error in handleRequestToken");
        } catch (Exception e) {
            showError("System error. Please try again later.");
            logger.log(Level.SEVERE, "Unexpected error in handleRequestToken", e);
        }
    }

    @FXML
    private void handleResetPassword(ActionEvent event) {
        String email = emailField.getText().trim();
        String token = tokenField.getText().trim();
        String newPassword = newPasswordField.getText().trim();

        if (!validateResetInputs(email, token, newPassword)) {
            return;
        }

        try (Connection conn = MyDataBase.getInstance().getCnx()) {
            if (!isValidToken(conn, email, token)) {
                showError("Invalid or expired token");
                return;
            }

            updatePassword(conn, email, newPassword); // Storing plain text password
            showSuccess("Password reset successfully!");
            disablePasswordResetFields();

        } catch (SQLException e) {
            showError("Database error. Please try again later.");
            logger.log(Level.SEVERE, "Database error in handleResetPassword", e);
        }
    }

    private javax.mail.Session createEmailSession() {
        Properties props = new Properties();

        // Office 365 SMTP configuration
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Timeout settings
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        // Additional Office 365 requirements
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        return javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
    }

    private void sendResetEmail(String recipientEmail, String token) throws MessagingException {
        javax.mail.Session session = createEmailSession();

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(EMAIL_SUBJECT);

            String resetLink = "https://your-app.com/reset?email=" +
                    URLEncoder.encode(recipientEmail, "UTF-8") +
                    "&token=" + URLEncoder.encode(token, "UTF-8");

            // HTML content
            String htmlContent = "<html><body style='font-family: Arial, sans-serif;'>"
                    + "<h2 style='color: #0078D4;'>Password Reset Request</h2>"
                    + "<p>Dear User,</p>"
                    + "<p>You requested a password reset for your account.</p>"
                    + "<p><a href='" + resetLink + "' style='background-color: #0078D4; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px;'>Reset Password</a></p>"
                    + "<p>Or use this verification code: <strong>" + token + "</strong></p>"
                    + "<p>This link will expire in 24 hours.</p>"
                    + "<p>If you didn't request this, please ignore this email.</p>"
                    + "<p>Best regards,<br/>Support Team</p>"
                    + "</body></html>";

            // Plain text alternative
            String textContent = "Password Reset Request\n\n"
                    + "Click here to reset: " + resetLink + "\n"
                    + "Or use this verification code: " + token + "\n"
                    + "This link expires in 24 hours.\n\n"
                    + "If you didn't request this, please ignore this email.";

            // Set both versions
            MimeMultipart multipart = new MimeMultipart("alternative");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(textContent, "utf-8");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
            Transport.send(message);

        } catch (UnsupportedEncodingException e) {
            logger.log(Level.SEVERE, "Encoding error when creating reset link", e);
            throw new MessagingException("Failed to create reset email");
        }
    }

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

    private void updatePassword(Connection conn, String email, String password) throws SQLException {
        String sql = "UPDATE user SET password = ?, reset_token = NULL, token_expiry = NULL WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, password); // Storing plain text password
            stmt.setString(2, email);
            stmt.executeUpdate();
        }
    }

    @FXML
    private void handleBackToLogin(ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load login page");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
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

        if (!password.matches(".*[A-Z].*")) {
            showError("Password must contain at least one uppercase letter");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            showError("Password must contain at least one lowercase letter");
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            showError("Password must contain at least one number");
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