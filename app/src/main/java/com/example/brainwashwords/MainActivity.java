package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity - מסך פתיחה של האפליקציה.
 * מציג שני כפתורים:
 * 1. התחברות לחשבון קיים
 * 2. הרשמה לחשבון חדש
 *
 * לחיצה על כפתור מפנה למסך המתאים (Login / Signup).
 */
public class MainActivity extends AppCompatActivity {

    // הגדרת שני הכפתורים במסך
    Button button1, button2;

    /**
     * מתבצע בעת יצירת המסך (onCreate).
     * קובע את עיצוב המסך, מקשר את הכפתורים, ומאזין ללחיצות.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // הפעלת עיצוב Edge-to-Edge (שוליים נסתרים)
        EdgeToEdge.enable(this);

        // קביעת קובץ ה-XML של המסך
        setContentView(R.layout.activity_main);

        // קישור בין רכיבי ה-XML למשתנים בקוד
        button1 = findViewById(R.id.button);   // כפתור Login
        button2 = findViewById(R.id.button2);  // כפתור Signup

        // מאזין ללחיצה על כפתור Login
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר למסך login
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
            }
        });

        // מאזין ללחיצה על כפתור Signup
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // מעבר למסך signup
                Intent intent = new Intent(MainActivity.this, signup.class);
                startActivity(intent);
            }
        });
    }
}
