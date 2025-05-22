package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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



        btnMultipleChoice = findViewById(R.id.btnMultipleChoice);
        btnFillInBlank = findViewById(R.id.btnFillInBlank);
        btnAudioTest = findViewById(R.id.btnAudioTest);
        btnAiQuiz = findViewById(R.id.btnAiQuiz);
        btnStt = findViewById(R.id.btnStt);
        setupDrawer();

        db = FirebaseFirestore.getInstance();


        View.OnClickListener protectedClickListener = view -> {
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

                            if (view.getId() == R.id.btnMultipleChoice)
                                activityClass = MultipleChoiceActivity.class;
                            else if (view.getId() == R.id.btnFillInBlank)
                                activityClass = FillInBlankActivity.class;
                            else if (view.getId() == R.id.btnAudioTest)
                                activityClass = AudioTestActivity.class;
                            else if (view.getId() == R.id.btnAiQuiz)
                                activityClass = AiQuizActivity.class;
                            else if (view.getId() == R.id.btnStt)
                                activityClass = SpeechToTextTestActivity.class;

                            if (activityClass != null)
                                startActivity(new Intent(this, activityClass));
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error checking known words!", Toast.LENGTH_SHORT).show()
                    );
        };

        // הגדרת מאזין לכל הכפתורים
        btnMultipleChoice.setOnClickListener(protectedClickListener);
        btnFillInBlank.setOnClickListener(protectedClickListener);
        btnAudioTest.setOnClickListener(protectedClickListener);
        btnAiQuiz.setOnClickListener(protectedClickListener);
        btnStt.setOnClickListener(protectedClickListener);

    }

    private void checkKnownWords() {
        db.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(result -> {
                    knownWordsCount = 0;
                    for (QueryDocumentSnapshot doc : result) {
                        Boolean known = doc.getBoolean("known");
                        if (Boolean.TRUE.equals(known)) {
                            knownWordsCount++;
                        }
                    }
                });
    }
}
