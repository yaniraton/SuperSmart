package com.yanir.supersmart;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Activity for administrators to review and manage user-submitted image suggestions.
 * Displays a list of product barcodes with suggestions, and allows admins to approve or deny each image.
 * Approved images are processed through a Firebase Function and denied images are deleted from storage.
 */
public class AdminImageApprovalActivity extends AppCompatActivity {

    private RecyclerView recyclerViewSuggestions;
    private SuggestionProductAdapter suggestionProductAdapter;
    private List<String> barcodeList = new ArrayList<>();
    private FirebaseFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_image_approval);

        recyclerViewSuggestions = findViewById(R.id.recyclerViewSuggestions);
        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestionProductAdapter = new SuggestionProductAdapter(barcodeList);
        recyclerViewSuggestions.setAdapter(suggestionProductAdapter);

        suggestionProductAdapter.setOnBarcodeClickListener(barcode -> {
            loadSuggestedImagesForBarcode(barcode);
        });

        functions = FirebaseFunctions.getInstance();
        loadSuggestedBarcodes();
    }

    /**
     * Loads barcodes of products for which users have suggested images using a Firebase Function.
     * Populates the RecyclerView with the list of barcodes.
     */
    private void loadSuggestedBarcodes() {
        functions
            .getHttpsCallable("getProductsWithSuggestions")
            .call()
            .addOnSuccessListener(result -> {
                barcodeList.clear();
                barcodeList.addAll((List<String>) result.getData());
                suggestionProductAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Log.e("AdminImageApproval", "Error loading barcodes", e);
                Toast.makeText(this, "Failed to load suggestions", Toast.LENGTH_SHORT).show();
            });
    }

    /**
     * Loads suggested images for a specific product barcode from Firebase Storage.
     * Initializes an adapter that allows the admin to approve or deny each image.
     * @param barcode the product barcode to load suggestions for
     */
    private void loadSuggestedImagesForBarcode(String barcode) {
        Toast.makeText(this, "Loading images for barcode: " + barcode, Toast.LENGTH_SHORT).show();

        StorageReference suggestionsRef = DB.getInstance().getStorage()
                .child("Products")
                .child(barcode)
                .child("suggestions");

        suggestionsRef.listAll()
            .addOnSuccessListener(listResult -> {
                List<StorageReference> imageRefs = listResult.getItems();

                ImageApprovalAdapter imageAdapter = new ImageApprovalAdapter(imageRefs, new ImageApprovalAdapter.OnImageActionListener() {
                    @Override
                    public void onApprove(StorageReference ref) {
                        String filename = ref.getName();

                        functions
                            .getHttpsCallable("approveSuggestedImage")
                            .call(Map.of("barcode", barcode, "filename", filename))
                            .addOnSuccessListener(task -> {
                                Toast.makeText(AdminImageApprovalActivity.this, "Image approved", Toast.LENGTH_SHORT).show();
                                // Refresh the image list after approval
                                loadSuggestedImagesForBarcode(barcode);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ImageApproval", "Approval failed", e);
                                Toast.makeText(AdminImageApprovalActivity.this, "Failed to approve image", Toast.LENGTH_SHORT).show();
                            });
                    }

                    @Override
                    public void onDeny(StorageReference ref) {
                        ref.delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(AdminImageApprovalActivity.this, "Image denied (deleted)", Toast.LENGTH_SHORT).show();
                                // Refresh the image list after denial
                                loadSuggestedImagesForBarcode(barcode);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ImageApproval", "Deletion failed", e);
                                Toast.makeText(AdminImageApprovalActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show();
                            });
                    }
                });

                recyclerViewSuggestions.setAdapter(imageAdapter);
            })
            .addOnFailureListener(e -> {
                Log.e("ImageSuggestion", "Failed to list images for " + barcode, e);
                Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show();
            });
    }
}