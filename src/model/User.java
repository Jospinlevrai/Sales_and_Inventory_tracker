package model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private int roleId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phoneNumber;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public User(int userId, int roleId, String username, String passwordHash, String fullName,
                String email, String phoneNumber, boolean active, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getUserId() { 
        return userId; }
    public void setUserId(int userId) { 
        this.userId = userId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { 
        this.roleId = roleId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username; }

    public String getPasswordHash() {
         return passwordHash; }
    public void setPasswordHash(String passwordHash) {
         this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
         this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
         this.email = email; }

    public String getPhoneNumber() {
         return phoneNumber; 
        
        }
    public void setPhoneNumber(String phoneNumber) 
    { this.phoneNumber = phoneNumber;

     }

    public boolean isActive() { return active; }
    public void setActive(boolean active) {
         this.active = active;
         }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
         this.createdAt = createdAt;
         }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", roleId=" + roleId + ", username=" + username
                + ", fullName=" + fullName + ", active=" + active + "}";
    }
}
