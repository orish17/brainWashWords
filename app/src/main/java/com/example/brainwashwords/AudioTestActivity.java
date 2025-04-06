package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

    private FirebaseFirestore db;
    private List<Word> wordList = new ArrayList<>();
    private Word currentWord;

    private int score = 0;
    private int totalQuestions = 0;
    private static final int MAX_QUESTIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        answerInput = findViewById(R.id.editAnswer);
        playButton = findViewById(R.id.btnPlay);
        submitButton = findViewById(R.id.btnSubmit);

        db = FirebaseFirestore.getInstance();
        loadWords();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        playButton.setOnClickListener(v -> speakWord());
        submitButton.setOnClickListener(v -> checkAnswer());
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
        answerInput.setText("");
        Collections.shuffle(wordList);
        currentWord = wordList.get(0);
        speakWord();
    }

    private void speakWord() {
        if (tts != null && currentWord != null) {
            tts.speak(currentWord.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void checkAnswer() {
        String userAnswer = answerInput.getText().toString().trim();
        totalQuestions++;

        if (userAnswer.equalsIgnoreCase(currentWord.getWord())) {
            score++;
            Toast.makeText(this, "✔️ Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Incorrect! The word was: " + currentWord.getWord(), Toast.LENGTH_SHORT).show();
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
    }
}
