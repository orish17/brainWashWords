package com.example.brainwashwords; // הגדרת החבילה של המחלקה

import android.content.Intent; // מאפשר פתיחת מסכים אחרים
import android.os.Bundle; // מחזיק מידע שמועבר בין מסכים
import android.view.View; // משמש לאירועים על רכיבים במסך
import android.widget.Button; // רכיב של כפתור במסך

import androidx.activity.EdgeToEdge; // מאפשר עיצוב במסך מלא (ללא שוליים)
import androidx.appcompat.app.AppCompatActivity; // בסיס למסכים עם תמיכה בעיצוב מודרני

/**
 * MainActivity - מסך פתיחה של האפליקציה.
 * מציג שני כפתורים:
 * 1. התחברות לחשבון קיים
 * 2. הרשמה לחשבון חדש
 *
 * לחיצה על כפתור מפנה למסך המתאים (Login / Signup).
 */
public class MainActivity extends AppCompatActivity {

    Button button1, button2; // הגדרת משתנים עבור שני הכפתורים: התחברות והרשמה

    /**
     * נקראת כאשר המסך נוצר – כאן מתחילים את כל הפעולות הראשוניות.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // קריאה לפונקציית onCreate של המחלקה שממנה ירשנו

        EdgeToEdge.enable(this); // הפעלת תצוגת "edge to edge" – מסך ללא שוליים

        setContentView(R.layout.activity_main); // קביעת קובץ ה-XML שמצייר את המסך

        // קישור בין משתני הכפתורים בקוד לבין הכפתורים בקובץ ה-XML
        button1 = findViewById(R.id.button);   // קישור לכפתור Login
        button2 = findViewById(R.id.button2);  // קישור לכפתור Signup

        // מאזין ללחיצה על כפתור ההתחברות
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת כוונה (Intent) לעבור למסך login
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent); // התחלת המסך החדש
            }
        });

        // מאזין ללחיצה על כפתור ההרשמה
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // יצירת כוונה (Intent) לעבור למסך signup
                Intent intent = new Intent(MainActivity.this, signup.class);
                startActivity(intent); // התחלת המסך החדש
            }
        });
    }
}
