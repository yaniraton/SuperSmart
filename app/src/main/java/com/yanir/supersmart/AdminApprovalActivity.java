package com.yanir.supersmart;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that allows administrators to approve or manage users.
 * Displays a list of all registered users retrieved from Firebase Realtime Database,
 * using a RecyclerView with a UserAdapter.
 */
public class AdminApprovalActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();

    /**
     * Initializes the activity, sets up the RecyclerView for displaying users,
     * and listens to Firebase for user data updates.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_approval);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userList);
        recyclerViewUsers.setAdapter(userAdapter);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.addValueEventListener(new ValueEventListener() {
            /**
             * Called when data at the "Users" Firebase database reference is changed.
             * Clears and repopulates the user list, then updates the adapter.
             *
             * @param snapshot The data snapshot containing the latest user data.
             */
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    User user = child.getValue(User.class);
                    if (user != null) {
                        user.setUid(child.getKey());
                        userList.add(user);
                    }
                }
                userAdapter.setUsers(userList);
            }

            /**
             * Called when the listener on the Firebase database is cancelled or fails.
             *
             * @param error DatabaseError indicating why the operation failed.
             */
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AdminApprovalActivity", "Failed to load users", error.toException());
            }
        });
    }
}