package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import tn.esprit.models.Case;
import tn.esprit.services.CaseService;

import java.time.format.DateTimeFormatter;

public class AdminCasesController {
    @FXML private ListView<Case> casesListView;
    private final CaseService caseService = new CaseService();

    @FXML
    public void initialize() {
        casesListView.setItems(caseService.getCasesObservable());
        casesListView.setCellFactory(param -> new AdminCaseCell());
    }

    private class AdminCaseCell extends ListCell<Case> {
        private final HBox container = new HBox(15);
        private final Label titleLabel = new Label();
        private final Label locationLabel = new Label();
        private final Label dateLabel = new Label();
        private final Button statusBtn = new Button();
        private final Button visibilityBtn = new Button();
        private final Button deleteBtn = new Button("Delete");

        public AdminCaseCell() {
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().addAll(titleLabel, locationLabel, dateLabel, statusBtn, visibilityBtn, deleteBtn);

            // Set styles
            titleLabel.getStyleClass().add("case-title");
            locationLabel.getStyleClass().add("case-detail");
            dateLabel.getStyleClass().add("case-detail");

            statusBtn.getStyleClass().add("status-btn");
            visibilityBtn.getStyleClass().add("visibility-btn");
            deleteBtn.getStyleClass().add("delete-btn");

            // Fixed widths
            titleLabel.setPrefWidth(200);
            locationLabel.setPrefWidth(150);
            dateLabel.setPrefWidth(100);
            statusBtn.setPrefWidth(100);
            visibilityBtn.setPrefWidth(100);

            // Button actions
            statusBtn.setOnAction(e -> updateStatus());
            visibilityBtn.setOnAction(e -> updateVisibility());
            deleteBtn.setOnAction(e -> deleteCase());
        }

        @Override
        protected void updateItem(Case caseObj, boolean empty) {
            super.updateItem(caseObj, empty);
            if (empty || caseObj == null) {
                setGraphic(null);
            } else {
                titleLabel.setText(caseObj.getTitle());
                locationLabel.setText(caseObj.getLocation());
                dateLabel.setText(caseObj.getCreatedAt().format(DateTimeFormatter.ISO_DATE));
                statusBtn.setText(caseObj.getStatus());
                visibilityBtn.setText(caseObj.getVisibility());
                setGraphic(container);
            }
        }


        private void updateStatus() {
            Case c = getItem();
            String newStatus = switch (c.getStatus()) {
                case "open" -> "pending";
                case "pending" -> "closed";
                case "closed" -> "open";
                default -> "open";
            };
            caseService.updateStatus(c.getId(), newStatus);
            c.setStatus(newStatus);
            statusBtn.setText(newStatus);
        }

        private void updateVisibility() {
            Case c = getItem();
            String newVis = c.getVisibility().equals("public") ? "unlisted" : "public";
            caseService.updateVisibility(c.getId(), newVis);
            c.setVisibility(newVis);
            visibilityBtn.setText(newVis);
        }

        private void deleteCase() {
            Case c = getItem();
            caseService.deleteCase(c.getId());
            casesListView.getItems().remove(c);
        }
    }
}