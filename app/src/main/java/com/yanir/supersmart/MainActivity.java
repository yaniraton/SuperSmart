package com.yanir.supersmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
                        Log.i(TAG, "No camera permission granted");
                        Toast.makeText(this, "No camera permission granted", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    //on click method
    public void openBarcodeScanner(View view) {
        Intent intent = new Intent(this, BarcodeScan.class);
        activityResultLauncher.launch(intent);
    }


}