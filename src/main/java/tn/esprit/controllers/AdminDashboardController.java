package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
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

public class AdminDashboardController {
    @FXML private ListView<Case> listView;
    @FXML private AnchorPane contentPane;
    @FXML private BarChart<String, Number> typeChart;
    private final CaseService caseService = new CaseService();
    private boolean chartInitialized = false;

    @FXML
    public void initialize() {
        if (!chartInitialized) {
            initializeChart();
            chartInitialized = true;
        }
    }


    @FXML
    private void handleDiscover() {
        loadPage("/HomePage.fxml");  // Replace with your actual home page FXML
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



    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void initializeChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        caseService.getCaseTypeCounts().forEach((type, count) ->
                series.getData().add(new XYChart.Data<>(type, count))
        );
        typeChart.getData().add(series);
    }

    @FXML
    private void showOverview() {
        contentPane.getChildren().clear();
        if (!chartInitialized) {
            contentPane.getChildren().add(typeChart);
            chartInitialized = true;
        }
    }

    @FXML
    private void showAllCases() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminCases.fxml"));
            Parent casesView = loader.load();
            contentPane.getChildren().setAll(casesView);
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load cases view");
        }
    }


    @FXML
    private void ManageUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminManageUser.fxml"));
            Parent casesView = loader.load();
            contentPane.getChildren().setAll(casesView);
        } catch (IOException e) {
            showErrorAlert("Navigation Error", "Failed to load cases view");
        }
    }



}
