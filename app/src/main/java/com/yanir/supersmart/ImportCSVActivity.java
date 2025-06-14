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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ImportCSVActivity allows the admin to upload a CSV file of products,
 * preview a portion of the file, and upload valid entries to Firebase Realtime Database.
 *
 * The CSV file should be in the format: barcode,name,price
 * - The app displays a preview of up to 200 valid products.
 * - Upon confirmation, the entire file is parsed again and uploaded to Firebase in batches.
 *
 * This activity is useful for bulk-adding products to the system.
 */
public class ImportCSVActivity extends AppCompatActivity {

    // Launches the system file picker to choose a CSV file
    private ActivityResultLauncher<Intent> filePickerLauncher;

    // Stores the selected file URI for reading
    private Uri fileUri;

    // Displays the preview list of parsed products
    private RecyclerView recyclerView;

    // Button to trigger CSV file selection
    private Button btnUploadCsv;

    // Stats used for UI display
    private int totalLines = 0;
    private int validLines = 0;

    // Temporarily holds products parsed from CSV for previewing
    private List<Product> previewList = new ArrayList<>();

    /**
     * Initializes the activity, sets up UI elements and file picker launcher.
     * Handles CSV file selection, parsing for preview, and UI updates.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_csv);

        btnUploadCsv = findViewById(R.id.btnUploadCsv);
        recyclerView = findViewById(R.id.rvParsedProducts);

        // Initialize file picker to handle result
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        fileUri = result.getData().getData(); // Save selected file URI

                        try (InputStream inputStream = getContentResolver().openInputStream(fileUri);
                             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                            String line;
                            int previewLimit = 200;
                            previewList = new ArrayList<>();

                            // Read lines from file to build preview list (up to 200 valid entries)
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
                                        previewList.add(product); // Add valid product to preview
                                    } catch (NumberFormatException ignored) {
                                        // Skip invalid price
                                    }
                                }
                            }

                            // Continue counting valid entries (for stats) after preview limit
                            while ((line = reader.readLine()) != null) {
                                totalLines++;
                                String[] columns = line.split(",");
                                if (columns.length >= 3) {
                                    String priceStr = columns[2].trim();
                                    try {
                                        Float.parseFloat(priceStr);
                                        validLines++;
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }

                            // Update UI with preview and statistics
                            findViewById(R.id.loadedCsvContainer).setVisibility(View.VISIBLE);
                            findViewById(R.id.btnUploadCsv).setVisibility(View.GONE);

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

        // When clicking upload, open the file picker
        btnUploadCsv.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/*"); // Some devices may not support "text/csv"
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(Intent.createChooser(intent, "Select CSV File"));
        });

        // Upload confirmed preview to database
        Button btnUploadToDb = findViewById(R.id.btnUploadToDb);
        btnUploadToDb.setOnClickListener(v -> uploadProducts());
    }

    /**
     * Reads the CSV file again and uploads all valid entries to Firebase Realtime Database.
     * Batches updates in groups of 200 to reduce write load.
     */
    private void uploadProducts() {
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

                    DatabaseReference dbRef = DB.getInstance().getDatabaseRef();
                    Map<String, Object> batchMap = new HashMap<>();

                    // Re-parse the file and collect valid entries
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

                            // Upload in batches
                            if (batchMap.size() >= batchSize) {
                                dbRef.updateChildren(batchMap);
                                batchMap.clear();
                            }
                        }
                    }

                    // Final batch push
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