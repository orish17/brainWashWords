package com.example.brainwashwords;

// signup.java
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class signup extends AppCompatActivity {
    ImageButton imageButton, home;
    EditText Name, Email, Password, RePassword;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Database
        FirebaseDatabase orishDatabase = FirebaseDatabase.getInstance();
        usersRef = orishDatabase.getReference("users");

        // Initialize views
        initializeViews();
        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        imageButton = findViewById(R.id.imageButton);
        Name = findViewById(R.id.editTextTextPersonName);
        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);
        RePassword = findViewById(R.id.editTextTextPassword2);
    }

    private void setupClickListeners() {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

    }

    private void signUpUser() {
        String name = Name.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String rePassword = RePassword.getText().toString().trim();

        if (validateInput(name, email, password, rePassword)) {
            User newUser = new User(name, email, password);
            String userId = usersRef.push().getKey();

            if (userId != null) {
                usersRef.child(userId).setValue(newUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Save username to SharedPreferences
                                    saveUsername(name);
                                    Toast.makeText(signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                    clearInputs();

                                    // Create intent and pass username as a string
                                    Intent intent = new Intent(signup.this, home.class);
                                    intent.putExtra("USERNAME", name);  // Pass the string, not the EditText
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(signup.this, "Failed to sign up: " +
                                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }




    private void saveUsername(String username) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("username", username);
        editor.apply();
    }


    private boolean validateInput(String name, String email, String password, String rePassword) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void clearInputs() {
        Name.setText("");
        Email.setText("");
        Password.setText("");
        RePassword.setText("");
    }

}