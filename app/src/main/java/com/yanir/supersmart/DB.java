package com.yanir.supersmart;


import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Singleton class that manages access to Firebase Realtime Database and Firebase Storage.
 * Provides centralized methods to retrieve database and storage references used throughout the app.
 */
public class DB {
    // singleton class
    final static String TAG = "DB";
    private FirebaseDatabase mDatabase;
    private FirebaseStorage mStorage;
    private static DB instance = null;

    /**
     * Returns the singleton instance of the DB class.
     * @return DB instance
     */
    public static DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    /**
     * Private constructor to initialize Firebase database and storage instances with specific URLs.
     */
    private DB(){
        mDatabase = FirebaseDatabase.getInstance("https://supersmart-yanir-default-rtdb.europe-west1.firebasedatabase.app/");
        mStorage = FirebaseStorage.getInstance("gs://supersmart-yanir.firebasestorage.app");
    }

    /**
     * Returns a DatabaseReference to a specific product node identified by its barcode.
     * @param barcode The barcode of the product
     * @return DatabaseReference to the product node
     */
    public DatabaseReference getProduct(String barcode) {
        return mDatabase.getReference("Products").child(barcode);
    }

    /**
     * Returns the root DatabaseReference of the Firebase Realtime Database.
     * @return Root DatabaseReference
     */
    public DatabaseReference getDatabaseRef() {
        return mDatabase.getReference();
    }

    /**
     * Returns a reference to the root of Firebase Storage.
     * @return StorageReference to the root of Firebase Storage
     */
    public StorageReference getStorage() {
        return mStorage.getReference();
    }

    /**
     * Returns a DatabaseReference to the 'productMappings' node used for produce-barcode mapping.
     * @return DatabaseReference to the 'productMappings' node
     */
    public DatabaseReference getFruitMappingRef() {
        return mDatabase.getReference("productMappings");
    }

    /**
     * Returns a DatabaseReference to a specific user node.
     * @param uid The UID of the user
     * @return DatabaseReference to the user node
     */
    public DatabaseReference getUserRef(String uid) {
        return mDatabase.getReference("Users").child(uid);
    }

}