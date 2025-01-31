package ent.orderManagement.model;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String password;
    private String role; // e.g. "ADMIN", "USER"
    // getters/setters
       // Getters
       public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
