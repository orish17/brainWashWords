package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    private final String API_KEY = ""; // <-- כאן תכניס את ה-API KEY שלך

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
        nextQuestionButton.setOnClickListener(v -> loadNewQuestion());
    }

    private void loadNewQuestion() {
        aiSentenceText.setText("AI is thinking...");
        userInput.setText("");
        resultText.setVisibility(View.GONE);

        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", "Create an English sentence with a missing word. Write the sentence, then in a new line write 'Missing word:' and specify the missing word.");
            messages.put(message);

            jsonBody.put("messages", messages);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(), MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    aiSentenceText.setText("Failed to load question.");
                    Toast.makeText(AiQuizActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        JSONArray choices = json.getJSONArray("choices");
                        JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");
                        String content = messageObj.getString("content");

                        // פיצול השאלה מהתשובה
                        String[] parts = content.split("Missing word:");
                        if (parts.length == 2) {
                            String question = parts[0].trim();
                            correctAnswer = parts[1].trim();

                            runOnUiThread(() -> aiSentenceText.setText(question));
                        } else {
                            runOnUiThread(() -> aiSentenceText.setText("Invalid response from AI."));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("AI_ERROR", response.code() + ": " + response.message());
                }
            }
        });
    }

    private void checkAnswer() {
        String userAnswer = userInput.getText().toString().trim();
        if (userAnswer.equalsIgnoreCase(correctAnswer)) {
            resultText.setText("Correct!");
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            resultText.setText("Incorrect. The missing word was: " + correctAnswer);
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        resultText.setVisibility(View.VISIBLE);
    }
}
