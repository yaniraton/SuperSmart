package com.yanir.supersmart;


import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DB {
    // singleton class
    final static String TAG = "DB";
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private static DB instance = null;

    public static DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    private DB(){
        mDatabase = FirebaseDatabase.getInstance("https://supersmart-yanir-default-rtdb.europe-west1.firebasedatabase.app/");
        mStorage = FirebaseStorage.getInstance("gs://supersmart-yanir.firebasestorage.app");
    }

    public DatabaseReference getProduct(String barcode) {
        return mDatabase.getReference("Products").child(barcode);
    }
    public void addProduct(Product product) {
        // refrance to the product in the database
        mDatabase.getReference("Products").child(product.getBarcode()).setValue(product);
        Log.d(TAG, "Product added to database");
    }

    public StorageReference getImage(Product product) {
        return mStorage.getReference().child("products").child(product.getBarcode() + ".JPG");
    }

    public StorageReference getStorage() {
        return mStorage.getReference();
    }

}
