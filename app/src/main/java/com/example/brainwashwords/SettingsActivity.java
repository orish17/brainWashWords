package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends BaseActivity {

    private EditText usernameEditText;
    private Button saveUsernameButton, logoutButton, resetProgressButton;
    private SharedPreferences prefs;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupDrawer();

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("uid", null);

        usernameEditText = findViewById(R.id.edit_username);
        saveUsernameButton = findViewById(R.id.btn_save_username);
        logoutButton = findViewById(R.id.btn_logout);
        resetProgressButton = findViewById(R.id.btn_reset_stats);

        String currentUsername = prefs.getString("username", "");
        usernameEditText.setText(currentUsername);

        saveUsernameButton.setOnClickListener(v -> handleUsernameSave());
        logoutButton.setOnClickListener(v -> handleLogout());
        resetProgressButton.setOnClickListener(v -> handleResetProgress());
    }

    private void handleUsernameSave() {
        String newName = usernameEditText.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        prefs.edit().putString("username", newName).apply();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("displayName").setValue(newName)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update name in Firebase", Toast.LENGTH_SHORT).show());
    }

    private void handleLogout() {
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
        finish();
    }

    private void handleResetProgress() {
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Cannot reset progress.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("tests")
                .removeValue()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Progress reset successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to reset progress", Toast.LENGTH_SHORT).show());
    }
}
