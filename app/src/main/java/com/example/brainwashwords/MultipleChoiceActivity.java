package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultipleChoiceActivity extends AppCompatActivity {

    private TextView questionText, timerText;
    private Button[] optionButtons = new Button[4];
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice);

        modeSwitch = findViewById(R.id.modeSwitch);
        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        optionButtons[0] = findViewById(R.id.optionA);
        optionButtons[1] = findViewById(R.id.optionB);
        optionButtons[2] = findViewById(R.id.optionC);
        optionButtons[3] = findViewById(R.id.optionD);

        db = FirebaseFirestore.getInstance();

        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE);
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        loadWordsFromFirebase();

        for (Button button : optionButtons) {
            button.setOnClickListener(this::checkAnswer);
        }
    }

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

        currentWord = shuffled.get(0);
        correctAnswer = currentWord.getDefinition();

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

        for (Button b : optionButtons) {
            b.setEnabled(false);
        }

        v.postDelayed(() -> {
            if (totalQuestions >= MAX_QUESTIONS) {
                showResult();
            } else {
                showNextQuestion();
            }
        }, 1000);
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
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
