package com.example.brainwashwords; // הגדרת מיקום המחלקה בתוך החבילה הראשית של האפליקציה

import android.content.Intent; // מאפשר מעבר בין מסכים (Activities)
import android.os.Bundle; // אובייקט שמעביר מידע בין מסכים בעת פתיחה
import android.os.CountDownTimer; // טיימר לספירה לאחור – משמש במצב מבחן
import android.speech.tts.TextToSpeech; // רכיב שמקריא טקסט בקול
import android.view.View; // בסיס לכל רכיב UI
import android.widget.*; // כולל כפתורים, טקסטים, תיבות קלט ועוד

import com.google.firebase.firestore.*; // עבודה עם מסד הנתונים Firestore

import java.util.*; // כולל רשימות, מחוללים, Collections וכו'

/**
 * AudioTestActivity – מבחן שמיעה שבו מושמעת מילה והמשתמש צריך לרשום אותה.
 * כולל מצב תרגול ומצב מבחן (עם טיימר), ניקוד, ודיווח תוצאה ל־Firebase.
 */
public class AudioTestActivity extends BaseActivity { // המחלקה יורשת מ־BaseActivity כדי להשתמש בתפריט צד וכלים משותפים

    private TextToSpeech tts; // מנוע דיבור שמשמיע את המילים
    private EditText answerInput; // שדה קלט למשתמש לכתוב את המילה
    private Button playButton, submitButton; // כפתור להשמעה וכפתור לבדיקה
    private Switch modeSwitch; // מתג בין מצב מבחן למצב תרגול
    private TextView timerText; // טיימר שמוצג על המסך

    private FirebaseFirestore db; // חיבור למסד הנתונים בענן (Firestore)
    private List<Word> wordList = new ArrayList<>(); // רשימת מילים לאימון
    private Word currentWord; // המילה הנוכחית לתרגול

    private int score = 0; // מספר תשובות נכונות
    private int totalQuestions = 0; // סך השאלות שנענו
    private static final int MAX_QUESTIONS = 10; // מקסימום שאלות למבחן

    private boolean isTestMode = false; // האם מצב מבחן מופעל
    private CountDownTimer countDownTimer; // טיימר ספציפי לשאלה

    @Override
    protected void onCreate(Bundle savedInstanceState) { // מופעל בעת פתיחת המסך
        ThemeHelper.applySavedTheme(this); // מפעיל את ערכת הצבעים הנבחרת
        super.onCreate(savedInstanceState); // קריאה למחלקת העל
        setContentView(R.layout.activity_audio_test); // קישור לקובץ XML המתאים

        answerInput = findViewById(R.id.editAnswer); // שדה שבו המשתמש כותב את המילה
        playButton = findViewById(R.id.btnPlay); // כפתור השמעת מילה
        submitButton = findViewById(R.id.btnSubmit); // כפתור בדיקת תשובה
        modeSwitch = findViewById(R.id.modeSwitch); // מתג תרגול/מבחן
        timerText = findViewById(R.id.timerText); // טקסט של הטיימר
        setupDrawer(); // תפריט צד

        db = FirebaseFirestore.getInstance(); // אתחול החיבור למסד הנתונים
        loadWords(); // טעינת מילים לתרגול/מבחן

        // אתחול מנוע TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US); // הגדרת שפה לאנגלית אמריקאית
            }
        });

        // מתג מצב – בין תרגול למבחן
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked; // עדכון מצב
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE); // הצגת טיימר רק אם מבחן
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show(); // הודעה מתאימה
        });

        playButton.setOnClickListener(v -> speakWord()); // האזנה ללחיצה על כפתור השמעה
        submitButton.setOnClickListener(v -> { // האזנה לכפתור בדיקה
            if (countDownTimer != null) countDownTimer.cancel(); // ביטול טיימר אם יש
            timerText.setVisibility(View.GONE); // הסתרת הטיימר
            checkAnswer(); // בדיקת תשובה
        });
    }

    // שליפת מילים מסומנות כ־known מקבוצת workout1
    private void loadWords() {
        db.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(query -> {
                    wordList.clear(); // מנקה את הרשימה
                    for (QueryDocumentSnapshot doc : query) {
                        String wordText = doc.getString("word");
                        Boolean known = doc.getBoolean("known");
                        String definition = doc.getString("definition");

                        if (wordText != null && Boolean.TRUE.equals(known)) {
                            wordList.add(new Word(wordText, true, definition, doc.getId(), "workout1")); // מוסיף לרשימה רק מילים שסומנו כ־known
                        }
                    }

                    if (wordList.size() < 4) { // אם יש פחות מדי מילים – לא מאפשר מבחן
                        Toast.makeText(this, "Please mark at least 4 known words.", Toast.LENGTH_LONG).show();
                        finish(); // יוצא מהמסך
                    } else {
                        showNextWord(); // ממשיך לשאלה ראשונה
                    }
                });
    }

    // מציג מילה חדשה לשאלה
    private void showNextWord() {
        if (countDownTimer != null) countDownTimer.cancel(); // ביטול טיימר קודם
        timerText.setVisibility(View.GONE); // הסתרת טיימר

        answerInput.setText(""); // איפוס שדה קלט
        Collections.shuffle(wordList); // ערבוב רשימת מילים
        currentWord = wordList.get(0); // בוחרים את הראשונה
        speakWord(); // משמיעים אותה

        if (isTestMode) startTimer(); // במצב מבחן – מפעילים טיימר
    }

    // משמיע את המילה באמצעות TextToSpeech
    private void speakWord() {
        if (tts != null && currentWord != null) {
            tts.speak(currentWord.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // טיימר של 10 שניות (לשאלה אחת)
    private void startTimer() {
        timerText.setVisibility(View.VISIBLE);
        timerText.setText("Time left: 10");

        countDownTimer = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                Toast.makeText(AudioTestActivity.this,
                        "⏱️ נגמר הזמן! המילה הייתה: " + currentWord.getWord(),
                        Toast.LENGTH_SHORT).show();

                totalQuestions++;
                if (totalQuestions >= MAX_QUESTIONS) {
                    showResult(); // סיום
                } else {
                    showNextWord(); // שאלה חדשה
                }
            }
        }.start();
    }

    // בדיקת תשובת המשתמש
    private void checkAnswer() {
        String userAnswer = answerInput.getText().toString().trim(); // שליפת תשובה
        totalQuestions++;

        if (userAnswer.equalsIgnoreCase(currentWord.getWord())) { // אם תשובה נכונה
            score++;
            Toast.makeText(this, "✔️ יפה מאוד!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ טעית, המילה הייתה: " + currentWord.getWord(), Toast.LENGTH_SHORT).show();
        }

        if (totalQuestions >= MAX_QUESTIONS) {
            showResult(); // מציג תוצאה
        } else {
            showNextWord(); // עובר לשאלה הבאה
        }
    }

    // תוצאה סופית + שמירה ל־Firebase + מעבר למסך סיכום
    private void showResult() {
        float successRate = ((float) score / totalQuestions) * 100f; // חישוב אחוז
        FirebaseUtils.saveTestResult(this, "AudioTest", successRate); // שמירה ב־Firebase

        Intent intent = new Intent(this, QuizResultActivity.class); // מעבר למסך תוצאה
        intent.putExtra("score", score); // מעביר ניקוד
        intent.putExtra("total", totalQuestions); // מעביר מספר שאלות
        startActivity(intent);
        finish(); // סוגר את המסך הנוכחי
    }

    // סגירת משאבים כשמסך נהרס
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop(); // עצירת הקראה
            tts.shutdown(); // שחרור משאב TTS
        }
        if (countDownTimer != null) {
            countDownTimer.cancel(); // ביטול טיימר אם רץ
        }
    }
}
