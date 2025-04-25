package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.utils.MyDataBase;

import java.io.File;
import java.sql.*;

public class ManageUserController {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
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
        setupTableColumns();
        setupFilterComboBox();
        loadUsersFromDatabase();
        setupSearchFunctionality();
    }



    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Users", "CLIENT", "ADMIN"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.setOnAction(e -> filterUsers());
    }

    private void loadUsersFromDatabase() {
        allUsers.clear();
        String query = "SELECT id, name, email, phonenumber, roles FROM `user`"; // Added backticks around table name

        try (Connection conn = MyDataBase.getInstance().getCnx(); // Use singleton instance
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String rolesJson = rs.getString("roles");
                String roleDisplay = "CLIENT";

                // Proper JSON handling
                if (rolesJson != null && rolesJson.contains("ROLE_ADMIN")) {
                    roleDisplay = "ADMIN";
                }

                allUsers.add(new User(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phonenumber"),
                        roleDisplay
                ));
            }

            usersTable.setItems(allUsers);
            statusLabel.setText("Loaded " + allUsers.size() + " users");

        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load users",
                    "Error details: " + e.getErrorCode() + " - " + e.getMessage());
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

        usersTable.setItems(filteredUsers);
        statusLabel.setText("Showing " + filteredUsers.size() + " of " + allUsers.size() + " users");
    }

    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
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

            Connection conn = MyDataBase.getInstance().getCnx(); // Get singleton connection
            try (PreparedStatement pstmt = conn.prepareStatement(query)) { // Only close the statement
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
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "No User Selected", "Please select a user to change role.");
            return;
        }

        String currentRole = selectedUser.getRole();
        String newDisplayRole = currentRole.equals("ADMIN") ? "CLIENT" : "ADMIN";

        // Create the proper JSON array for the database
        String newDbRole = newDisplayRole.equals("ADMIN")
                ? "[\"ROLE_ADMIN\"]"
                : "[\"ROLE_CLIENT\"]";

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Role Change");
        confirmation.setHeaderText("Change User Role");
        confirmation.setContentText("Change " + selectedUser.getName() + "'s role from " +
                currentRole + " to " + newDisplayRole + "?");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            String query = "UPDATE user SET roles = ? WHERE id = ?";

            Connection conn = MyDataBase.getInstance().getCnx(); // Get singleton connection
            try (PreparedStatement pstmt = conn.prepareStatement(query)) { // Only close the statement
                pstmt.setString(1, newDbRole);
                pstmt.setInt(2, selectedUser.getId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    // Update the UI
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
                showAlert("Database Error", "Failed to update role", "Error: " + e.getMessage());
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