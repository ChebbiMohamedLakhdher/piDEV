package tn.esprit.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.models.Case;
import tn.esprit.utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaseService {
    private Connection cnx;

    public CaseService() {
        // Initialize database connection once for all operations
        cnx = MyDataBase.getInstance().getCnx();
    }

    // Validate case form data
    public List<String> validateCase(Case caseObj) {
        List<String> errors = new ArrayList<>();

        // Mandatory fields
        if (caseObj.getTitle().isEmpty()) errors.add("Title is required");
        if (caseObj.getLocation().isEmpty()) errors.add("Location is required");
        if (caseObj.getDescription().isEmpty()) errors.add("Description is required");
        if (caseObj.getTelephone().isEmpty()) errors.add("Telephone is required");

        // Length validations
        if (caseObj.getTitle().length() > 30)
            errors.add("Title must be ≤30 characters");
        if (caseObj.getLocation().length() > 30)
            errors.add("Location must be ≤30 characters");
        if (caseObj.getDescription().length() > 500)
            errors.add("Description must be ≤500 characters");
        if (caseObj.getTelephone().length() > 15)
            errors.add("Telephone must be ≤15 digits");

        // Telephone numeric check
        if (!caseObj.getTelephone().matches("^[0-9]+$")) {
            errors.add("Telephone must contain only numbers");
        }

        return errors;
    }

    // Save case to database (includes validation)
    public List<String> saveCase(Case caseObj) {
        List<String> errors = validateCase(caseObj);

        if (errors.isEmpty()) {

            String query = "INSERT INTO legal_case (user_id, title, location, type, visibility, " +
                    "description, telephone, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                // Set parameters
                stmt.setInt(1, caseObj.getUserId());
                stmt.setString(2, caseObj.getTitle());
                stmt.setString(3, caseObj.getLocation());
                stmt.setString(4, caseObj.getType());
                stmt.setString(5, caseObj.getVisibility());
                stmt.setString(6, caseObj.getDescription());
                stmt.setString(7, caseObj.getTelephone());
                stmt.setString(8, caseObj.getStatus());
                stmt.setTimestamp(9, Timestamp.valueOf(caseObj.getCreatedAt()));

                // Execute and return success
                stmt.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());

            }
        }
        return errors;
    }

    public List<Case> getCases() {
        List<Case> cases = new ArrayList<>();
        String query = "SELECT * FROM legal_case";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Case caseObj = new Case(
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("location"),
                        rs.getString("type"),
                        rs.getString("visibility"),
                        rs.getString("description"),
                        rs.getString("telephone")
                );
                caseObj.setId(rs.getInt("id"));
                caseObj.setStatus(rs.getString("status"));
                caseObj.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                cases.add(caseObj);
            }
        } catch (SQLException e) {
            System.err.println("Error loading cases: " + e.getMessage());
        }
        return cases;
    }

    public ObservableList<Case> getCasesObservable() {
        return FXCollections.observableArrayList(getCases());
    }

    public Map<String, Integer> getCaseTypeCounts() {
        Map<String, Integer> counts = new HashMap<>();
        String query = "SELECT type, COUNT(*) as count FROM legal_case GROUP BY type";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                counts.put(rs.getString("type"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting type counts: " + e.getMessage());
        }
        return counts;
    }

    public void updateStatus(int id, String newStatus) {
        updateField(id, "status", newStatus);
    }

    public void updateVisibility(int id, String newVisibility) {
        updateField(id, "visibility", newVisibility);
    }

    private void updateField(int id, String field, String value) {
        String query = String.format("UPDATE legal_case SET %s = ? WHERE id = ?", field);
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, value);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating " + field + ": " + e.getMessage());
        }
    }

    public void deleteCase(int caseId) {
        String query = "DELETE FROM legal_case WHERE id = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, caseId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting case: " + e.getMessage());
        }

    }
}