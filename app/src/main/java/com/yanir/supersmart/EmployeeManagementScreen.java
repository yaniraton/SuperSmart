package com.yanir.supersmart;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class EmployeeManagementScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_management_screen);
        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeManagementScreen.this, AdminApprovalActivity.class);
            startActivity(intent);
        });

        Button btnAddItem = findViewById(R.id.btnAddNewItem);
        btnAddItem.setOnClickListener(v -> {
            Intent intent = new Intent(EmployeeManagementScreen.this, EditItem.class);
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
    }
}