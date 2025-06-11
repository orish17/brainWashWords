package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.*;

import org.json.*;

import java.io.IOException;
import java.util.*;

import okhttp3.*;

/**
 * FillInBlankActivity – פעילות המבחן שבה מוצג משפט עם מילה חסרה שהמשתמש צריך להשלים.
 * המשפט נוצר על ידי AI (באמצעות OpenRouter), על בסיס מילה שסומנה כ־known.
 * המבחן כולל מצב מבחן עם טיימר של 15 שניות, ניקוד, ושמירה ל־Firebase.
 */
public class FillInBlankActivity extends BaseActivity {

    private TextView sentenceText, timerText;
    private EditText userInput;
    private Button submitBtn;
    private Switch modeSwitch;

    private FirebaseFirestore db;
    private List<Word> wordList = new ArrayList<>();
    private Word currentWord;

    private int score = 0;
    private int totalQuestions = 0;
    private static final int MAX_QUESTIONS = 10;

    private final String apiKey = "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."; // מפתח ל־OpenRouter – יש להחליף בזה שלך

    private boolean isWaitingForResponse = false;
    private boolean isTestMode = false;
    private CountDownTimer countDownTimer;

    private String workoutName;

    /**
     * מופעל כשנוצר המסך. טוען את הקבוצה, המילים, ומאתחל UI + מאזינים.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_blank);

        // קישור רכיבי UI
        sentenceText = findViewById(R.id.sentenceText);
        userInput = findViewById(R.id.editAnswer);
        submitBtn = findViewById(R.id.btnSubmit);
        modeSwitch = findViewById(R.id.modeSwitch);
        timerText = findViewById(R.id.timerText);
        setupDrawer();

        db = FirebaseFirestore.getInstance();

        // קבלת שם הקבוצה מהמבחן
        workoutName = getIntent().getStringExtra("workoutName");
        if (workoutName == null) workoutName = "workout1";

        // מעבר בין מצב תרגול למבחן
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE);
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        // שליפת מילים מהקבוצה
        loadWords(workoutName);

        // לחיצה על כפתור שליחה
        submitBtn.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            timerText.setVisibility(View.GONE);
            checkAnswer();
        });
    }

    /**
     * טוען את המילים שסומנו כ־known מתוך קבוצת המילים שנבחרה.
     */
    private void loadWords(String workoutName) {
        db.collection("groups").document(workoutName).collection("words")
                .get()
                .addOnSuccessListener(query -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String wordText = doc.getString("word");
                        String definition = doc.getString("definition");
                        Boolean known = doc.getBoolean("known");

                        if (wordText != null && Boolean.TRUE.equals(known)) {
                            wordList.add(new Word(wordText, true, definition, doc.getId(), workoutName));
                        }
                    }

                    if (wordList.size() < 4) {
                        Toast.makeText(this, "Please mark at least 4 known words to start the test.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        showNextQuestion();
                    }
                });
    }

    /**
     * מציג שאלה חדשה על סמך מילה אקראית מהרשימה.
     */
    private void showNextQuestion() {
        if (isWaitingForResponse) {
            Toast.makeText(this, "Wait for the AI to respond...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setVisibility(View.GONE);

        userInput.setText("");
        Collections.shuffle(wordList);
        currentWord = wordList.get(0);

        generateSentence(currentWord.getWord());

        if (isTestMode) startTimer();
    }

    /**
     * מפעיל טיימר של 15 שניות (מצב מבחן).
     */
    private void startTimer() {
        timerText.setVisibility(View.VISIBLE);
        timerText.setText("Time left: 15");

        countDownTimer = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                String answer = userInput.getText().toString().trim();
                if (answer.isEmpty()) {
                    totalQuestions++;
                    Toast.makeText(FillInBlankActivity.this,
                            "⏱️ Time's up! The correct word was: " + currentWord.getWord(),
                            Toast.LENGTH_SHORT).show();

                    if (totalQuestions >= MAX_QUESTIONS) {
                        showResult();
                    } else {
                        showNextQuestion();
                    }
                }
            }
        }.start();
    }

    /**
     * שולח בקשה ל-AI ליצירת משפט עם מקום ריק על בסיס מילה.
     */
    private void generateSentence(String word) {
        sentenceText.setText("AI is thinking...");
        isWaitingForResponse = true;

        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "mistralai/mistral-7b-instruct");
            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "Write an English sentence using the word '" + word + "', but replace the word with a blank (____). Respond only with the sentence.");
            messages.put(message);
            jsonBody.put("messages", messages);
        } catch (JSONException e) {
            runOnUiThread(() -> sentenceText.setText("Error building request."));
            return;
        }

        Request request = new Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://brainwashwords.com")
                .addHeader("X-Title", "BrainWashWords App")
                .post(RequestBody.create(jsonBody.toString(), MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    sentenceText.setText("❌ Failed to connect to AI.");
                    isWaitingForResponse = false;
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isWaitingForResponse = false;
                String resStr = response.body().string();

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> sentenceText.setText("❌ AI Error: " + response.code()));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(resStr);
                    String content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    runOnUiThread(() -> sentenceText.setText(content.trim()));
                } catch (Exception e) {
                    runOnUiThread(() -> sentenceText.setText("Error parsing AI response"));
                }
            }
        });
    }

    /**
     * בודק אם תשובת המשתמש נכונה או לא.
     */
    private void checkAnswer() {
        String answer = userInput.getText().toString().trim();
        totalQuestions++;

        if (answer.equalsIgnoreCase(currentWord.getWord())) {
            score++;
            Toast.makeText(this, "✔️ Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Incorrect! The correct word was: " + currentWord.getWord(), Toast.LENGTH_SHORT).show();
        }

        if (totalQuestions >= MAX_QUESTIONS) {
            showResult();
        } else {
            showNextQuestion();
        }
    }

    /**
     * מציג את תוצאת המבחן, שומר אותה ל־Firebase, ועובר למסך תוצאה.
     */
    private void showResult() {
        float successRate = ((float) score / totalQuestions) * 100f;

        // שמירה ל־Firebase תחת FillInBlank
        FirebaseUtils.saveTestResult(this, "FillInBlank", successRate);

        // מעבר למסך תוצאה
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total", totalQuestions);
        startActivity(intent);
        finish();
    }

    /**
     * ביטול הטיימר בעת סגירת המסך.
     */
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
