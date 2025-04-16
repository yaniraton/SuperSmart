package com.yanir.supersmart;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class employee_management_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_management_screen);
        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(employee_management_screen.this, AdminApprovalActivity.class);
            startActivity(intent);
        });
    }
}