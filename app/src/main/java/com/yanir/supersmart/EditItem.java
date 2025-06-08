package com.yanir.supersmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Activity for adding or editing a product in the database.
 * If launched with a barcode, the activity loads existing product details for editing.
 * Otherwise, it allows adding a new product with a scanned barcode.
 */
public class EditItem extends AppCompatActivity {

    /**
     * Request code for starting the barcode scanning activity.
     */
    private static final int BARCODE_SCAN_REQUEST = 1001;
    private EditText etProductName, etPrice, etDescription, etBarcode;
    private Button btnSaveItem, btnScanBarcode;

    @Override
    /**
     * Initializes the activity, sets up UI components, and handles both edit and add modes
     * based on the presence of a barcode passed via Intent.
     * Connects to Firebase Realtime Database to load and save product information.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_screen);

        etProductName = findViewById(R.id.etProductName);
        etPrice = findViewById(R.id.etPrice);
        etDescription = findViewById(R.id.etDescription);
        etBarcode = findViewById(R.id.etBarcode);
        btnSaveItem = findViewById(R.id.btnSaveItem);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);

        String barcode = getIntent().getStringExtra("barcode");

        if (barcode != null) {
            // Edit mode
            etBarcode.setText(barcode);
            etBarcode.setEnabled(false);
            btnScanBarcode.setVisibility(View.GONE);
        } else {
            // Add mode
            btnScanBarcode.setVisibility(View.VISIBLE);
            btnScanBarcode.setOnClickListener(v -> {
                Intent intent = new Intent(EditItem.this, BarcodeScan.class);
                startActivityForResult(intent, BARCODE_SCAN_REQUEST);
            });
        }

        if (barcode != null) {
            DatabaseReference ref = DB.getInstance().getProduct(barcode);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Product product = snapshot.getValue(Product.class);
                        if (product != null) {
                            etProductName.setText(product.getName());
                            etPrice.setText(String.valueOf(product.getPrice()));
                            etDescription.setText(product.getDescription());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(EditItem.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Save or update the product in Firebase when the user clicks the save button.
        btnSaveItem.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String code = etBarcode.getText().toString().trim();
            float price = 0;
            try {
                price = Float.parseFloat(etPrice.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(EditItem.this, "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }

            Product product = new Product(name, code, description, price);

            DB.getInstance().getProduct(code)
                .setValue(product)
                .addOnSuccessListener(unused -> Toast.makeText(EditItem.this, "Product saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(EditItem.this, "Save failed", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    /**
     * Handles the result from the barcode scanning activity.
     * Updates the barcode EditText with the scanned value.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode The integer result code returned by the child activity.
     * @param data An Intent, which can return result data to the caller.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_SCAN_REQUEST && resultCode == RESULT_OK && data != null) {
            String scannedBarcode = data.getStringExtra("barcode");
            if (scannedBarcode != null) {
                etBarcode.setText(scannedBarcode);
            }
        }
    }
}