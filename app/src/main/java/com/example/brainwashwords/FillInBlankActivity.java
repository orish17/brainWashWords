package com.example.brainwashwords; // מציין שהמחלקה שייכת לחבילת הפרויקט הראשית

import android.content.Intent; // מאפשר מעבר בין מסכים (Activities)
import android.os.Bundle; // אובייקט להעברת מידע בין מחזורי חיים של האקטיביטי
import android.os.CountDownTimer; // טיימר שמבצע ספירה לאחור
import android.util.Log; // מאפשר כתיבת הודעות דיבוג ליומן
import android.view.View; // מחלקת בסיס לרכיבי UI
import android.widget.*; // כולל טקסטים, כפתורים, תיבות קלט וכו'

import androidx.appcompat.app.AppCompatActivity; // מחלקת בסיס למסכי Android עם תמיכה ב־Toolbar

import com.google.firebase.firestore.*; // עבודה עם Firebase Firestore (מסד נתונים בענן)

import org.json.*; // עבודה עם פורמט JSON

import java.io.IOException; // טיפול בשגיאות IO
import java.util.*; // כולל רשימות וכלי עזר (כמו Collections)

import okhttp3.*; // ספרייה לשליחת בקשות רשת (API)

/**
 * FillInBlankActivity – מבחן השלמה עם מילה חסרה.
 * נשלפת מילה שסומנה כ־known, וה־AI יוצר עליה משפט עם מקום ריק.
 * כולל טיימר, ניקוד, שמירת תוצאה.
 */
public class FillInBlankActivity extends BaseActivity {

    // רכיבי ממשק משתמש
    private TextView sentenceText, timerText; // טקסט להצגת המשפט והטיימר
    private EditText userInput; // קלט מהמשתמש
    private Button submitBtn; // כפתור שליחה
    private Switch modeSwitch; // מתג בין מצב מבחן לתרגול

    // חיבור למסד הנתונים
    private FirebaseFirestore db;
    private List<Word> wordList = new ArrayList<>(); // רשימת מילים מתוך הקבוצה
    private Word currentWord; // המילה הנוכחית לשאלה

    // ניקוד
    private int score = 0;
    private int totalQuestions = 0;
    private static final int MAX_QUESTIONS = 10; // סך שאלות למבחן

    private final String apiKey = "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"; // מפתח חיבור ל־OpenRouter (להחלפה)

    private boolean isWaitingForResponse = false; // אם ממתין לתשובת AI
    private boolean isTestMode = false; // האם מופעל מצב מבחן
    private CountDownTimer countDownTimer; // הטיימר לפעולה

    private String workoutName; // שם הקבוצה שממנה נשלפות מילים

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // מפעיל מצב תאורה לפי ההעדפות
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_blank); // טוען את עיצוב המסך מה־XML

        // קישור רכיבי UI
        sentenceText = findViewById(R.id.sentenceText);
        userInput = findViewById(R.id.editAnswer);
        submitBtn = findViewById(R.id.btnSubmit);
        modeSwitch = findViewById(R.id.modeSwitch);
        timerText = findViewById(R.id.timerText);
        setupDrawer(); // תפריט צד

        db = FirebaseFirestore.getInstance(); // התחברות ל־Firestore

        // קבלת שם הקבוצה מהמבחן (אם לא נשלח – נבחר workout1)
        workoutName = getIntent().getStringExtra("workoutName");
        if (workoutName == null) workoutName = "workout1";

        // מעבר בין מצב מבחן לתרגול
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE); // מציג טיימר רק במצב מבחן
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show();
        });

        // טוען את המילים מהקבוצה שנבחרה
        loadWords(workoutName);

        // מאזין ללחיצה על כפתור שליחה
        submitBtn.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel(); // מבטל טיימר אם פעיל
            timerText.setVisibility(View.GONE); // מסתיר את הטיימר
            checkAnswer(); // בודק את התשובה
        });
    }

    // טוען את המילים שסומנו כ־known מתוך הקבוצה
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
                        finish(); // סוגר את המסך אם אין מספיק מילים
                    } else {
                        showNextQuestion(); // מתחיל את המבחן
                    }
                });
    }

    // מציג שאלה חדשה מתוך מילה אקראית
    private void showNextQuestion() {
        if (isWaitingForResponse) {
            Toast.makeText(this, "Wait for the AI to respond...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setVisibility(View.GONE);
        userInput.setText(""); // איפוס קלט

        Collections.shuffle(wordList); // ערבוב מילים
        currentWord = wordList.get(0); // בחירת מילה אקראית
        generateSentence(currentWord.getWord()); // שולח ל־AI ליצירת משפט

        if (isTestMode) startTimer(); // אם במבחן – טיימר
    }

    // טיימר של 15 שניות למענה
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
                        showResult(); // סיום
                    } else {
                        showNextQuestion(); // שאלה חדשה
                    }
                }
            }
        }.start();
    }

    // שולח בקשה ל־AI שייצור משפט עם מקום ריק עבור מילה
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

        // שליחת הבקשה ברקע
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

    // בדיקת תשובת המשתמש מול המילה המקורית
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

    // מציג תוצאה, שומר ל־Firebase, ועובר למסך תוצאה
    private void showResult() {
        float successRate = ((float) score / totalQuestions) * 100f;
        FirebaseUtils.saveTestResult(this, "FillInBlank", successRate);

        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("total", totalQuestions);
        startActivity(intent);
        finish(); // סוגר את המסך הנוכחי
    }

    // מנקה טיימר כשעוזבים את המסך
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel();
        super.onDestroy();
    }
}
