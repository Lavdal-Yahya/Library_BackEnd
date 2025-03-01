package com.library.models;

public class User {
    private String id;
    private String name;
    private String password; // Used for authentication
    private Role role; // Defines user permissions

    public enum Role {
        ADMIN, LIBRARIAN, STUDENT
    }

    public User(String id, String name, String password, Role role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
}
