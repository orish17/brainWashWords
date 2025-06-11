package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class QuizResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        TextView resultText = findViewById(R.id.resultText);
        Button tryAgainBtn = findViewById(R.id.btnTryAgain);
        Button backToMenuBtn = findViewById(R.id.btnBackToMenu);

        // קבלת הנתונים מהמבחן
        int score = getIntent().getIntExtra("score", 0);
        int total = getIntent().getIntExtra("total", 0);
        int percentage = (int) ((score * 100.0f) / total);

        String message = "You got " + score + " out of " + total + "\nScore: " + percentage + "%";

        if (percentage >= 90) {
            message += "\n🔥 Amazing!";
        } else if (percentage >= 60) {
            message += "\n👍 Good job!";
        } else {
            message += "\n😅 Keep practicing!";
        }

        resultText.setText(message);

        // זיהוי סוג מבחן לצורך חזרה
        String examType = getIntent().getStringExtra("examType");

        tryAgainBtn.setOnClickListener(v -> {
            Class<?> activityClass = MultipleChoiceActivity.class; // ברירת מחדל

            if ("FillInBlank".equals(examType)) {
                activityClass = FillInBlankActivity.class;
            } else if ("AudioTest".equals(examType)) {
                activityClass = AudioTestActivity.class;
            } else if ("SpeechToText".equals(examType)) {
                activityClass = SpeechToTextTestActivity.class;
            }

            startActivity(new Intent(this, activityClass));
            finish();
        });

        backToMenuBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, TestMenuActivity.class));
            finish();
        });
    }
}
