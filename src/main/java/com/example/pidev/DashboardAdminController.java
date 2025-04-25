package com.example.pidev;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.io.File;
import java.sql.*;

public class DashboardAdminController {
    @FXML private ListView<User> usersListView;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label statusLabel;
    @FXML private Label userInfoLabel;
    @FXML private ImageView brandingImageView;

    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        File brandingFile = new File("images/Logo.png");
        Image brandingImage = new Image(brandingFile.toURI().toString());
        brandingImageView.setImage(brandingImage);
        setupListViewCellFactory();
        setupFilterComboBox();
        loadUsersFromDatabase();
        setupSearchFunctionality();
    }

    private void setupListViewCellFactory() {
        usersListView.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
            @Override
            public ListCell<User> call(ListView<User> param) {
                return new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(String.format("%s (%s) - %s - %s",
                                    user.getName(),
                                    user.getEmail(),
                                    user.getPhoneNumber(),
                                    user.getRole()));
                        }
                    }
                };
            }
        });
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Users", "CLIENT", "LAWYER"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.setOnAction(e -> filterUsers());
    }

    private void loadUsersFromDatabase() {
        allUsers.clear();
        String query = "SELECT id, name, email, phonenumber, roles FROM user";

        try (Connection conn = new DatabaseConnection().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String rolesJson = rs.getString("roles");
                // Convert JSON array to display role
                String roleDisplay = rolesJson.contains("ROLE_ADMIN") ? "LAWYER" : "CLIENT";

                allUsers.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phonenumber"),
                        roleDisplay
                ));
            }

            usersListView.setItems(allUsers);
            statusLabel.setText("Loaded " + allUsers.size() + " users");

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load users", e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupSearchFunctionality() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());
    }

    @FXML
    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase();
        String selectedRole = filterComboBox.getValue();

        ObservableList<User> filteredUsers = allUsers.filtered(user -> {
            boolean matchesSearch = user.getName().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText);

            boolean matchesRole = selectedRole.equals("All Users") ||
                    user.getRole().equals(selectedRole);

            return matchesSearch && matchesRole;
        });

        usersListView.setItems(filteredUsers);
        statusLabel.setText("Showing " + filteredUsers.size() + " of " + allUsers.size() + " users");
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "No User Selected", "Please select a user to delete.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete User");
        confirmation.setContentText("Are you sure you want to delete " + selectedUser.getName() + "?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String query = "DELETE FROM user WHERE id = ?";

            try (Connection conn = new DatabaseConnection().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setInt(1, selectedUser.getId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    allUsers.remove(selectedUser);
                    statusLabel.setText("Successfully deleted user: " + selectedUser.getName());
                } else {
                    showAlert("Deletion Failed", "User Not Deleted", "The user could not be deleted.");
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete user", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleChangeRole() {
        User selectedUser = usersListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "No User Selected", "Please select a user to change role.");
            return;
        }

        String currentRole = selectedUser.getRole();
        String newDisplayRole = currentRole.equals("LAWYER") ? "CLIENT" : "LAWYER";

        // Create the proper JSON array for the database
        String newDbRole = newDisplayRole.equals("LAWYER")
                ? "[\"ROLE_ADMIN\"]"
                : "[\"ROLE_CLIENT\"]";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Role Change");
        confirmation.setHeaderText("Change User Role");
        confirmation.setContentText("Change " + selectedUser.getName() + "'s role from " +
                currentRole + " to " + newDisplayRole + "?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String query = "UPDATE user SET roles = ? WHERE id = ?";

            try (Connection conn = new DatabaseConnection().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, newDbRole);
                pstmt.setInt(2, selectedUser.getId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    // Update the UI with the display role
                    User updatedUser = new User(
                            selectedUser.getId(),
                            selectedUser.getName(),
                            selectedUser.getEmail(),
                            selectedUser.getPhoneNumber(),
                            newDisplayRole
                    );

                    int index = allUsers.indexOf(selectedUser);
                    allUsers.set(index, updatedUser);
                    statusLabel.setText("Successfully changed role for " + selectedUser.getName());
                } else {
                    showAlert("Update Failed", "Role Not Changed", "The user role could not be updated.");
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to update role",
                        "Error: " + e.getMessage() +
                                "\n\nNote: Roles must be in format [\"ROLE_ADMIN\"] or [\"ROLE_CLIENT\"]");
                e.printStackTrace();
            }
        }
    }

    public static class User {
        private final int id;
        private final String name;
        private final String email;
        private final String phoneNumber;
        private final String role;

        public User(int id, String name, String email, String phoneNumber, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.role = role;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getRole() { return role; }
    }
}