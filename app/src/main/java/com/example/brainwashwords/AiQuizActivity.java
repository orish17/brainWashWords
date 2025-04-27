package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiQuizActivity extends AppCompatActivity {

    private TextView aiSentenceText;
    private EditText userInput;
    private Button checkAnswerButton;
    private TextView resultText;
    private Button nextQuestionButton;

    private String correctAnswer = "";
    private final String apiKey = "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"; // כאן תשים את המפתח מ-OpenRouter!!

    private boolean isWaitingForResponse = false; // 🔥 משתנה לניהול בקשות

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_quiz);

        aiSentenceText = findViewById(R.id.aiSentenceText);
        userInput = findViewById(R.id.userInput);
        checkAnswerButton = findViewById(R.id.checkAnswerButton);
        resultText = findViewById(R.id.resultText);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);

        loadNewQuestion();

        checkAnswerButton.setOnClickListener(v -> checkAnswer());
        nextQuestionButton.setOnClickListener(v -> {
            if (!isWaitingForResponse) {
                loadNewQuestion();
            } else {
                Toast.makeText(this, "Please wait for the AI to respond...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadNewQuestion() {
        aiSentenceText.setText("AI is thinking...");
        userInput.setText("");
        resultText.setVisibility(View.GONE);

        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(this, "⚠️ API Key missing. Check your configuration.", Toast.LENGTH_LONG).show();
            aiSentenceText.setText("Missing API key.");
            return;
        }

        isWaitingForResponse = true;
        nextQuestionButton.setEnabled(false);

        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "mistralai/mistral-7b-instruct");
            // 💬 ככה OpenRouter רוצה
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
                .url("https://openrouter.ai/api/v1/chat/completions")  // 💬 כתובת של OpenRouter
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://brainwashwords.com")  // 💬 חייב לשים משהו, אפשר דומיין פיקטיבי
                .addHeader("X-Title", "BrainWashWords App") // 💬 לא חובה, אבל נחמד
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
                        Log.e("AI_ERROR", response.code() + ": " + response.message());
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

    private void checkAnswer() {
        String userAnswer = userInput.getText().toString().trim();
        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            resultText.setText("✔️ Correct!");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            resultText.setText("❌ Incorrect. The missing word was: " + correctAnswer);
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        resultText.setVisibility(View.VISIBLE);
    }
}
