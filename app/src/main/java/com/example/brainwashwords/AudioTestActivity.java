package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AudioTestActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        answerInput = findViewById(R.id.editAnswer);
        playButton = findViewById(R.id.btnPlay);
        submitButton = findViewById(R.id.btnSubmit);
        modeSwitch = findViewById(R.id.modeSwitch);
        timerText = findViewById(R.id.timerText);

        db = FirebaseFirestore.getInstance();
        loadWords();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

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

    private void showNextWord() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setVisibility(View.GONE);

        answerInput.setText("");
        Collections.shuffle(wordList);
        currentWord = wordList.get(0);
        speakWord();

        if (isTestMode) startTimer();
    }

    private void speakWord() {
        if (tts != null && currentWord != null) {
            tts.speak(currentWord.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

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

    private void showResult() {
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total", totalQuestions);
        startActivity(intent);
        finish();
    }

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
