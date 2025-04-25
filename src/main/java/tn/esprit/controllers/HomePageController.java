package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.models.Case;
import tn.esprit.services.CaseService;
import javafx.scene.layout.VBox;  // Add this at the top
import java.time.format.DateTimeFormatter;  // Add this for date formatting
import java.io.IOException;

public class HomePageController {
    @FXML private AnchorPane contentPane;
    @FXML
    private ListView<Case> listView;
    private final CaseService caseService = new CaseService();

    @FXML
    private void handlePostCase() {
        loadPage("/Post_Case.fxml");
    }

    @FXML
    private void handleDiscover() {
        loadPage("/HomePage.fxml");  // Replace with your actual home page FXML
    }

    @FXML
    private void handleCases() {
        loadPage("/Cases.fxml");
    }



    @FXML
    private void handleSettings() {
        loadPage("/AdminDashboard.fxml");
    }


    private void loadPage(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load page: " + fxmlPath);
        }
    }

    private void showErrorAlert(String navigationError, String s) {

    }


}
