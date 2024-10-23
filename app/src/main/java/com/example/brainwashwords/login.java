package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class login extends AppCompatActivity {
    ImageButton imageButton;
    Button button5;
    EditText Email, Password, Name;
    private DatabaseReference usersRef;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize views first
        initializeViews();

        // Set up click listeners after views are initialized
        setupClickListeners();

        FirebaseDatabase orishDatabase = FirebaseDatabase.getInstance();
        usersRef = orishDatabase.getReference("users");

        button5.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(login.this, signup.class);
                startActivity(intent);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                Intent intent = new Intent(login.this, home.class);
                startActivity(intent);
            }
        });

        // Initialize views
        initializeViews();
        // Set up click listeners
        setupClickListeners();
    }


    private void initializeViews() {
        button5 = findViewById(R.id.button5);
        Name = findViewById(R.id.editTextTextPersonName);
        imageButton = findViewById(R.id.imageButton);
        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);
    }
    private void setupClickListeners() {
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, signup.class);
                startActivity(intent);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }


    private void signUpUser() {

        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String name = Name.getText().toString().trim();

        if (validateInput(name,email,password)) {
            User newUser = new User(email, password, name);
            String userId = usersRef.push().getKey();

            if (userId != null) {
                usersRef.child(userId).setValue(newUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Save username to SharedPreferences
                                    saveUsername(name);
                                    Toast.makeText(login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    clearInputs();

                                    // Create intent and pass username as a string
                                    Intent intent = new Intent(login.this, home.class);
                                    intent.putExtra("USERNAME", name);  // Pass the string, not the EditText
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(login.this, "Failed to sign up: " +
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

    private boolean validateInput(String name, String email, String password) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return false;
        }


        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Wrong Email,try again!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Wrong Password,try again!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void clearInputs() {
        Name.setText("");
        Email.setText("");
        Password.setText("");
    }


}


