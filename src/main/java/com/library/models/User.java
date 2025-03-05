package com.library.models;


public class User {
    private String id;
    private String name;
    private String email;
    private String password; // Hashed password
    private Role role;

    public enum Role {
        ADMIN, LIBRARIAN, STUDENT
    }

    public User(String id, String name, String email, String password, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; } // Ensure password is hashed
    public void setRole(Role role) { this.role = role; }
}