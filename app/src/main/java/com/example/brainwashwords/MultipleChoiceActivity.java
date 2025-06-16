package com.example.brainwashwords; // הגדרת מיקום המחלקה בפרויקט

import android.content.Intent; // מאפשר לעבור בין מסכים
import android.os.Bundle; // מאפשר להעביר נתונים בעת פתיחת מסך
import android.os.CountDownTimer; // טיימר ספירה לאחור
import android.view.View; // ניהול אירועים על רכיבי UI
import android.widget.*; // כולל TextView, Button, Switch, Toast וכו'

import com.google.firebase.firestore.*; // עבודה עם Firestore של Firebase

import java.util.*; // כולל רשימות, מחלקות shuffle ועוד

import androidx.appcompat.app.AppCompatActivity; // בסיס למסכים מודרניים עם תמיכה בעיצוב

/**
 * MultipleChoiceActivity – פעילות מבחן רב-ברירה.
 * המשתמש בוחר את הפירוש הנכון של מילה באנגלית מתוך 4 אפשרויות.
 * כולל תמיכה במצב מבחן עם טיימר של 7 שניות, ניקוד, ושמירה ל־Firebase.
 */
public class MultipleChoiceActivity extends BaseActivity {

    private TextView questionText, timerText; // תצוגות שאלה וטיימר
    private Button[] optionButtons = new Button[4]; // מערך של 4 כפתורי תשובות
    private Switch modeSwitch; // מתג מעבר בין מצב תרגול למבחן

    private FirebaseFirestore db; // מסד הנתונים של Firestore
    private List<Word> wordList = new ArrayList<>(); // רשימת מילים (רק כאלו שסומנו כ־known)
    private Word currentWord; // המילה הנוכחית
    private String correctAnswer; // הפירוש הנכון למילה הנוכחית

    private int score = 0; // ניקוד נוכחי
    private int totalQuestions = 0; // סך כל השאלות שנענו
    private static final int MAX_QUESTIONS = 10; // מספר השאלות במבחן
    private boolean isTestMode = false; // האם אנחנו במצב מבחן
    private CountDownTimer countDownTimer; // אובייקט טיימר

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // הפעלת ערכת נושא (מצב כהה/בהיר)
        super.onCreate(savedInstanceState); // קריאה ל־onCreate של ההורה
        setContentView(R.layout.activity_multiple_choice); // קביעת ה־layout של המסך

        // קישור רכיבי XML לקוד
        modeSwitch = findViewById(R.id.modeSwitch);
        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        optionButtons[0] = findViewById(R.id.optionA);
        optionButtons[1] = findViewById(R.id.optionB);
        optionButtons[2] = findViewById(R.id.optionC);
        optionButtons[3] = findViewById(R.id.optionD);
        setupDrawer(); // הפעלת תפריט הצד

        db = FirebaseFirestore.getInstance(); // התחברות ל־Firestore

        // מאזין למתג – שינוי מצב מבחן או תרגול
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE); // מציג טיימר רק במבחן
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        loadWordsFromFirebase(); // טעינת מילים שסומנו כ־known

        // חיבור מאזין לכל כפתור תשובה
        for (Button button : optionButtons) {
            button.setOnClickListener(this::checkAnswer);
        }
    }

    private void loadWordsFromFirebase() {
        db.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(result -> {
                    wordList.clear(); // איפוס רשימת מילים

                    for (QueryDocumentSnapshot doc : result) {
                        String wordText = doc.getString("word");
                        String definition = doc.getString("definition");
                        Boolean known = doc.getBoolean("known");

                        if (wordText != null && definition != null && Boolean.TRUE.equals(known)) {
                            // יצירת אובייקט Word רק אם המילה מוכרת
                            wordList.add(new Word(wordText, true, definition, doc.getId(), "workout1"));
                        }
                    }

                    if (wordList.size() < 4) {
                        Toast.makeText(this, "אתה צריך למיין לפחות 4 מילים!", Toast.LENGTH_LONG).show();
                        finish(); // יציאה מהמסך
                    } else {
                        showNextQuestion(); // מעבר לשאלה ראשונה
                    }
                });
    }

    private void showNextQuestion() {
        if (countDownTimer != null) countDownTimer.cancel(); // עצירת טיימר קודם
        timerText.setVisibility(View.GONE); // הסתרת טיימר

        if (wordList.size() < 4) {
            Toast.makeText(this, "לא מספיק מילים למבחן!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        List<Word> shuffled = new ArrayList<>(wordList);
        Collections.shuffle(shuffled); // ערבוב מילים

        currentWord = shuffled.get(0); // מילה לשאלה הנוכחית
        correctAnswer = currentWord.getDefinition(); // שמירת התשובה הנכונה

        // בניית רשימת תשובות
        List<String> options = new ArrayList<>();
        options.add(correctAnswer);
        options.add(shuffled.get(1).getDefinition());
        options.add(shuffled.get(2).getDefinition());
        options.add(shuffled.get(3).getDefinition());
        Collections.shuffle(options); // ערבוב תשובות

        questionText.setText("מה הפירוש של: " + currentWord.getWord()); // הצגת השאלה

        for (int i = 0; i < 4; i++) {
            optionButtons[i].setText(options.get(i)); // הצגת תשובה על כל כפתור
            optionButtons[i].setEnabled(true); // הפעלת לחיצה
        }

        if (isTestMode) startTimer(); // התחלת טיימר במצב מבחן
    }

    private void startTimer() {
        timerText.setVisibility(View.VISIBLE);
        timerText.setText("Time left: 7");

        countDownTimer = new CountDownTimer(7000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time left: " + millisUntilFinished / 1000); // עדכון טיימר
            }

            public void onFinish() {
                timerText.setText("Time's up!");
                totalQuestions++;
                Toast.makeText(MultipleChoiceActivity.this,
                        "⏱️ Time's up! The correct answer was: " + correctAnswer,
                        Toast.LENGTH_SHORT).show();

                if (totalQuestions >= MAX_QUESTIONS) {
                    showResult(); // הצגת תוצאה
                } else {
                    showNextQuestion(); // שאלה חדשה
                }
            }
        }.start(); // התחלת הטיימר
    }

    private void checkAnswer(View v) {
        if (countDownTimer != null) countDownTimer.cancel(); // עצירת טיימר
        timerText.setVisibility(View.GONE); // הסתרת טיימר

        Button clicked = (Button) v;
        String answer = clicked.getText().toString();
        totalQuestions++;

        if (answer.equals(correctAnswer)) {
            score++;
            Toast.makeText(this, "✔️ Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Incorrect! The correct answer was: " + correctAnswer, Toast.LENGTH_SHORT).show();
        }

        for (Button b : optionButtons) {
            b.setEnabled(false); // מניעת לחיצה כפולה
        }

        // השהיה של שנייה לפני השאלה הבאה
        v.postDelayed(() -> {
            if (totalQuestions >= MAX_QUESTIONS) {
                showResult();
            } else {
                showNextQuestion();
            }
        }, 1000);
    }

    private void showResult() {
        float successRate = ((float) score / totalQuestions) * 100f; // חישוב אחוז הצלחה
        FirebaseUtils.saveTestResult(this, "MultipleChoice", successRate); // שמירה ל־Firebase

        Intent intent = new Intent(this, QuizResultActivity.class); // מעבר למסך תוצאה
        intent.putExtra("score", score);
        intent.putExtra("total", totalQuestions);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel(); // ביטול טיימר בעת סגירה
        super.onDestroy();
    }
}
