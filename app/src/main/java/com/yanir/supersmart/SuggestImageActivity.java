package com.yanir.supersmart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yanir.supersmart.AuthManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SuggestImageActivity extends AppCompatActivity {

    private static final String TAG = "SuggestImageActivity";
    private ImageView ivPreviewImage;
    private Uri selectedImageUri;
    private Uri cameraImageUri;
    private File photoFile;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_image);

        ivPreviewImage = findViewById(R.id.ivPreviewImage);

        Button btnReplaceImage = findViewById(R.id.btnReplaceImage);
        btnReplaceImage.setOnClickListener(v -> showImageChoiceSheet());

        Button btnUploadImage = findViewById(R.id.btnUploadImage);
        btnUploadImage.setOnClickListener(v -> uploadSelectedImage());

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Upload cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivPreviewImage.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                        selectedImageUri = cameraImageUri;
                        ivPreviewImage.setImageURI(selectedImageUri);
                    } else {
                        Toast.makeText(this, "No photo captured", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void openCamera() {
        Log.d(TAG, "openCamera() called");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
                Log.d(TAG, "Photo file created: " + photoFile.getAbsolutePath());
            } catch (IOException ex) {
                Log.e(TAG, "Error creating file", ex);
                Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this,
                        "com.yanir.supersmart.fileprovider",
                        photoFile);
                Log.d(TAG, "Camera image URI: " + cameraImageUri);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraLauncher.launch(takePictureIntent);
                Log.d(TAG, "Camera intent launched");
            }
        } else {
            Log.w(TAG, "No camera activity available to handle intent.");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void showImageChoiceSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_image_choice, null);
        bottomSheetDialog.setContentView(sheetView);

        sheetView.findViewById(R.id.btnCamera).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            openCamera();
        });

        sheetView.findViewById(R.id.btnGallery).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            openImagePicker();
        });

        bottomSheetDialog.show();
    }

    private void uploadSelectedImage() {
        String barcode = getIntent().getStringExtra("barcode");
        if (selectedImageUri == null || barcode == null) {
            Toast.makeText(this, "Missing image or product barcode", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = AuthManager.getInstance().getCurrentUser().getUid();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String random = java.util.UUID.randomUUID().toString().substring(0, 6);
        String filename = uid + "_" + timestamp + "_" + random + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference()
                .child("Products")
                .child(barcode)
                .child("suggestions")
                .child(filename);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "Upload successful! Awaiting admin approval.", Toast.LENGTH_LONG).show();
                    finish(); // optionally return to product screen
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}