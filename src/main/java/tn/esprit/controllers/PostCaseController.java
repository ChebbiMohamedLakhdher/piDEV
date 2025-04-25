package tn.esprit.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Case;
import tn.esprit.services.CaseService;
import javafx.fxml.FXML;

import java.io.IOException;
import java.util.List;

public class PostCaseController {
    @FXML private TextField titleField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> typeDropdown;
    @FXML private ComboBox<String> visibilityDropdown;
    @FXML private TextArea descriptionArea;
    @FXML private TextField telephoneField;

    private CaseService caseService = new CaseService();

    @FXML
    public void initialize() {
        // Populate dropdowns
        typeDropdown.getItems().addAll("CIVIL", "CRIMINAL", "FAMILY", "IMMIGRATION");
        visibilityDropdown.getItems().addAll("PUBLIC", "UNLISTED");
    }

    @FXML
    private void handleSubmit() {
        // Hardcoded user ID for testing (replace later)
        int userId = 1;

        Case newCase = new Case(
                userId,
                titleField.getText().trim(),
                locationField.getText().trim(),
                typeDropdown.getValue(),
                visibilityDropdown.getValue(),
                descriptionArea.getText().trim(),
                telephoneField.getText().trim()
        );

        List<String> errors = caseService.saveCase(newCase);

        if(errors.isEmpty()) {

            navigateToCasesPage();

        } else {
            showErrorAlert(errors);
        }
    }

    private void navigateToCasesPage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Cases.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("All Cases");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String navigationError, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Errors");
        alert.setHeaderText("Please fix the following issues:");
        alert.setContentText("idk");
        alert.showAndWait();
    }

    private void showErrorAlert(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Errors");
        alert.setHeaderText("Please fix the following issues:");
        alert.setContentText(String.join("\n• ", errors));
        alert.showAndWait();
    }


    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Cases.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}