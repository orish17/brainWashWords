package com.example.brainwashwords; // הצהרה על המיקום של המחלקה בתוך הפרויקט

import android.content.Intent; // מאפשר לעבור בין מסכים
import android.os.Bundle; // מכיל מידע על המצב הקודם של המסך
import android.widget.Button; // כפתור
import android.widget.TextView; // תצוגת טקסט

import androidx.appcompat.app.AppCompatActivity; // פעילות עם תמיכה בעיצוב מודרני (AppCompat)

/**
 * QuizResultActivity - מסך שמוצג לאחר סיום מבחן.
 * מציג את הציון, הודעת עידוד, כפתור לנסות שוב וכפתור לחזרה לתפריט.
 */
public class QuizResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) { // פונקציה שפועלת כשנכנסים למסך
        super.onCreate(savedInstanceState); // קריאה לפונקציית האבא
        setContentView(R.layout.activity_quiz_result); // קישור לקובץ העיצוב XML

        TextView resultText = findViewById(R.id.resultText); // תצוגת טקסט לתוצאה
        Button tryAgainBtn = findViewById(R.id.btnTryAgain); // כפתור לנסות שוב
        Button backToMenuBtn = findViewById(R.id.btnBackToMenu); // כפתור חזרה לתפריט

        // קבלת נתוני תוצאה מהמבחן הקודם
        int score = getIntent().getIntExtra("score", 0); // ניקוד שהשיג המשתמש
        int total = getIntent().getIntExtra("total", 0); // סך השאלות במבחן
        int percentage = (int) ((score * 100.0f) / total); // חישוב אחוז הצלחה

        // בניית הודעה להצגה
        String message = "You got " + score + " out of " + total + "\nScore: " + percentage + "%";

        // הוספת הודעת עידוד בהתאם לציון
        if (percentage >= 90) {
            message += "\n🔥 Amazing!";
        } else if (percentage >= 60) {
            message += "\n👍 Good job!";
        } else {
            message += "\n😅 Keep practicing!";
        }

        resultText.setText(message); // הצגת ההודעה על המסך

        // קבלת סוג המבחן שחזרנו ממנו, כדי לדעת לאן לחזור אם לוחצים "Try Again"
        String examType = getIntent().getStringExtra("examType");

        // מאזין ללחיצה על כפתור "Try Again"
        tryAgainBtn.setOnClickListener(v -> {
            Class<?> activityClass = MultipleChoiceActivity.class; // ברירת מחדל: מבחן רב-ברירה

            // קביעת מחלקת המבחן לפי סוג
            if ("FillInBlank".equals(examType)) {
                activityClass = FillInBlankActivity.class;
            } else if ("AudioTest".equals(examType)) {
                activityClass = AudioTestActivity.class;
            } else if ("SpeechToText".equals(examType)) {
                activityClass = SpeechToTextTestActivity.class;
            }

            // הפעלת המבחן מחדש
            startActivity(new Intent(this, activityClass));
            finish(); // סיום המסך הנוכחי
        });

        // מאזין ללחיצה על כפתור "Back to Menu"
        backToMenuBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, TestMenuActivity.class)); // חזרה למסך תפריט מבחנים
            finish(); // סגירת המסך הנוכחי
        });
    }
}
