package com.yanir.supersmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * The EmployeeManagementScreen is the central admin interface for managing application resources.
 * It provides navigation to various administrative tasks such as managing users, adding items,
 * approving images, importing CSV data, and editing produce mappings.
 */
public class EmployeeManagementScreen extends AppCompatActivity {

    private ActivityResultLauncher<Intent> activityResultLauncher;

    /**
     * Initializes the activity and sets up button click listeners that navigate to various
     * administrative functions within the application.
     *
     * @param savedInstanceState A Bundle containing the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra("barcode")) {
                            String scannedBarcode = data.getStringExtra("barcode");
                            if (scannedBarcode != null && !scannedBarcode.isEmpty()) {
                                new AlertDialog.Builder(EmployeeManagementScreen.this)
                                    .setTitle("Confirm Deletion")
                                    .setMessage("Are you sure you want to delete this product?\n\nBarcode: " + Html.fromHtml("<b>" + scannedBarcode + "</b>"))
                                    .setPositiveButton("Yes", (dialogInterface, which) -> {
                                        DB.getInstance().deleteProduct(scannedBarcode, task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EmployeeManagementScreen.this, "Product deleted successfully.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(EmployeeManagementScreen.this, "Failed to delete product.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                            } else {
                                Toast.makeText(EmployeeManagementScreen.this, "Scanned barcode is invalid.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        setContentView(R.layout.employee_management_screen);
        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeManagementScreen.this, AdminApprovalActivity.class);
            startActivity(intent);
        });

        Button btnAddItem = findViewById(R.id.btnAddNewItem);
        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeManagementScreen.this, EditItemActivity.class);
            startActivity(intent);
        });

        Button btnApproveSuggestedImages = findViewById(R.id.btnApproveSuggestedImages);
        btnApproveSuggestedImages.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeManagementScreen.this, AdminImageApprovalActivity.class);
            startActivity(intent);
        });

        Button btnImportCSV = findViewById(R.id.btnImportCSV);
        btnImportCSV.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeManagementScreen.this, ImportCSVActivity.class);
            startActivity(intent);
        });

        Button btnEditProduceMappings = findViewById(R.id.btnEditProduceMappings);
        btnEditProduceMappings.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeManagementScreen.this, AdminProduceMappingActivity.class);
            startActivity(intent);
        });

        Button btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
        btnDeleteProduct.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeManagementScreen.this);
            builder.setTitle("Delete Product")
                   .setMessage("Choose how you want to identify the product to delete:")
                   .setPositiveButton("Enter Barcode", (dialog, which) -> {
                       AlertDialog.Builder inputDialog = new AlertDialog.Builder(EmployeeManagementScreen.this);
                       inputDialog.setTitle("Enter Product Barcode");

                       final EditText input = new EditText(EmployeeManagementScreen.this);
                       input.setInputType(InputType.TYPE_CLASS_NUMBER);
                       inputDialog.setView(input);

                       inputDialog.setPositiveButton("Delete", (dialogInterface, i1) -> {
                           String barcode = input.getText().toString().trim();
                           if (!barcode.isEmpty()) {
                               DB.getInstance().deleteProduct(barcode, task -> {
                                   if (task.isSuccessful()) {
                                       Toast.makeText(EmployeeManagementScreen.this, "Product deleted successfully.", Toast.LENGTH_SHORT).show();
                                   } else {
                                       Toast.makeText(EmployeeManagementScreen.this, "Failed to delete product.", Toast.LENGTH_SHORT).show();
                                   }
                               });
                           } else {
                               Toast.makeText(EmployeeManagementScreen.this, "Barcode cannot be empty.", Toast.LENGTH_SHORT).show();
                           }
                       });

                       inputDialog.setNegativeButton("Cancel", (dialogInterface, i1) -> dialogInterface.cancel());
                       inputDialog.show();
                   })
                   .setNegativeButton("Scan Barcode", (dialog, which) -> {
                       Intent intent = new Intent(EmployeeManagementScreen.this, BarcodeScan.class);
                       activityResultLauncher.launch(intent);
                   })
                   .setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss())
                   .show();
        });

    }
}