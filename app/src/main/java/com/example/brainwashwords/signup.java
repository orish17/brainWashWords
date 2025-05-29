package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {
    ImageButton imageButton;
    EditText Name, Email, Password, RePassword;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        FirebaseDatabase orishDatabase = FirebaseDatabase.getInstance();
        usersRef = orishDatabase.getReference("users");

        imageButton = findViewById(R.id.imageButton);
        Name = findViewById(R.id.editTextTextPersonName);
        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);
        RePassword = findViewById(R.id.editTextTextPassword2);

        imageButton.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String name = Name.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String rePassword = RePassword.getText().toString().trim();

        if (!validateInput(name, email, password, rePassword)) return;

        String userId = usersRef.push().getKey();
        if (userId == null) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(name, email, password);

        usersRef.child(userId).setValue(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                resetWordsForUser(userId, name);
            } else {
                Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetWordsForUser(String userId, String userName) {
        DatabaseReference wordsRef = usersRef.child(userId).child("words");

        // הכנת מילים התחלתיות
        Map<String, Word> initialWords = new HashMap<>();
        initialWords.put("apple", new Word("apple", false, "A fruit", "apple", "group1"));
        initialWords.put("run", new Word("run", false, "To move fast", "run", "group1"));
        initialWords.put("book", new Word("book", false, "Something to read", "book", "group1"));

        // מחיקת מילים קיימות אם יש, ואז הוספה מחדש
        wordsRef.removeValue().addOnCompleteListener(clearTask -> {
            wordsRef.setValue(initialWords).addOnCompleteListener(wordsTask -> {
                if (wordsTask.isSuccessful()) {
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("uid", userId)
                            .putString("username", userName)
                            .apply();

                    Toast.makeText(signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(signup.this, home.class);
                    intent.putExtra("USERNAME", userName);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "User saved but failed to set words", Toast.LENGTH_SHORT).show();
                }
            });
        });
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
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
