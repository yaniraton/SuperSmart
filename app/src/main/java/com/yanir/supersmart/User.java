package com.yanir.supersmart;

/**
 * Represents a user in the SuperSmart app.
 * This class is used to store and retrieve user information from Firebase,
 * including UID, email, display name, and permission level.
 */

public class User {

    private String uid;
    private String email;
    private String displayName;
    private boolean permission;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(User.class).
     */
    public User() {
    }

    /**
     * Constructs a new User with specified attributes.
     *
     * @param uid The unique identifier of the user.
     * @param email The user's email address.
     * @param displayName The name displayed for the user.
     * @param permission True if the user has admin permissions; false otherwise.
     */
    public User(String uid, String email, String displayName, boolean permission) {
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.permission = permission;
    }

    // Getters and Setters

    /**
     * Returns the user's UID.
     *
     * @return UID as a string.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the user's UID.
     *
     * @param uid The UID to set.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Returns the user's email address.
     *
     * @return Email as a string.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the user's display name.
     *
     * @return Display name as a string.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the user's display name.
     *
     * @param displayName The name to display.
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the user's permission status.
     *
     * @return True if the user has admin permissions; false otherwise.
     */
    public boolean isPermission() {
        return permission;
    }

    /**
     * Sets the user's permission status.
     *
     * @param permission True to grant admin permissions; false otherwise.
     */
    public void setPermission(boolean permission) {
        this.permission = permission;
    }
}