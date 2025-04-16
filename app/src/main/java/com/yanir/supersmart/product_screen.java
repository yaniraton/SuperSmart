package com.yanir.supersmart;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.net.MalformedURLException;
import java.net.URL;

public class product_screen extends AppCompatActivity {

    final String TAG = "ProductScreen";
    ImageView ivProductImage;
    TextView tvProductName;
    TextView tvProductPrice;
    TextView tvProductDescription;
    //Button btnEditProduct;
    Button btnBackToHome;
    Product product;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // locks the screen in the horizontol state.
        // get the barcode from the intent
        String barcode = getIntent().getStringExtra("barcode");
        // log the barcode
        Log.d(TAG, "Barcode: " + barcode);
        // initialize the views
        initViews();
        Log.d(TAG, "Views initialized");
        // get the product data
        getProductData(barcode);
        // toste message - barcode
        Toast.makeText(this, "Barcode: " + barcode, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Product data retrieved");
    }

    private void initViews() {
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvProductDescription = findViewById(R.id.tvProductDescription);
        btnBackToHome = findViewById(R.id.btnBackToHome);
    }
    private void setProductData(Product product) {
        //ivProductImage.setImageResource(product.getImage());
        tvProductName.setText(product.getName());
        tvProductPrice.setText(product.getPrice() + "");
        tvProductDescription.setText(product.getDescription());
    }
    private void getProductData(String barcode) {
        // get a database reference to the product
        DB.getInstance().getProduct(barcode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // get the product data
                product = snapshot.getValue(Product.class);
                // set the product data
                if (product != null) {
                    setProductData(product);
                    DB.getInstance().getStorage()
                        .child("Products")
                        .child(product.getBarcode())
                        .child("approved")
                        .listAll()
                        .addOnSuccessListener(listResult -> {
                            if (!listResult.getItems().isEmpty()) {
                                listResult.getItems().get(0).getDownloadUrl()
                                    .addOnSuccessListener(uri -> loadProductImage(uri.toString()))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error getting image URL: " + e.getMessage()));
                            } else {
                                Log.d(TAG, "No approved images found for product");
                            }
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error listing approved images: " + e.getMessage()));
                }
                else {
                    Log.e(TAG, "Product data is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // log the error
                Log.e(TAG, "Error getting product data: " + error.getMessage());
            }
        });
    }

    private void loadProductImage(String imageUrl) {
       new Thread(() -> {
            try {
                URL url = new URL(imageUrl);
                Log.d(TAG, "Image URL: " + url);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                Log.d(TAG, "Image loaded");
                // Update the UI on the main thread
                runOnUiThread(() -> {
                    ivProductImage.setImageBitmap(bmp);
                    Log.d(TAG, "Image set");
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading product image: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public void backToHome(View view) {
        finish();
    }


}