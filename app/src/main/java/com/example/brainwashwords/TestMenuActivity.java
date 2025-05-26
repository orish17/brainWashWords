package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TestMenuActivity extends BaseActivity {

    Button btnMultipleChoice, btnFillInBlank, btnAudioTest, btnAiQuiz, btnStt;
    private FirebaseFirestore db;
    private int knownWordsCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_menu);
        setupDrawer();

        btnMultipleChoice = findViewById(R.id.btnMultipleChoice);
        btnFillInBlank = findViewById(R.id.btnFillInBlank);
        btnAudioTest = findViewById(R.id.btnAudioTest);
        btnAiQuiz = findViewById(R.id.btnAiQuiz);
        btnStt = findViewById(R.id.btnStt);

        db = FirebaseFirestore.getInstance();

        View.OnClickListener protectedClickListener = view -> {
            // אפקט לחיצה מגניב
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_click_scale);
            view.startAnimation(anim);

            // בדיקת מילים מסומנות כ-known
            db.collection("groups").document("workout1").collection("words")
                    .get()
                    .addOnSuccessListener(result -> {
                        int count = 0;
                        for (QueryDocumentSnapshot doc : result) {
                            Boolean known = doc.getBoolean("known");
                            if (Boolean.TRUE.equals(known)) {
                                count++;
                            }
                        }

                        if (count < 4) {
                            Toast.makeText(this, "You must mark at least 4 known words before taking a test!", Toast.LENGTH_LONG).show();
                        } else {
                            Class<?> activityClass = null;

                            int viewId = view.getId();
                            if (viewId == R.id.btnMultipleChoice)
                                activityClass = MultipleChoiceActivity.class;
                            else if (viewId == R.id.btnFillInBlank)
                                activityClass = FillInBlankActivity.class;
                            else if (viewId == R.id.btnAudioTest)
                                activityClass = AudioTestActivity.class;
                            else if (viewId == R.id.btnAiQuiz)
                                activityClass = AiQuizActivity.class;
                            else if (viewId == R.id.btnStt)
                                activityClass = SpeechToTextTestActivity.class;

                            if (activityClass != null) {
                                startActivity(new Intent(this, activityClass));
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error checking known words!", Toast.LENGTH_SHORT).show()
                    );
        };

        // מאזינים לכל כפתור
        btnMultipleChoice.setOnClickListener(protectedClickListener);
        btnFillInBlank.setOnClickListener(protectedClickListener);
        btnAudioTest.setOnClickListener(protectedClickListener);
        btnAiQuiz.setOnClickListener(protectedClickListener);
        btnStt.setOnClickListener(protectedClickListener);
    }
    }