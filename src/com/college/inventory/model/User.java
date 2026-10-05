package com.college.inventory.model;

/**
 * Represents an authenticated staff or admin user in the inventory system.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String fullName;
    private String role; // "ADMIN", "LAB_ASSISTANT", "HOD", "FACULTY"
    private String department;

    public User() {}

    public User(int id, String username, String password, String fullName, String role, String department) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
    }

    public User(String username, String password, String fullName, String role, String department) {
        this(-1, username, password, fullName, role, department);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
