package com.yanir.supersmart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScan extends AppCompatActivity {
    private PreviewView previewView;
    private Intent resultIntent;
    private ExecutorService cameraExecutor;
    private static final String TAG = "BarcodeScanner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.barcode_scan_screen);
        if (!cameraPermissionGranted()) {
            requestCameraPermission();
        }
        if (!cameraPermissionGranted()) {
            Log.e(TAG, "Camera permission not granted");
            setResult(RESULT_CANCELED);
            finish();
        }
        Log.d(TAG, "Camera permission granted");
        previewView = findViewById(R.id.previewView);
        resultIntent = new Intent();
        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();
    }

    private boolean cameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 0);
    }



    private void startCamera() {
        Log.d(TAG, "Initializing Camera...");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Log.d(TAG, "CameraProvider obtained");

                // Set up the camera preview use case
                Preview preview = new Preview.Builder().build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Set up the camera selector
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                if (cameraProvider.hasCamera(cameraSelector)) {
                    Log.d(TAG, "Camera found");
                } else {
                    Log.e(TAG, "Camera not found");
                    setResult(RESULT_CANCELED);
                    finish();
                }

                // Set up image analysis for barcode scanning
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                // Bind all use cases to the lifecycle
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

                Log.d(TAG, "Camera is now running");

            } catch (Exception e) {
                Log.e(TAG, "Failed to bind use cases", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeImage(@NonNull ImageProxy imageProxy) {
        try {
            @SuppressWarnings("UnsafeOptInUsageError")
            InputImage inputImage = InputImage.fromMediaImage(
                    imageProxy.getImage(),
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            BarcodeScanner scanner = BarcodeScanning.getClient();
            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            Log.d(TAG, "Barcode detected: " + rawValue);
                            resultIntent.putExtra("barcode", rawValue);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                            break; // Stop after processing the first barcode
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Barcode scanning failed", e))
                    .addOnCompleteListener(task -> {
                        imageProxy.close(); // Important to close the image
                        Log.d(TAG, "Image processing complete");
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error during image analysis", e);
            imageProxy.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null)
            cameraExecutor.shutdown();
        Log.d(TAG, "Camera executor shut down");
    }
}