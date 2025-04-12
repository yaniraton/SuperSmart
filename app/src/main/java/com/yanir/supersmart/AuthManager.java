package com.yanir.supersmart;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.android.gms.tasks.OnCompleteListener;

public class AuthManager {
    private static AuthManager instance = null;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;

    private AuthManager() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://supersmart-yanir-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("Users");
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void register(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void login(String email, String password, OnCompleteListener<AuthResult> listener) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(listener);
    }

    public void logout() {
        auth.signOut();
    }

    public void isAdmin(String uid, ValueEventListener listener) {
        usersRef.child(uid).child("permission").addListenerForSingleValueEvent(listener);
    }
}