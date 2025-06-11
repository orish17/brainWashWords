package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Signup Activity – מאפשר למשתמש להירשם עם שם, אימייל וסיסמה.
 * הנתונים נשמרים ב־Firebase Realtime Database, ולאחר ההרשמה המשתמש מועבר למסך הבית.
 */
public class signup extends AppCompatActivity {

    ImageButton imageButton; // כפתור הרשמה
    EditText Name, Email, Password, RePassword; // שדות קלט מהמשתמש
    Button button5; // כפתור מעבר למסך התחברות
    private DatabaseReference usersRef; // רפרנס לטבלת users ב־Firebase

    /**
     * onCreate – מופעל כשנוצר המסך. מאתחל רכיבי UI ומאזינים.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // התחברות ל־Realtime Database של Firebase
        FirebaseDatabase orishDatabase = FirebaseDatabase.getInstance();
        usersRef = orishDatabase.getReference("users");

        // קישור רכיבי UI מה-XML
        imageButton = findViewById(R.id.imageButton);
        Name = findViewById(R.id.editTextTextPersonName);
        Email = findViewById(R.id.editTextTextPersonName2);
        Password = findViewById(R.id.editTextTextPassword);
        RePassword = findViewById(R.id.editTextTextPassword2);
        button5 = findViewById(R.id.button5);

        // כפתור הרשמה – מפעיל את המתודה signUpUser
        imageButton.setOnClickListener(v -> signUpUser());

        // כפתור מעבר חזרה למסך התחברות
        button5.setOnClickListener(v -> {
            Intent intent = new Intent(signup.this, login.class);
            startActivity(intent);
            finish(); // מסיים את המסך הנוכחי – שלא יהיה חזור (Back)
        });
    }

    /**
     * signUpUser – אחראי על תהליך ההרשמה: ולידציה, שמירה ל־Firebase, מעבר למסך הבית.
     */
    private void signUpUser() {
        String name = Name.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String rePassword = RePassword.getText().toString().trim();

        // בדיקת תקינות של הקלט
        if (!validateInput(name, email, password, rePassword)) return;

        // יצירת מפתח ייחודי חדש למשתמש
        String userId = usersRef.push().getKey();
        if (userId == null) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            return;
        }

        // יצירת אובייקט משתמש חדש
        User newUser = new User(name, email, password);

        // שמירת המשתמש ב־Realtime Database
        usersRef.child(userId).setValue(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // איפוס מילים מסומנות – רק ליתר ביטחון
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("knownWords")
                        .removeValue();

                // שמירת מזהה המשתמש ופרטים בזיכרון המקומי
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("uid", userId)
                        .putString("username", name)
                        .apply();

                Toast.makeText(signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();

                // מעבר למסך הבית עם העברת השם
                Intent intent = new Intent(signup.this, home.class);
                intent.putExtra("USERNAME", name);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Signup failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * validateInput – מבצעת בדיקות על קלט המשתמש לפני שליחה ל־Firebase.
     *
     * @return true אם הכל תקין, אחרת false.
     */
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
