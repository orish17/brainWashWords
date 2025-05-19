
package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        setContentView(R.layout.activity_login);

        button5 = findViewById(R.id.button5);
        Name = findViewById(R.id.editTextTextPersonName);
        imageButton = findViewById(R.id.imageButton);
        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        button5.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, signup.class);
            startActivity(intent);
        });

        imageButton.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String name = Name.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = usersRef.push().getKey();
        User newUser = new User(email, password, name);

        if (userId != null) {
            usersRef.child(userId).setValue(newUser).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit().putString("uid", userId).apply();
                    Toast.makeText(login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(login.this, home.class);
                    intent.putExtra("USERNAME", name);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(login.this, "Failed to sign up", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}


