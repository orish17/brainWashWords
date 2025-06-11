package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;

import com.google.firebase.firestore.*;

import java.util.*;

/**
 * AudioTestActivity – מבחן שמיעה שבו מושמעת מילה והמשתמש צריך לרשום אותה.
 * כולל מצב תרגול ומצב מבחן (עם טיימר), ניקוד, ודיווח תוצאה ל־Firebase.
 */
public class AudioTestActivity extends BaseActivity {

    private TextToSpeech tts;
    private EditText answerInput;
    private Button playButton, submitButton;
    private Switch modeSwitch;
    private TextView timerText;

    private FirebaseFirestore db;
    private List<Word> wordList = new ArrayList<>();
    private Word currentWord;

    private int score = 0;
    private int totalQuestions = 0;
    private static final int MAX_QUESTIONS = 10;

    private boolean isTestMode = false;
    private CountDownTimer countDownTimer;

    /**
     * onCreate – אתחול רכיבי המסך, שליפת מילים מה־Firebase, והפעלת TTS.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        // קישור רכיבי תצוגה
        answerInput = findViewById(R.id.editAnswer);
        playButton = findViewById(R.id.btnPlay);
        submitButton = findViewById(R.id.btnSubmit);
        modeSwitch = findViewById(R.id.modeSwitch);
        timerText = findViewById(R.id.timerText);
        setupDrawer();

        db = FirebaseFirestore.getInstance();
        loadWords();

        // אתחול TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        // מצב מבחן או תרגול
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE);
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        playButton.setOnClickListener(v -> speakWord());
        submitButton.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            timerText.setVisibility(View.GONE);
            checkAnswer();
        });
    }

    /**
     * שליפת מילים מסומנות כ־known מתוך הקבוצה workout1.
     */
    private void loadWords() {
        db.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(query -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String wordText = doc.getString("word");
                        Boolean known = doc.getBoolean("known");
                        String definition = doc.getString("definition");

                        if (wordText != null && Boolean.TRUE.equals(known)) {
                            wordList.add(new Word(wordText, true, definition, doc.getId(), "workout1"));
                        }
                    }

                    if (wordList.size() < 4) {
                        Toast.makeText(this, "Please mark at least 4 known words.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        showNextWord();
                    }
                });
    }

    /**
     * מציג מילה חדשה להשמעה ובודק האם יש צורך להתחיל טיימר.
     */
    private void showNextWord() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setVisibility(View.GONE);

        answerInput.setText("");
        Collections.shuffle(wordList);
        currentWord = wordList.get(0);
        speakWord();

        if (isTestMode) startTimer();
    }

    /**
     * משמיע את המילה הנוכחית באמצעות TextToSpeech.
     */
    private void speakWord() {
        if (tts != null && currentWord != null) {
            tts.speak(currentWord.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    /**
     * מפעיל טיימר של 10 שניות במצב מבחן.
     */
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
                    showResult();
                } else {
                    showNextWord();
                }
            }
        }.start();
    }

    /**
     * בודק האם תשובת המשתמש תואמת למילה שהושמעה.
     */
    private void checkAnswer() {
        String userAnswer = answerInput.getText().toString().trim();
        totalQuestions++;

        if (userAnswer.equalsIgnoreCase(currentWord.getWord())) {
            score++;
            Toast.makeText(this, "✔️ יפה מאוד!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ טעית, המילה הייתה: " + currentWord.getWord(), Toast.LENGTH_SHORT).show();
        }

        if (totalQuestions >= MAX_QUESTIONS) {
            showResult();
        } else {
            showNextWord();
        }
    }

    /**
     * הצגת תוצאה, שמירה ל־Firebase, ומעבר למסך סיכום.
     */
    private void showResult() {
        float successRate = ((float) score / totalQuestions) * 100f;
        FirebaseUtils.saveTestResult(this, "AudioTest", successRate);

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total", totalQuestions);
        startActivity(intent);
        finish();
    }

    /**
     * סגירת TTS וביטול טיימר כשהמסך נהרס.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
