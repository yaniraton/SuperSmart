package com.yanir.supersmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.drawable.Drawable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import com.yanir.supersmart.AuthManager;

public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private static final String TAG = "MainActivity_SuperSmart";


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        Log.d(TAG, "Activity started");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // locks the screen in the horizontol state.
        Button btnLogin = findViewById(R.id.btnLogin);
        checkAdminStatus();

        btnLogin.setOnClickListener(v -> handleLoginButtonClick());

        btnLogin.setOnLongClickListener(v -> {
            AuthManager authManager = AuthManager.getInstance();
            if (authManager.getCurrentUser() != null) {
                authManager.logout();
                Toast.makeText(MainActivity.this, "You have been successfully logged out", Toast.LENGTH_SHORT).show();
                updateLoginButton(btnLogin, "Login", R.drawable.login);
            }
            return true;
        });

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        // Log the data
                        Log.i(TAG, "Data: " + data.getStringExtra("barcode"));
                        Intent intent = new Intent(this, product_screen.class);
                        intent.putExtra("barcode", data.getStringExtra("barcode"));
                        startActivity(intent);
                    }
                    if (result.getResultCode() == RESULT_CANCELED) {
                        ;
                    }
                }
        );
    }

    //on click method
    public void openBarcodeScanner(View view) {
        if (!cameraPermissionGranted()) {
            requestCameraPermission();
        } else {
            Intent intent = new Intent(this, BarcodeScan.class);
            activityResultLauncher.launch(intent);
        }
    }

    private boolean cameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Camera Permission Needed")
                .setMessage("This app needs the camera to scan product barcodes. Please allow camera access.")
                .setPositiveButton("OK", (dialog, which) -> {
                    requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 0);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openBarcodeScanner(null); // permission granted, retry scan
            } else {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                    new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Camera permission was permanently denied. Please go to settings to enable it.")
                        .setPositiveButton("Go to Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void checkAdminStatus() {
        AuthManager authManager = AuthManager.getInstance();
        Button btnLogin = findViewById(R.id.btnLogin);

        if (authManager.getCurrentUser() == null) {
            updateLoginButton(btnLogin, "Login", R.drawable.login);
            Log.d(TAG, "No user is logged in.");
            return;
        }

        authManager.isAdmin(authManager.getCurrentUser().getUid(), new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isAdmin = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(isAdmin)) {
                    onAdminDetected(btnLogin);
                } else {
                    updateLoginButton(btnLogin, "Connected (Not Admin)", R.drawable.login);
                    Log.d(TAG, "User is not admin.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check admin status", error.toException());
            }
        });
    }

    private void onAdminDetected(Button btnLogin) {
        updateLoginButton(btnLogin, "Manage Products", R.drawable.worker);
        Log.d(TAG, "Admin user detected.");
    }

    private void updateLoginButton(Button button, String text, int drawableResId) {
        Drawable icon = ResourcesCompat.getDrawable(getResources(), drawableResId, null);
        button.setText(text);
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    private void handleLoginButtonClick() {
        AuthManager authManager = AuthManager.getInstance();
        if (authManager.getCurrentUser() == null) {
            // Not logged in – go to login screen
            Intent intent = new Intent(this, login_screen.class);
            startActivity(intent);
        } else {
            authManager.isAdmin(authManager.getCurrentUser().getUid(), new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Boolean isAdmin = snapshot.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(isAdmin)) {
                        // Admin – go to admin panel
                        Intent intent = new Intent(MainActivity.this, employee_management_screen.class);
                        startActivity(intent);
                    } else {
                        // Logged in but not admin – do nothing
                        Toast.makeText(MainActivity.this, "You are logged in but not an admin.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to check admin status", error.toException());
                }
            });
        }
    }

}