package com.lottery.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private int userID;
    private String passwordHash;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private LocalDate createdDate;
    private LocalDate lastLoginDate;
    private boolean isActive;
    private java.math.BigDecimal balance;
    private boolean passwordSet = false;

    public User() {
    }

    public User(String passwordHash, String email) {
        this.passwordHash = passwordHash;
        this.email = email;
        this.isActive = true;
        this.role = "user"; 
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public void setCreationDate(LocalDateTime createdDate) {
        if (createdDate != null) {
            this.createdDate = createdDate.toLocalDate();
        }
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public void setLastLoginDate(java.sql.Date lastLoginDate) {
        if (lastLoginDate != null) {
            this.lastLoginDate = lastLoginDate.toLocalDate();
        }
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public java.math.BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(java.math.BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isPasswordSet() {
        return passwordSet;
    }

    public void setPasswordSet(boolean passwordSet) {
        this.passwordSet = passwordSet;
    }

    public String getDisplayName() {
        String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        name = name.trim();
        return name.isEmpty() ? email : name;
    }

}