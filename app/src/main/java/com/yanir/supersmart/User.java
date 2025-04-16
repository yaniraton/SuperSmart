package com.yanir.supersmart;

public class User {

    private String uid;
    private String email;
    private String displayName;
    private boolean permission;

    // Required empty constructor for Firebase
    public User() {
    }

    // Full constructor
    public User(String uid, String email, String displayName, boolean permission) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.permission = permission;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }
}