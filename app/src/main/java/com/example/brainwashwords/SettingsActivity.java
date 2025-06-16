package com.example.brainwashwords; // חבילת הפרויקט

import android.content.Intent; // מאפשר מעבר בין מסכים
import android.content.SharedPreferences; // לשמירת נתונים מקומיים של המשתמש
import android.os.Bundle; // מכיל מידע על מצב קודם של המסך
import android.widget.Button; // כפתור
import android.widget.EditText; // שדה טקסט להזנה
import android.widget.Switch; // מתג (למשל מצב כהה)
import android.widget.Toast; // הודעות קצרות למשתמש

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference; // רפרנס למסד נתונים Firebase
import com.google.firebase.database.FirebaseDatabase; // גישה למסד Firebase

/**
 * SettingsActivity - מסך הגדרות:
 * שינוי שם משתמש, מעבר למצב כהה/בהיר, איפוס התקדמות, התנתקות.
 * משתמש ב־SharedPreferences וב־Firebase.
 */
public class SettingsActivity extends BaseActivity {

    private EditText usernameEditText; // שדה להזנת שם המשתמש
    private Button saveUsernameButton, logoutButton, resetProgressButton; // כפתורים לפעולות השונות
    private Switch darkModeSwitch; // מתג מצב כהה/בהיר
    private SharedPreferences prefs; // אובייקט לשמירת נתונים מקומיים
    private String userId; // מזהה המשתמש המחובר

    /**
     * הפונקציה הראשית שמופעלת כשהמסך נוצר.
     * מגדירה את הממשק ומבצעת טעינת ערכים מהזיכרון המקומי.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // החלת נושא (theme) שמור
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // קישור לקובץ העיצוב
        setupDrawer(); // תפריט צד

        prefs = getSharedPreferences("settings", MODE_PRIVATE); // טעינת ההגדרות המקומיות
        userId = prefs.getString("uid", null); // שליפת מזהה המשתמש מההגדרות

        // קישור רכיבי ממשק UI
        usernameEditText = findViewById(R.id.edit_username); // שדה להזנת שם משתמש
        saveUsernameButton = findViewById(R.id.btn_save_username); // כפתור שמירה
        logoutButton = findViewById(R.id.btn_logout); // כפתור התנתקות
        resetProgressButton = findViewById(R.id.btn_reset_stats); // כפתור איפוס התקדמות
        darkModeSwitch = findViewById(R.id.switch_dark_mode); // מתג מצב כהה

        String currentUsername = prefs.getString("username", ""); // שם משתמש נוכחי מהעדפות
        usernameEditText.setText(currentUsername); // הצגת השם בטופס

        boolean isDarkMode = prefs.getBoolean("dark_mode", true); // טעינת מצב כהה
        darkModeSwitch.setChecked(isDarkMode); // הפעלת המתג בהתאם

        // כשהמשתמש משנה את המצב (כהה/בהיר)
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.setNightMode(this, isChecked); // שמירה והחלת מצב
            recreate(); // רענון המסך
        });

        // מאזינים לפעולות המשתמש
        saveUsernameButton.setOnClickListener(v -> handleUsernameSave()); // שמירת שם חדש
        logoutButton.setOnClickListener(v -> handleLogout()); // התנתקות
        resetProgressButton.setOnClickListener(v -> handleResetProgress()); // איפוס התקדמות
    }

    /**
     * פונקציה לשמירת שם חדש:
     * - בודקת אם השם חוקי
     * - שומרת בזיכרון מקומי
     * - מעדכנת ב-Firebase
     */
    private void handleUsernameSave() {
        String newName = usernameEditText.getText().toString().trim(); // קריאת שם חדש מהשדה

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter a valid name", Toast.LENGTH_SHORT).show(); // הודעת שגיאה אם ריק
            return;
        }

        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show(); // אם אין משתמש
            return;
        }

        prefs.edit().putString("username", newName).apply(); // שמירת שם חדש מקומית

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId); // רפרנס למשתמש
        userRef.child("displayName").setValue(newName) // עדכון ב־Firebase
                .addOnSuccessListener(unused -> Toast.makeText(this, "Username updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update name in Firebase", Toast.LENGTH_SHORT).show());
    }

    /**
     * התנתקות מהחשבון:
     * - מוחקת את כל ההעדפות המקומיות
     * - חוזרת למסך login
     */
    private void handleLogout() {
        prefs.edit().clear().apply(); // איפוס ההעדפות
        startActivity(new Intent(this, login.class)); // מעבר למסך התחברות
        finish(); // סיום המסך
    }

    /**
     * איפוס התקדמות מבחנים:
     * - מוחק את הציונים מה־Firebase
     */
    private void handleResetProgress() {
        if (userId == null) {
            Toast.makeText(this, "User ID not found. Cannot reset progress.", Toast.LENGTH_SHORT).show(); // לא נמצא משתמש
            return;
        }

        FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("tests")
                .removeValue() // מחיקת התקדמות
                .addOnSuccessListener(unused -> Toast.makeText(this, "Progress reset successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reset progress", Toast.LENGTH_SHORT).show());
    }
}
