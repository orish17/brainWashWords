package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;
import java.util.Locale;

public class AiQuizActivity extends AppCompatActivity {

    private TextView aiSentenceText;
    private EditText userInput;
    private Button checkAnswerButton, nextQuestionButton;
    private TextView resultText;

    private final String apiKey = "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"; //
    private final String endpoint = "https://openrouter.ai/api/v1/chat/completions";
    private final String model = "mistralai/mistral-7b-instruct";

    private String correctWord = "absorb"; // לדוגמה – בהמשך נטען אקראית

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_quiz);

        aiSentenceText = findViewById(R.id.aiSentenceText);
        userInput = findViewById(R.id.userInput);
        checkAnswerButton = findViewById(R.id.checkAnswerButton);
        nextQuestionButton = findViewById(R.id.nextQuestionButton);
        resultText = findViewById(R.id.resultText);

        checkAnswerButton.setOnClickListener(v -> checkAnswer());
        nextQuestionButton.setOnClickListener(v -> generateSentence());

        generateSentence();
    }

    private void generateSentence() {
        resultText.setVisibility(View.GONE);
        userInput.setText("");
        aiSentenceText.setText("AI is thinking...");

        // בהמשך נחליף את זה במילה אקראית שהמשתמש מכיר
        correctWord = "absorb";

        OkHttpClient client = new OkHttpClient();

        JSONObject message = new JSONObject();
        try {
            message.put("role", "user");
            message.put("content", "Create an English sentence using the word '" + correctWord +
                    "', but replace the word with a blank (____). Respond only with the sentence.");
        } catch (JSONException e) {
            aiSentenceText.setText("Error building message.");
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("model", model);
            body.put("messages", new JSONArray().put(message));
        } catch (JSONException e) {
            aiSentenceText.setText("Error building request.");
            return;
        }

        Request request = new Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("HTTP-Referer", "https://yourapp.com")
                .addHeader("X-Title", "BrainWashOrish")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> aiSentenceText.setText("Failed to connect to AI."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resStr = response.body().string();
                Log.d("AI_RESPONSE", resStr);

                try {
                    JSONObject json = new JSONObject(resStr);
                    String content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    runOnUiThread(() -> aiSentenceText.setText(content.trim()));
                } catch (Exception e) {
                    runOnUiThread(() -> aiSentenceText.setText("Failed to parse AI response."));
                }
            }
        });
    }

    private void checkAnswer() {
        String userAnswer = userInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        if (userAnswer.equals(correctWord.toLowerCase())) {
            resultText.setText("✅ Correct!");
        } else {
            resultText.setText("❌ Incorrect. The correct word was: " + correctWord);
        }
        resultText.setVisibility(View.VISIBLE);
    }
}
