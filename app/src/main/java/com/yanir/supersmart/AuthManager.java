package com.yanir.supersmart;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.tasks.OnCompleteListener;

/**
 * AuthManager is a singleton class responsible for managing user authentication using Firebase Authentication.
 * It provides methods for registration, login, logout, and checking admin permissions.
 *
 * This is the only class in the application that interacts directly with FirebaseAuth.
 * All authentication-related operations from other parts of the app must go through this class.
 */
public class AuthManager {
    private static AuthManager instance = null;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private AuthManager() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://supersmart-yanir-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
    }

    /**
     * Returns the singleton instance of AuthManager.
     * @return the AuthManager instance
     */
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    /**
     * Retrieves the currently authenticated Firebase user.
     * @return the current FirebaseUser, or null if not logged in
     */
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    /**
     * Registers a new user with the provided email and password.
     * The result is delivered via the provided OnCompleteListener.
     *
     * @param email the user's email
     * @param password the user's password
     * @param listener the callback listener to handle success or failure
     */
    public void register(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    /**
     * Logs in a user with the provided email and password.
     * The result is delivered via the provided OnCompleteListener.
     *
     * @param email the user's email
     * @param password the user's password
     * @param listener the callback listener to handle success or failure
     */
    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    /**
     * Logs out the currently authenticated user.
     */
    public void logout() {
        auth.signOut();
    }

    /**
     * Checks if the user with the given UID has admin permissions.
     * The result is passed to the provided ValueEventListener.
     *
     * @param uid the user's UID
     * @param listener the listener to handle the permission value
     */
    public void isAdmin(String uid, ValueEventListener listener) {
        usersRef.child(uid).child("permission").addListenerForSingleValueEvent(listener);
    }
}