package com.example.brainwashwords; // הגדרת החבילה שבה נמצאת המחלקה

// ייבוא מחלקות נחוצות לעבודה עם דיבור, טיימר, תצוגה ו־UI
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.Locale;

/**
 * מחלקת SpeechToTextTestActivity
 * מבחן שמיעתי שבו המשתמש שומע מילה (TTS) ומנסה לחזור עליה בקול.
 * מזהה דיבור באמצעות SpeechRecognizer ומשווה לתשובה הנכונה.
 */
public class SpeechToTextTestActivity extends BaseActivity {

    private TextToSpeech tts; // מנוע להמרת טקסט לדיבור
    private SpeechRecognizer speechRecognizer; // מזהה דיבור
    private TextView timerText, resultText, feedbackText; // תצוגות: טיימר, תוצאה, פידבק
    private Button playButton, listenButton; // כפתורים להשמעה והאזנה
    private String currentWord = "example"; // המילה שנבדקת – ניתן להחלפה בעתיד
    private boolean isTestMode = false; // האם האפליקציה במצב מבחן או תרגול
    private Switch modeSwitch; // מתג מעבר בין מצב תרגול למבחן
    private CountDownTimer countDownTimer; // טיימר לספירה לאחור

    /**
     * onCreate – אתחול כל רכיבי המסך, TTS, זיהוי דיבור, תפריט צד, האזנה ללחיצות.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // הגדרת מצב תאורה (כהה/בהיר)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text_test); // קביעת layout XML

        // קישור רכיבי UI מתוך קובץ ה־XML
        timerText = findViewById(R.id.timerText);
        resultText = findViewById(R.id.resultText);
        feedbackText = findViewById(R.id.feedbackText);
        playButton = findViewById(R.id.playButton);
        listenButton = findViewById(R.id.listenButton);
        modeSwitch = findViewById(R.id.modeSwitch);
        setupDrawer(); // תפריט צד

        setupTTS(); // אתחול TextToSpeech
        setupSpeechRecognizer(); // אתחול זיהוי דיבור

        // לחיצה על כפתור ההשמעה: מדבר את המילה
        playButton.setOnClickListener(v -> speakWord(currentWord));

        // לחיצה על כפתור ההאזנה: במצב מבחן עם טיימר, אחרת תרגול רגיל
        listenButton.setOnClickListener(v -> {
            if (isTestMode) {
                startTimerAndListen(); // התחלה עם טיימר
            } else {
                startListening(); // התחלה ישירה
            }
        });

        // מתג למעבר בין מצב מבחן למצב תרגול
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked; // עדכון המצב
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE); // הצגת טיימר רק במבחן
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated", // הודעה
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * אתחול מנוע TTS – הגדרת שפה ומוכנות לדיבור.
     */
    private void setupTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US); // הגדרת שפה לאנגלית אמריקאית
            }
        });
    }

    /**
     * קריאת המילה הנוכחית בקול.
     */
    private void speakWord(String word) {
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null); // דיבור מיידי
    }

    /**
     * אתחול מנגנון זיהוי דיבור והאזנה לתוצאה.
     */
    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this); // יצירת מזהה דיבור
        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override public void onReadyForSpeech(Bundle params) {} // מוכנות
            @Override public void onBeginningOfSpeech() {} // תחילת דיבור
            @Override public void onRmsChanged(float rmsdB) {} // שינוי עוצמת קול
            @Override public void onBufferReceived(byte[] buffer) {} // קבלת נתונים
            @Override public void onEndOfSpeech() {} // סיום דיבור

            @Override
            public void onError(int error) {
                feedbackText.setText("Error recognizing speech. Try again."); // שגיאה
            }

            /**
             * עיבוד התוצאה: בדיקה אם המילה שנאמרה תואמת למילה הנכונה.
             */
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0); // קבלת המשפט הראשון שנשמע
                    resultText.setText("You said: " + spokenText); // תצוגה
                    if (spokenText.equalsIgnoreCase(currentWord)) { // בדיקה אם נכון
                        feedbackText.setText("✅ Correct!");
                    } else {
                        feedbackText.setText("❌ Try again");
                    }
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {} // תוצאה חלקית
            @Override public void onEvent(int eventType, Bundle params) {} // אירוע כללי
        });
    }

    /**
     * הפעלת טיימר של 5 שניות לפני תחילת האזנה (במצב מבחן).
     */
    private void startTimerAndListen() {
        timerText.setVisibility(View.VISIBLE); // הצגת טיימר
        countDownTimer = new CountDownTimer(5000, 1000) { // טיימר של 5 שניות
            public void onTick(long millisUntilFinished) {
                timerText.setText("Speak in: " + millisUntilFinished / 1000); // ספירה לאחור
            }

            public void onFinish() {
                timerText.setText("Listening..."); // התחלה
                startListening(); // התחלת האזנה
            }
        }.start();
    }

    /**
     * התחלת האזנה לדיבור של המשתמש.
     */
    private void startListening() {
        timerText.setVisibility(View.GONE); // הסתרת טיימר
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // כוונה להאזנה
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); // מודל שפה
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US); // שפה אנגלית
        speechRecognizer.startListening(intent); // התחלת האזנה
    }

    /**
     * שחרור משאבים כשעוזבים את המסך (TTS, זיהוי דיבור, טיימר).
     */
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop(); // עצירה
            tts.shutdown(); // סגירה
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy(); // ניקוי
        }
        if (countDownTimer != null) {
            countDownTimer.cancel(); // עצירת טיימר
        }
        super.onDestroy(); // סיום רגיל
    }
}
