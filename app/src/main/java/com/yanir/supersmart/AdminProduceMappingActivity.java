package com.yanir.supersmart;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * AdminProduceMappingActivity is an administrative screen that allows admins
 * to view and update the mapping between produce names and their assigned barcode numbers.
 * Data is fetched from and saved to the Firebase Realtime Database using the DB helper class.
 */
public class AdminProduceMappingActivity extends AppCompatActivity {

    private RecyclerView mappingRecyclerView;
    private Button saveButton;

    /**
     * Initializes the activity, loads the current produce-to-barcode mappings from Firebase,
     * and sets up the RecyclerView and save functionality.
     * When the save button is clicked, all changes are uploaded to Firebase.
     *
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_produce_mapping);

        mappingRecyclerView = findViewById(R.id.rvProduceMappings);
        saveButton = findViewById(R.id.btnSaveMappings);

        mappingRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DB.getInstance().getFruitMappingRef().addListenerForSingleValueEvent(new ValueEventListener() {
            /**
             * Callback method invoked when the produce mapping data is successfully retrieved from Firebase.
             * Parses the data and displays it in the RecyclerView.
             *
             * @param snapshot the data retrieved from Firebase
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> produceMap = new HashMap<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.getKey();
                    Long valueStr = child.getValue(Long.class); // get as String

                    try {
                        int barcode = Math.toIntExact(valueStr);;
                        if (name != null) {
                            produceMap.put(name, barcode);
                        }
                    } catch (NumberFormatException e) {
                        // Log or handle invalid number format
                    }
                }

                ProduceMappingAdapter adapter = new ProduceMappingAdapter(produceMap);
                mappingRecyclerView.setAdapter(adapter);

                saveButton.setOnClickListener(v -> {
                    Map<String, Integer> updatedMap = adapter.getCurrentMappings();
                    DB.getInstance().getFruitMappingRef().setValue(updatedMap).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AdminProduceMappingActivity.this, "Mappings saved successfully.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminProduceMappingActivity.this, "Failed to save mappings.", Toast.LENGTH_SHORT).show();
                            Log.e("AdminProduceMapping", "Save failed", task.getException());
                        }
                    });
                });
            }

            /**
             * Callback method invoked when data retrieval from Firebase is cancelled or fails.
             * Logs the error and displays a toast to the user.
             *
             * @param error the DatabaseError describing the failure
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminProduceMapping", "Database read failed", error.toException());
                Toast.makeText(AdminProduceMappingActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}