package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * SettingsActivity handles user preferences and actions including:
 * - Updating display name
 * - Toggling dark mode
 * - Resetting progress
 * - Logging out
 *
 * The activity uses SharedPreferences and Firebase Realtime Database
 * to store and retrieve user-specific data.
 */
public class SettingsActivity extends BaseActivity {

    private EditText usernameEditText;
    private Button saveUsernameButton, logoutButton, resetProgressButton;
    private Switch darkModeSwitch;
    private SharedPreferences prefs;
    private String userId;

    /**
     * Called when the activity is starting.
     * Sets up UI elements and loads saved preferences.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // הפעלת מצב תאורה (נושא) שנשמר
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupDrawer(); // תפריט צד

        // גישה ל־SharedPreferences תחת קובץ בשם "settings"
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        userId = prefs.getString("uid", null); // שליפת מזהה המשתמש

        // קישור רכיבי ממשק משתמש
        usernameEditText = findViewById(R.id.edit_username);
        saveUsernameButton = findViewById(R.id.btn_save_username);
        logoutButton = findViewById(R.id.btn_logout);
        resetProgressButton = findViewById(R.id.btn_reset_stats);
        darkModeSwitch = findViewById(R.id.switch_dark_mode);

        // טעינת שם המשתמש מההעדפות המקומיות
        String currentUsername = prefs.getString("username", "");
        usernameEditText.setText(currentUsername);

        // הגדרת מצב כהה אם שמור כ־true
        boolean isDarkMode = prefs.getBoolean("dark_mode", true);
        darkModeSwitch.setChecked(isDarkMode);

        // כאשר המשתמש מחליף מצב תאורה
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.setNightMode(this, isChecked); // שמירה + החלפת מצב
            recreate(); // רענון המסך
        });

        // טיפול בלחיצות
        saveUsernameButton.setOnClickListener(v -> handleUsernameSave());
        logoutButton.setOnClickListener(v -> handleLogout());
        resetProgressButton.setOnClickListener(v -> handleResetProgress());
    }

    /**
     * Handles saving the new display name:
     * - Validates input
     * - Saves locally to SharedPreferences
     * - Updates Firebase database
     */
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

        // שמירת השם החדש בהעדפות מקומיות
        prefs.edit().putString("username", newName).apply();

        // עדכון השם במסד הנתונים
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("displayName").setValue(newName)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update name in Firebase", Toast.LENGTH_SHORT).show());
    }

    /**
     * Logs out the user by clearing all saved preferences,
     * then returns to the login screen.
     */
    private void handleLogout() {
        prefs.edit().clear().apply(); // איפוס ההעדפות
        startActivity(new Intent(this, login.class)); // מעבר למסך התחברות
        finish(); // סגירת המסך הנוכחי
    }

    /**
     * Resets the user's test progress by deleting the "tests" node
     * under the current user's data in Firebase.
     */
    private void handleResetProgress() {
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Cannot reset progress.", Toast.LENGTH_SHORT).show();
            return;
        }

        // הסרת ציון המבחנים מ־Firebase
        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("tests")
                .removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(this, "Progress reset successfully", Toast.LENGTH_SHORT).show())//
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reset progress", Toast.LENGTH_SHORT).show());
    }
}
