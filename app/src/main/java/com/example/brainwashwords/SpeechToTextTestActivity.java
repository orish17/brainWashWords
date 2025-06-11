package com.example.brainwashwords;

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

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private TextView timerText, resultText, feedbackText;
    private Button playButton, listenButton;
    private String currentWord = "example"; // מילה קבועה לדוגמה – ניתן להרחיב בהמשך
    private boolean isTestMode = false;
    private Switch modeSwitch;
    private CountDownTimer countDownTimer;

    /**
     * onCreate – אתחול רכיבי ממשק, הגדרות TTS ודיבור, מצבי תרגול/מבחן.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text_test);

        // אתחול רכיבי ממשק
        timerText = findViewById(R.id.timerText);
        resultText = findViewById(R.id.resultText);
        feedbackText = findViewById(R.id.feedbackText);
        playButton = findViewById(R.id.playButton);
        listenButton = findViewById(R.id.listenButton);
        modeSwitch = findViewById(R.id.modeSwitch);
        setupDrawer();

        // אתחול TTS ודיבור
        setupTTS();
        setupSpeechRecognizer();

        // כפתור להפעלת השמעת מילה
        playButton.setOnClickListener(v -> speakWord(currentWord));

        // כפתור להאזנה לדיבור של המשתמש
        listenButton.setOnClickListener(v -> {
            if (isTestMode) {
                startTimerAndListen(); // טיימר ואז האזנה
            } else {
                startListening(); // האזנה ישירה
            }
        });

        // החלפת מצב מבחן/תרגול
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE);
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * אתחול TextToSpeech – המרה מטקסט לדיבור.
     */
    private void setupTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    /**
     * השמעת המילה הנוכחית.
     */
    private void speakWord(String word) {
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    /**
     * אתחול SpeechRecognizer לזיהוי דיבור.
     */
    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                feedbackText.setText("Error recognizing speech. Try again.");
            }

            /**
             * טיפול בתוצאה: השוואת הטקסט המדובר למילה הנכונה.
             */
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0); // המשפט הראשון שנאמר
                    resultText.setText("You said: " + spokenText);
                    if (spokenText.equalsIgnoreCase(currentWord)) {
                        feedbackText.setText("✅ Correct!");
                    } else {
                        feedbackText.setText("❌ Try again");
                    }
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    /**
     * התחלת טיימר של 5 שניות לפני התחלת ההאזנה.
     */
    private void startTimerAndListen() {
        timerText.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Speak in: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerText.setText("Listening...");
                startListening();
            }
        }.start();
    }

    /**
     * התחלת ההאזנה לקול המשתמש.
     */
    private void startListening() {
        timerText.setVisibility(View.GONE);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        speechRecognizer.startListening(intent);
    }

    /**
     * שחרור משאבים – TTS, דיבור, טיימר.
     */
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
