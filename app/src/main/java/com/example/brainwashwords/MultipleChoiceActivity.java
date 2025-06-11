package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;

import com.google.firebase.firestore.*;

import java.util.*;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MultipleChoiceActivity – פעילות מבחן רב-ברירה.
 * המשתמש בוחר את הפירוש הנכון של מילה באנגלית מתוך 4 אפשרויות.
 * כולל תמיכה במצב מבחן עם טיימר של 7 שניות, ניקוד, ושמירה ל־Firebase.
 */
public class MultipleChoiceActivity extends BaseActivity {

    private TextView questionText, timerText;
    private Button[] optionButtons = new Button[4]; // 4 אפשרויות תשובה
    private Switch modeSwitch;

    private FirebaseFirestore db;
    private List<Word> wordList = new ArrayList<>();
    private Word currentWord;
    private String correctAnswer;

    private int score = 0;
    private int totalQuestions = 0;
    private static final int MAX_QUESTIONS = 10;
    private boolean isTestMode = false;
    private CountDownTimer countDownTimer;

    /**
     * מופעל בעת פתיחת הפעילות – קושר רכיבי UI ומעלה מילים מה־Firebase.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice);

        // קישור רכיבים מה-XML
        modeSwitch = findViewById(R.id.modeSwitch);
        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        optionButtons[0] = findViewById(R.id.optionA);
        optionButtons[1] = findViewById(R.id.optionB);
        optionButtons[2] = findViewById(R.id.optionC);
        optionButtons[3] = findViewById(R.id.optionD);
        setupDrawer();

        db = FirebaseFirestore.getInstance();

        // מעבר בין מצב מבחן לתרגול
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE);
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        // שליפת מילים שסומנו כ-known
        loadWordsFromFirebase();

        // מאזין לכל אחד מהכפתורים
        for (Button button : optionButtons) {
            button.setOnClickListener(this::checkAnswer);
        }
    }

    /**
     * טוען את המילים מתוך קבוצת "workout1" שסומנו כ־known.
     */
    private void loadWordsFromFirebase() {
        db.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(result -> {
                    wordList.clear();

                    for (QueryDocumentSnapshot doc : result) {
                        String wordText = doc.getString("word");
                        String definition = doc.getString("definition");
                        Boolean known = doc.getBoolean("known");

                        if (wordText != null && definition != null && Boolean.TRUE.equals(known)) {
                            wordList.add(new Word(wordText, true, definition, doc.getId(), "workout1"));
                        }
                    }

                    if (wordList.size() < 4) {
                        Toast.makeText(this, "אתה צריך למיין לפחות 4 מילים!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        showNextQuestion();
                    }
                });
    }

    /**
     * מציג שאלה חדשה – מילה עם 4 פירושים (אחד נכון, 3 שגויים).
     */
    private void showNextQuestion() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setVisibility(View.GONE);

        if (wordList.size() < 4) {
            Toast.makeText(this, "לא מספיק מילים למבחן!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<Word> shuffled = new ArrayList<>(wordList);
        Collections.shuffle(shuffled);

        currentWord = shuffled.get(0); // המילה לשאלה הזו
        correctAnswer = currentWord.getDefinition();

        // יצירת רשימת אפשרויות
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        options.add(shuffled.get(1).getDefinition());
        options.add(shuffled.get(2).getDefinition());
        options.add(shuffled.get(3).getDefinition());
        Collections.shuffle(options);

        questionText.setText("מה הפירוש של: " + currentWord.getWord());

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options.get(i));
            optionButtons[i].setEnabled(true);
        }

        if (isTestMode) startTimer();
    }

    /**
     * מפעיל טיימר של 7 שניות במצב מבחן.
     */
    private void startTimer() {
        timerText.setVisibility(View.VISIBLE);
        timerText.setText("Time left: 7");

        countDownTimer = new CountDownTimer(7000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerText.setText("Time's up!");
                totalQuestions++;
                Toast.makeText(MultipleChoiceActivity.this,
                        "⏱️ Time's up! The correct answer was: " + correctAnswer,
                        Toast.LENGTH_SHORT).show();

                if (totalQuestions >= MAX_QUESTIONS) {
                    showResult();
                } else {
                    showNextQuestion();
                }
            }
        }.start();
    }

    /**
     * בודק האם המשתמש בחר בתשובה הנכונה.
     */
    private void checkAnswer(View v) {
        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setVisibility(View.GONE);

        Button clicked = (Button) v;
        String answer = clicked.getText().toString();
        totalQuestions++;

        if (answer.equals(correctAnswer)) {
            score++;
            Toast.makeText(this, "✔️ Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Incorrect! The correct answer was: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }

        // מניעת לחיצה חוזרת
        for (Button b : optionButtons) {
            b.setEnabled(false);
        }

        // שאלה הבאה תוך שנייה
        v.postDelayed(() -> {
            if (totalQuestions >= MAX_QUESTIONS) {
                showResult();
            } else {
                showNextQuestion();
            }
        }, 1000);
    }

    /**
     * מציג את התוצאה, שומר ל־Firebase, ומעביר למסך סיכום.
     */
    private void showResult() {
        float successRate = ((float) score / totalQuestions) * 100f;
        FirebaseUtils.saveTestResult(this, "MultipleChoice", successRate);

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total", totalQuestions);
        startActivity(intent);
        finish();
    }

    /**
     * ביטול הטיימר במעבר בין מסכים.
     */
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
