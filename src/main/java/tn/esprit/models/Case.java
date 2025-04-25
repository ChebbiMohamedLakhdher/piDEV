package tn.esprit.models;

import java.time.LocalDateTime;

public class Case {
    private int id;
    private int userId;
    private String title;
    private String location;
    private String type;
    private String visibility;
    private String description;
    private String telephone;
    private String status = "OPEN"; // Default value
    private LocalDateTime createdAt;
    // claimedById, claimStatus, resolvedAt omitted for now

    // Constructor without auto-generated fields (id, createdAt)
    public Case(int userId, String title, String location, String type,
                String visibility, String description, String telephone) {
        this.userId = userId;
        this.title = title;
        this.location = location;
        this.type = type;
        this.visibility = visibility;
        this.description = description;
        this.telephone = telephone;
        this.createdAt = LocalDateTime.now(); // Auto-set timestamp
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
