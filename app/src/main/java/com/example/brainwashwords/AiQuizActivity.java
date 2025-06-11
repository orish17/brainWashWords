package com.example.brainwashwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.json.*;

import java.io.IOException;

import okhttp3.*;

/**
 * Activity that displays an AI-generated sentence with a missing word.
 * The user must guess the missing word within a time limit (if test mode is enabled).
 * Supports Practice and Test modes, using OpenRouter AI model.
 */
public class AiQuizActivity extends BaseActivity {

    private TextView aiSentenceText, resultText, timerText;
    private EditText userInput;
    private Button checkAnswerButton, nextQuestionButton;
    private Switch modeSwitch;

    private String correctAnswer = "";
    private boolean isWaitingForResponse = false;
    private boolean isTestMode = false;
    private CountDownTimer countDownTimer;

    private final String apiKey = "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"; // 🔐 מפתח ל־OpenRouter –

    private int score = 0;
    private int totalQuestions = 0;
    private static final int MAX_QUESTIONS = 10;

    /**
     * onCreate – מופעל בעת טעינת המסך. קושר UI, מפעיל תפריט צד ומעלה שאלה ראשונה.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_quiz);

        // קישור רכיבי UI
        aiSentenceText = findViewById(R.id.aiSentenceText);
        userInput = findViewById(R.id.userInput);
        checkAnswerButton = findViewById(R.id.checkAnswerButton);
        resultText = findViewById(R.id.resultText);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        modeSwitch = findViewById(R.id.modeSwitch);
        timerText = findViewById(R.id.timerText);
        setupDrawer();

        loadNewQuestion(); // שאלה ראשונה

        // מצב תרגול או מבחן (עם טיימר)
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE);
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        // בדיקת תשובה
        checkAnswerButton.setOnClickListener(v -> checkAnswer());

        // שאלה חדשה
        nextQuestionButton.setOnClickListener(v -> {
            if (!isWaitingForResponse) {
                loadNewQuestion();
            } else {
                Toast.makeText(this, "Please wait for the AI to respond...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * שולף שאלה חדשה ממודל AI דרך OpenRouter.
     */
    private void loadNewQuestion() {
        aiSentenceText.setText("AI is thinking...");
        userInput.setText("");
        resultText.setVisibility(View.GONE);
        timerText.setVisibility(View.GONE);

        if (countDownTimer != null) countDownTimer.cancel();

        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(this, "⚠️ API Key missing. Check your configuration.", Toast.LENGTH_LONG).show();
            aiSentenceText.setText("Missing API key.");
            return;
        }

        isWaitingForResponse = true;
        nextQuestionButton.setEnabled(false);

        // שליחת בקשת POST למודל
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "mistralai/mistral-7b-instruct");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "Create an English sentence with a missing word. Write the sentence, then in a new line write 'Missing word:' and specify the missing word.");
            messages.put(message);
            jsonBody.put("messages", messages);
        } catch (JSONException e) {
            e.printStackTrace();
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
                    aiSentenceText.setText("Failed to connect to AI.");
                    Toast.makeText(AiQuizActivity.this, "❌ Network error", Toast.LENGTH_SHORT).show();
                    isWaitingForResponse = false;
                    nextQuestionButton.setEnabled(true);
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string();
                Log.d("AI_RESPONSE", responseData);

                runOnUiThread(() -> {
                    isWaitingForResponse = false;
                    nextQuestionButton.setEnabled(true);

                    if (!response.isSuccessful()) {
                        aiSentenceText.setText("Error: " + response.code());
                        Toast.makeText(AiQuizActivity.this, "❌ AI server error: " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray choices = json.getJSONArray("choices");
                        JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");
                        String content = messageObj.getString("content");

                        String[] parts = content.split("Missing word:");
                        if (parts.length == 2) {
                            String question = parts[0].trim();
                            correctAnswer = parts[1].trim();
                            aiSentenceText.setText(question);

                            if (isTestMode) startCountdownTimer();
                        } else {
                            aiSentenceText.setText("Invalid AI response.");
                        }
                    } catch (JSONException e) {
                        aiSentenceText.setText("Failed to parse AI response.");
                        Log.e("AI_PARSE_ERROR", e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * מפעיל טיימר של 15 שניות במצב מבחן.
     */
    private void startCountdownTimer() {
        timerText.setVisibility(View.VISIBLE);
        timerText.setText("Time left: 15");

        countDownTimer = new CountDownTimer(15000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time left: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                String userAnswer = userInput.getText().toString().trim();
                if (userAnswer.isEmpty()) {
                    totalQuestions++;
                    resultText.setText("❌ Time's up! The correct answer was: " + correctAnswer);
                    resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    resultText.setVisibility(View.VISIBLE);
                    checkIfEnd();
                }
            }
        }.start();
    }

    /**
     * בודק את התשובה שהמשתמש כתב מול התשובה הנכונה.
     */
    private void checkAnswer() {
        String userAnswer = userInput.getText().toString().trim();
        if (countDownTimer != null) countDownTimer.cancel();

        if (!isTestMode) {
            Toast.makeText(this, "Practice Mode: Answer not checked", Toast.LENGTH_SHORT).show();
            return;
        }

        totalQuestions++;

        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            score++;
            resultText.setText("✔️ Correct!");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            resultText.setText("❌ Incorrect. The missing word was: " + correctAnswer);
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        resultText.setVisibility(View.VISIBLE);
        checkIfEnd();
    }

    /**
     * בודק אם סיימנו את מספר השאלות המקסימלי, שומר תוצאה ל־Firebase ומאפס.
     */
    private void checkIfEnd() {
        if (totalQuestions >= MAX_QUESTIONS) {
            float successRate = ((float) score / totalQuestions) * 100f;
            FirebaseUtils.saveTestResult(this, "AiTest", successRate);

            Toast.makeText(this, "Test completed! Score: " + score + "/" + totalQuestions, Toast.LENGTH_LONG).show();
            score = 0;
            totalQuestions = 0;
        }

        // שאלה חדשה בעוד 2 שניות
        new android.os.Handler().postDelayed(this::loadNewQuestion, 2000);
    }

    /**
     * onDestroy – מנקה את הטיימר כדי למנוע נזילות זיכרון.
     */
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
