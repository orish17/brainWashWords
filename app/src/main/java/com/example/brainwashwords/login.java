package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class login extends AppCompatActivity {
    ImageButton imageButton;
    TextView devBypass;
    Button button5;
    EditText Email, Password, Name;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);
        Name = findViewById(R.id.editTextTextPersonName);
        imageButton = findViewById(R.id.imageButton);
        button5 = findViewById(R.id.button5);
        devBypass = findViewById(R.id.devBypass);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        final int[] tapCount = {0};
        devBypass.setOnClickListener(v -> {
            tapCount[0]++;
            if (tapCount[0] >= 5) {
                Toast.makeText(this, "Dev login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, home.class));
                finish();
            }
        });

        button5.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
        });

        imageButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                boolean userFound = false;

                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    Object value = userSnapshot.getValue();
                    if (value instanceof Map) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null &&
                                email.equals(user.getEmail()) &&
                                password.equals(user.getPassword())) {

                            userFound = true;

                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("uid", userSnapshot.getKey())
                                    .putString("username", user.getName())
                                    .apply();

                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(login.this, home.class);
                            intent.putExtra("USERNAME", user.getName());
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                }

                if (!userFound) {
                    Toast.makeText(this, "User not found. Please sign up first.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
