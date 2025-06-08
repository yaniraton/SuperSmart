package com.yanir.supersmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import com.yanir.supersmart.GeminiManager;
import com.yanir.supersmart.GeminiCallback;

/**
 * MainActivity serves as the primary entry point of the application.
 * It supports barcode scanning and image-based identification of fruits and vegetables
 * using Google's Gemini API. It also provides login/logout functionality,
 * including admin role-based access to management features.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private static final String TAG = "MainActivity_SuperSmart";

    private Uri photoUri;
    private ActivityResultLauncher<Uri> photoCaptureLauncher;

    /**
     * Called when the activity is starting. Initializes the UI, sets up button click handlers,
     * registers for activity results, and configures Gemini-based image recognition and camera handling.
     * @param savedInstanceState The saved instance state from previous activity states.
     */
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        Log.d(TAG, "Activity started");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // locks the screen in the horizontol state.
        Button btnLogin = findViewById(R.id.btnLogin);


        btnLogin.setOnClickListener(v -> handleLoginButtonClick());

        // Long press on login button logs the user out if currently logged in
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
                        String scannedBarcode = data.getStringExtra("barcode");

                        DB.getInstance().getProduct(scannedBarcode).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Intent intent = new Intent(MainActivity.this, product_screen.class);
                                    intent.putExtra("barcode", scannedBarcode);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this, "Product not found in database", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "Failed to check product", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Database error: ", error.toException());
                            }
                        });
                    }
                    if (result.getResultCode() == RESULT_CANCELED) {
                        ;
                    }
                }
        );

        Button btnIdentifyByPhoto = findViewById(R.id.btnIdentifyByPhoto);
        btnIdentifyByPhoto.setOnClickListener(v -> {
            if (!cameraPermissionGranted()) {
                requestCameraPermission();
            } else {
                File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg");
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                photoCaptureLauncher.launch(photoUri);
            }
        });

        photoCaptureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    // After image is captured, use Gemini to identify produce and redirect to product screen
                    if (result) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                            String prompt = "IYou are given a photo of a fruit or vegetable. You must identify it and return only its name, using one of the following known English names exactly: zucchini, eggplant, hot green pepper, dill, red pepper, yellow pepper, hot red pepper, melon, watermelon, onion, purple onion, kohlrabi, potato, dry garlic, pumpkin, packed potato, packed red potato, packed corn, kiwi, tomato, cherry tomato, cucumber, apple golden, apple hermon, apple granny smith, pear, white peach, red plum, red nectarine, orange, pomelo, lemon, banana, black grapes, apricot, red beet, lettuce, jerusalem artichoke, white cabbage, packed cilantro, celery, packed green onion, packed cherries, packed mushrooms, avocado, sweet potato, purple cabbage, white plum, green almonds, mango, clementine, fennel, persimmon, grapefruit, pomelit, baby leaves, yellow date, pomegranate, carrot, mint, cauliflower, parsley, loquat, guava, ginger, strawberries, pineapple, fresh figs, prickly pear, radish, broccoli, ripe avocado pack, white grapes, turnip, coconut, passionfruit.\n" +
                                    "If the item does not match any of these exactly, return the phrase: `unknown_fruit`. Do not return anything else. Use only lowercase, no punctuation or extra words.";
                            GeminiManager.getInstance().sendTextWithPhotoPrompt(prompt, bitmap, new GeminiCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    Log.d("Gemini", "onSuccess hit: " + result);
                                    String identifiedName = result.trim().toLowerCase(); // "tomato"

                                    DB.getInstance().getDatabaseRef().child("productMappings").child(identifiedName)
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        String mappedBarcode = snapshot.getValue(String.class); // "31"
                                                        Intent intent = new Intent(MainActivity.this, product_screen.class);
                                                        intent.putExtra("barcode", mappedBarcode);
                                                        startActivity(intent);
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "Unknown product: " + identifiedName, Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(MainActivity.this, "Failed to lookup product", Toast.LENGTH_SHORT).show();
                                                    Log.e("Gemini", "Firebase error: ", error.toException());
                                                }
                                            });
                                }
                                @Override
                                public void onFailure(Throwable t) {
                                    Log.e("Gemini", "Error: " + t.getMessage());
                                }
                            });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAdminStatus();
    }

    /**
     * Launches the barcode scanning activity if camera permission is granted.
     * If not, requests permission.
     * @param view The button view that triggered the event.
     */
    public void openBarcodeScanner(View view) {
        if (!cameraPermissionGranted()) {
            requestCameraPermission();
        } else {
            Intent intent = new Intent(this, BarcodeScan.class);
            activityResultLauncher.launch(intent);
        }
    }

    /**
     * Checks if camera permission has been granted.
     * @return true if permission is granted; false otherwise.
     */
    private boolean cameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests camera permission from the user with an explanatory dialog if needed.
     */
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

    /**
     * Callback for the result from requesting permissions.
     * If granted, it retries the barcode scanning; otherwise shows rationale or redirect to settings.
     */
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

    /**
     * Checks if the currently logged-in user is an admin.
     * Updates the login button accordingly.
     */
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

    /**
     * Updates UI when an admin user is detected.
     * @param btnLogin The login button to update.
     */
    private void onAdminDetected(Button btnLogin) {
        updateLoginButton(btnLogin, "Manage Products", R.drawable.worker);
        Log.d(TAG, "Admin user detected.");
    }

    /**
     * Updates the login button’s text and icon.
     * @param button The button to update.
     * @param text The new text to display.
     * @param drawableResId The drawable resource for the button icon.
     */
    private void updateLoginButton(Button button, String text, int drawableResId) {
        Drawable icon = ResourcesCompat.getDrawable(getResources(), drawableResId, null);
        button.setText(text);
        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }

    /**
     * Handles login button clicks.
     * Navigates to login screen or admin panel based on the user's authentication and role.
     */
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
                        Intent intent = new Intent(MainActivity.this, EmployeeManagementScreen.class);
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