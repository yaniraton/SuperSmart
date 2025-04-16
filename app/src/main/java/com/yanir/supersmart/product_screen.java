package com.yanir.supersmart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class product_screen extends AppCompatActivity {

    final String TAG = "ProductScreen";
    ImageView ivProductImage;
    TextView tvProductName;
    TextView tvProductPrice;
    TextView tvProductDescription;
    Button btnEditProduct;
    RecyclerView rvProductImages;
    List<StorageReference> imageRefs = new ArrayList<>();
    Product product;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // locks the screen in the horizontol state.
        String barcode = getIntent().getStringExtra("barcode");
        Log.d(TAG, "Barcode: " + barcode);
        initViews();
        Log.d(TAG, "Views initialized");
        getProductData(barcode);
        Toast.makeText(this, "Barcode: " + barcode, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Product data retrieved");
    }

    private void initViews() {
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        btnEditProduct = findViewById(R.id.btnEditProduct);
        rvProductImages = findViewById(R.id.rvProductImages);
        rvProductImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        String uid = AuthManager.getInstance().getCurrentUser().getUid();
        AuthManager.getInstance().isAdmin(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isAdmin = snapshot.getValue(Boolean.class);
                if (Boolean.TRUE.equals(isAdmin)) {
                    btnEditProduct.setVisibility(View.VISIBLE);
                    btnEditProduct.setOnClickListener(v -> {
                        Intent intent = new Intent(product_screen.this, EditItem.class);
                        intent.putExtra("barcode", product.getBarcode());
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to check admin status for edit button: " + error.getMessage());
            }
        });
        findViewById(R.id.fabAddPhoto).setOnClickListener(v -> {
            if (product != null) {
                Intent intent = new Intent(product_screen.this, SuggestImageActivity.class);
                intent.putExtra("barcode", product.getBarcode());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Product data not loaded yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setProductData(Product product) {
        tvProductName.setText(product.getName());
        tvProductPrice.setText(product.getPrice() + "");
        tvProductDescription.setText(product.getDescription());
    }

    private void getProductData(String barcode) {
        DB.getInstance().getProduct(barcode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                product = snapshot.getValue(Product.class);
                if (product != null) {
                    setProductData(product);
                    DB.getInstance().getStorage()
                        .child("Products")
                        .child(product.getBarcode())
                        .child("approved")
                        .listAll()
                        .addOnSuccessListener(listResult -> {
                            imageRefs.clear();
                            imageRefs.addAll(listResult.getItems());
                            rvProductImages.setAdapter(new ProductImageAdapter(imageRefs, product_screen.this));
                            Log.d(TAG, "Loaded " + imageRefs.size() + " approved images.");
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error listing approved images: " + e.getMessage()));
                } else {
                    Log.e(TAG, "Product data is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error getting product data: " + error.getMessage());
            }
        });
    }
}