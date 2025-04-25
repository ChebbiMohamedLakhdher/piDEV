package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import tn.esprit.models.Case;
import tn.esprit.services.CaseService;
import javafx.scene.layout.VBox;  // Add this at the top
import java.time.format.DateTimeFormatter;  // Add this for date formatting
import java.io.IOException;

public class CasesController {
    @FXML private ListView<Case> listView;
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

    @FXML
    public void initialize() {
        setupListView();
        loadCases();
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


    private void setupListView() {
        listView.setCellFactory(param -> new ListCell<Case>() {
            @Override
            protected void updateItem(Case caseObj, boolean empty) {
                super.updateItem(caseObj, empty);
                if (empty || caseObj == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CaseCell.fxml"));
                        VBox cellRoot = loader.load(); // Changed to VBox

                        // Access labels through controller
                        Label titleLabel = (Label) cellRoot.lookup("#titleLabel");
                        Label typeLabel = (Label) cellRoot.lookup("#typeLabel");
                        Label locationLabel = (Label) cellRoot.lookup("#locationLabel");
                        Label dateLabel = (Label) cellRoot.lookup("#dateLabel");

                        titleLabel.setText(caseObj.getTitle());
                        typeLabel.setText(caseObj.getType());
                        locationLabel.setText(caseObj.getLocation());
                        dateLabel.setText(caseObj.getCreatedAt()
                                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

                        setGraphic(cellRoot);
                    } catch (IOException e) {
                        setText("Error loading cell");
                    }
                }
            }
        });
        listView.setOnMouseClicked(this::handleCaseClick);
    }

    private void loadCases() {
        listView.getItems().setAll(caseService.getCases());
    }

    private void handleCaseClick(MouseEvent event) {
        Case selected = listView.getSelectionModel().getSelectedItem();
        if (selected != null && event.getClickCount() == 1) {
            openCaseDetails(selected);
        }
    }

    private void openCaseDetails(Case selectedCase) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CaseDetails.fxml"));
            Parent root = loader.load();

            CaseDetailsController controller = loader.getController();
            controller.initData(selectedCase);

            Stage stage = (Stage) listView.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            System.err.println("Error loading case details: " + e.getMessage());
        }
    }




}