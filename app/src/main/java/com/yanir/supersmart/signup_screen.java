package com.yanir.supersmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.Collections;

/**
 * signup_screen is the user registration screen for the SuperSmart app.
 * <p>
 * This Activity allows new users to create an account by entering their email, password,
 * and display name. It handles user input, validates the registration data, interacts with
 * the AuthManager for Firebase Authentication, and sets up the display name via a Firebase Function.
 * </p>
 */
public class signup_screen extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etDisplayName;
    private Button btnSignUp;
    private AuthManager authManager;
    private TextView tvLoginFooter;
    FirebaseFunctions functions = FirebaseFunctions.getInstance();


    @Override
    /**
     * Called when the activity is starting.
     * <p>
     * This method sets up the user interface for the signup screen, initializes UI elements,
     * configures user interaction handlers, and manages the registration workflow including
     * input validation and communication with the AuthManager for Firebase Authentication.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etDisplayName = findViewById(R.id.etUsername);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLoginFooter = findViewById(R.id.tvLoginFooter);

        authManager = AuthManager.getInstance();

        btnSignUp.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.register(email, password, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, login_screen.class)); // Make sure this class exists
                    setDisplayName(etDisplayName.getText().toString().trim());
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(this, "Signup failed: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            });
        });

        tvLoginFooter.setOnClickListener(v -> {
            startActivity(new Intent(this, login_screen.class));
            finish();
        });
    }

    /**
     * Calls a Firebase Function to set the user's display name after successful signup.
     * <p>
     * This method invokes the 'setDisplayName' HTTPS Callable Firebase Function directly,
     * passing the provided display name as a parameter. There is no FunctionsManager,
     * so direct access to Firebase Functions is acceptable here.
     * </p>
     *
     * @param displayName The display name to set for the newly registered user.
     */
    private void setDisplayName(String displayName) {
        functions
                .getHttpsCallable("setDisplayName")
                .call(Collections.singletonMap("displayName", displayName))
                .addOnSuccessListener(result -> {
                    Log.d("Signup", "Display name updated");
                })
                .addOnFailureListener(e -> {
                    Log.e("Signup", "Failed to update display name", e);
                });
    }
}