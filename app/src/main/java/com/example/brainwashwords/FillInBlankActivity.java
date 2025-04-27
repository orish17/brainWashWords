package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FillInBlankActivity extends AppCompatActivity {

    private TextView sentenceText;
    private EditText userInput;
    private Button submitBtn;

    private FirebaseFirestore db;
    private List<Word> wordList = new ArrayList<>();
    private Word currentWord;

    private int score = 0;
    private int totalQuestions = 0;
    private static final int MAX_QUESTIONS = 10;

    private final String apiKey = "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"; // 💬 תכניס כאן את מפתח OpenRouter שלך

    private boolean isWaitingForResponse = false;

    private String workoutName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_blank);

        sentenceText = findViewById(R.id.sentenceText);
        userInput = findViewById(R.id.editAnswer);
        submitBtn = findViewById(R.id.btnSubmit);

        db = FirebaseFirestore.getInstance();

        workoutName = getIntent().getStringExtra("workoutName");
        if (workoutName == null) {
            workoutName = "workout1";
        }

        loadWords(workoutName);

        submitBtn.setOnClickListener(v -> checkAnswer());
    }

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

    private void showNextQuestion() {
        if (isWaitingForResponse) {
            Toast.makeText(this, "Wait for the AI to respond...", Toast.LENGTH_SHORT).show();
            return;
        }

        userInput.setText("");
        Collections.shuffle(wordList);
        currentWord = wordList.get(0);

        generateSentence(currentWord.getWord());
    }

    private void generateSentence(String word) {
        sentenceText.setText("AI is thinking...");
        isWaitingForResponse = true;

        OkHttpClient client = new OkHttpClient();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "mistralai/mistral-7b-instruct");
            // OpenRouter uses this model naming
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
                .addHeader("HTTP-Referer", "https://brainwashwords.com") // חשוב!
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isWaitingForResponse = false;

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> sentenceText.setText("❌ AI Error: " + response.code()));
                    Log.e("AI_ERROR", response.code() + ": " + response.message());
                    return;
                }

                String resStr = response.body().string();
                Log.d("AI_RESPONSE", resStr);

                try {
                    JSONObject json = new JSONObject(resStr);
                    String content = json.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    runOnUiThread(() -> sentenceText.setText(content.trim()));
                } catch (Exception e) {
                    runOnUiThread(() -> sentenceText.setText("Error parsing AI response"));
                    Log.e("AI_PARSE_ERROR", e.getMessage());
                }
            }
        });
    }

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

    private void showResult() {
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total", totalQuestions);
        startActivity(intent);
        finish();
    }
}
