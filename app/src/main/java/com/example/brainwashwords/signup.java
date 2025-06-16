package com.example.brainwashwords; // מגדיר את שם החבילה באפליקציה

import android.content.Intent; // מאפשר מעבר בין מסכים
import android.content.SharedPreferences; // מאפשר שמירת מידע מקומי באפליקציה
import android.os.Bundle; // משמש לשמירת מצב activity
import android.widget.*; // כולל רכיבי ממשק: טקסט, שדות קלט, כפתורים

import androidx.annotation.NonNull; // תכונה לזיהוי ערכים שאינם null
import androidx.appcompat.app.AppCompatActivity; // מחלקת בסיס לפעילויות עם תמיכה בעיצוב מודרני

import com.google.firebase.auth.FirebaseAuth; // Firebase – ניהול הרשאות
import com.google.firebase.auth.FirebaseUser; // אובייקט משתמש מ-Firebase
import com.google.firebase.auth.UserProfileChangeRequest; // שינוי שם תצוגה
import com.google.firebase.database.FirebaseDatabase; // מסד נתונים Realtime של Firebase

public class signup extends AppCompatActivity { // הפעילות של מסך ההרשמה

    private EditText Name, Email, Password, RePassword; // שדות קלט: שם, אימייל, סיסמה ואישור
    private ImageButton registerButton; // כפתור להרשמה
    private Button loginRedirectButton; // כפתור למעבר למסך התחברות
    private FirebaseAuth mAuth; // מנגנון Firebase לאימות

    @Override
    protected void onCreate(Bundle savedInstanceState) { // מתבצע כשנטען המסך
        super.onCreate(savedInstanceState); // קריאה למחלקת העל
        setContentView(R.layout.activity_signup); // קובע את קובץ העיצוב למסך

        mAuth = FirebaseAuth.getInstance(); // אתחול FirebaseAuth

        Name = findViewById(R.id.editTextTextPersonName); // שדה קלט שם
        Email = findViewById(R.id.editTextTextPersonName2); // שדה קלט אימייל
        Password = findViewById(R.id.editTextTextPassword); // שדה קלט סיסמה
        RePassword = findViewById(R.id.editTextTextPassword2); // שדה קלט אישור סיסמה
        registerButton = findViewById(R.id.imageButton); // כפתור הרשמה
        loginRedirectButton = findViewById(R.id.button5); // כפתור מעבר למסך התחברות

        registerButton.setOnClickListener(v -> registerUser()); // הפעלת מתודת הרשמה בלחיצה

        loginRedirectButton.setOnClickListener(v -> { // בלחיצה על "יש לי חשבון"
            startActivity(new Intent(this, login.class)); // מעבר למסך login
            finish(); // סיום המסך הנוכחי
        });
    }

    private void registerUser() { // פונקציית רישום משתמש
        String name = Name.getText().toString().trim(); // קבלת השם מהקלט
        String email = Email.getText().toString().trim(); // קבלת האימייל מהקלט
        String password = Password.getText().toString().trim(); // קבלת הסיסמה מהקלט
        String rePassword = RePassword.getText().toString().trim(); // קבלת אישור הסיסמה

        if (!validateInput(name, email, password, rePassword)) return; // בדיקת תקינות שדות

        mAuth.createUserWithEmailAndPassword(email, password) // יצירת משתמש חדש ב-Firebase
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // אם ההרשמה הצליחה
                        FirebaseUser user = mAuth.getCurrentUser(); // קבלת המשתמש הנוכחי
                        if (user != null) {
                            String uid = user.getUid(); // מזהה ייחודי של המשתמש

                            // עדכון שם בפרופיל FirebaseAuth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            user.updateProfile(profileUpdates); // שמירת השם

                            // שמירה ב-Firebase Realtime Database
                            User newUser = new User(name, email, password); // יצירת אובייקט משתמש
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(uid)
                                    .setValue(newUser); // שמירה למסד הנתונים

                            // שמירה מקומית
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("uid", uid)
                                    .putString("username", name)
                                    .apply();

                            // שליחת אימייל אימות
                            user.sendEmailVerification(); // שליחת קישור לאימות מייל
                            Toast.makeText(this, "Signup successful! Please verify your email.", Toast.LENGTH_LONG).show();

                            mAuth.signOut(); // ניתוק המשתמש עד לאימות
                            startActivity(new Intent(this, login.class)); // חזרה למסך login
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // שגיאת הרשמה
                    }
                });
    }

    private boolean validateInput(String name, String email, String password, String rePassword) { // פונקציה לבדוק תקינות קלט
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || rePassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show(); // בדיקה שהכול מלא
            return false;
        }
        if (!password.equals(rePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show(); // בדיקה שהסיסמאות תואמות
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show(); // אורך מינימלי לסיסמה
            return false;
        }
        return true; // כל הבדיקות תקינות
    }
}