package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.Case;
import javafx.fxml.FXMLLoader;

public class CaseDetailsController {
    @FXML private Label titleLabel;
    @FXML private Label typeLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label phoneLabel;

    public void initData(Case caseObj) {
        titleLabel.setText(caseObj.getTitle() + "    ");
        typeLabel.setText(caseObj.getType() + "    ");
        locationLabel.setText(caseObj.getLocation()+ "    ");
        dateLabel.setText((caseObj.getCreatedAt().toLocalDate())+ "    ");
        descriptionLabel.setText(caseObj.getDescription());
        phoneLabel.setText("  " + caseObj.getTelephone());
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Cases.fxml"));
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}