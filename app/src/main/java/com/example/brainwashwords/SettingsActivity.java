package com.example.brainwashwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // ✅ חייב להיות ראשון

        prefs = getSharedPreferences("AppSettings", MODE_PRIVATE); // או השם שלך

        EditText usernameEditText = findViewById(R.id.edit_username);
        Button saveUsernameButton = findViewById(R.id.btn_save_username);

        usernameEditText.setText(prefs.getString("username", ""));

        saveUsernameButton.setOnClickListener(v -> {
            String newName = usernameEditText.getText().toString().trim();
            if (!newName.isEmpty()) {
                prefs.edit().putString("username", newName).apply();
                Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
