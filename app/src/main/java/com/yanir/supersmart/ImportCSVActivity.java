package com.yanir.supersmart;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportCSVActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private Uri fileUri;
    private RecyclerView recyclerView;
    private Button btnUploadCsv;
    private int totalLines = 0;
    private int validLines = 0;
    private List<Product> previewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_csv);

        btnUploadCsv = findViewById(R.id.btnUploadCsv);
        recyclerView = findViewById(R.id.rvParsedProducts);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        fileUri = result.getData().getData(); // Save for future upload

                        try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
                             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                            String line;
                            int previewLimit = 200;
                            previewList = new ArrayList<>();

                            while ((line = reader.readLine()) != null && previewList.size() < previewLimit) {
                                totalLines++;
                                String[] columns = line.split(",");
                                if (columns.length >= 3) {
                                    String barcode = columns[0].trim();
                                    String name = columns[1].trim();
                                    String priceStr = columns[2].trim();

                                    try {
                                        float price = Float.parseFloat(priceStr);
                                        validLines++;
                                        Product product = new Product(name, barcode, "", price);
                                        previewList.add(product);
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }

                            // Continue counting valid lines without storing products
                            while ((line = reader.readLine()) != null) {
                                totalLines++;
                                String[] columns = line.split(",");
                                if (columns.length >= 3) {
                                    String priceStr = columns[2].trim();
                                    try {
                                        Float.parseFloat(priceStr);
                                        validLines++;
                                    } catch (NumberFormatException ignored) {}
                                }
                            }

                            // Update UI with preview list and stats
                            findViewById(R.id.loadedCsvContainer).setVisibility(View.VISIBLE);
                            findViewById(R.id.btnUploadCsv).setVisibility(View.GONE);

                            recyclerView = findViewById(R.id.rvParsedProducts);
                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                            recyclerView.setAdapter(new ParsedProductAdapter(previewList));

                            TextView stats = findViewById(R.id.tvCsvStats);
                            if (validLines > previewList.size()) {
                                stats.setText("Previewing " + previewList.size() + " valid products from CSV.\nFile contains " + validLines + " valid products.");
                            } else {
                                stats.setText("Showing all " + validLines + " valid products from CSV.");
                            }

                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to preview CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                });

        btnUploadCsv.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/*"); // "text/csv" may not be accepted by all devices
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select CSV File"));
        });

        Button btnUploadToDb = findViewById(R.id.btnUploadToDb);
        btnUploadToDb.setOnClickListener(v -> uploadProducts());
    }

    private void uploadProducts() {
        // This method should be called when the user confirms the upload
        new AlertDialog.Builder(this)
            .setTitle("Confirm Upload")
            .setMessage("Are you sure you want to upload the products?")
            .setPositiveButton("Yes", (dialog, which) -> {
                try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                    String line;
                    int successCount = 0;
                    int failCount = 0;
                    int batchSize = 200;

                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    Map<String, Object> batchMap = new HashMap<>();

                    while ((line = reader.readLine()) != null) {
                        String[] columns = line.split(",");
                        if (columns.length >= 3) {
                            String barcode = columns[0].trim();
                            String name = columns[1].trim();
                            String priceStr = columns[2].trim();

                            try {
                                float price = Float.parseFloat(priceStr);
                                Product product = new Product(name, barcode, "", price);
                                batchMap.put("/Products/" + barcode, product);
                                successCount++;
                            } catch (NumberFormatException ignored) {
                                failCount++;
                            }

                            if (batchMap.size() >= batchSize) {
                                dbRef.updateChildren(batchMap);
                                batchMap.clear();
                            }
                        }
                    }

                    if (!batchMap.isEmpty()) {
                        dbRef.updateChildren(batchMap);
                    }

                    Toast.makeText(this, "Upload complete. Success: " + successCount + ", Skipped: " + failCount, Toast.LENGTH_LONG).show();
                    finish();

                } catch (Exception e) {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            })
            .setNegativeButton("No", null)
            .show();
    }
}