package com.example.brainwashwords; // מציין שהמחלקה הזו שייכת לחבילת הפרויקט בשם הזה. מאפשר לקוד להישאר מאורגן היטב.

import android.content.SharedPreferences; // מאפשר אחסון נתונים פשוטים (כמו מזהה משתמש) באופן מקומי במכשיר, בין פתיחות של האפליקציה.
import android.os.Bundle; // מחלקה שמכילה מידע שמועבר בין מסכים (Activities), בעיקר בעת יצירתם.
import android.os.CountDownTimer; // טיימר שמספק פונקציה לספירה לאחור עם מרווחים קבועים, שימושי ליצירת מגבלת זמן.
import android.util.Log; // מאפשר רישום הודעות לוג לצורכי דיבוג (debugging).
import android.view.View; // מחלקת בסיס לכל רכיבי התצוגה, מאפשרת שליטה על נראות ולחיצות.
import android.widget.*; // כולל את כל רכיבי התצוגה הבסיסיים (כפתורים, טקסטים, תיבות קלט ועוד) מבלי לייבא כל אחד בנפרד.

import androidx.appcompat.app.AppCompatActivity; // מחלקת Activity בסיסית עם תמיכה ב־ActionBar ותאימות לאחור בגרסאות ישנות.

import org.json.*; // מספק מחלקות לעבודה עם פורמט JSON – נחוץ לשליחת וקליטת נתונים עם שרת AI.

import java.io.IOException; // מאפשר טיפול בשגיאות קלט/פלט, לדוגמה כאשר בקשת רשת נכשלת.

import okhttp3.*; // ספריית צד שלישי חזקה לשליחת בקשות רשת HTTP, כולל POST ו־GET.

/**
 * מחלקת AiQuizActivity מייצגת מסך שבו המשתמש מתמודד עם מבחן או תרגול המבוסס על השלמת מילה חסרה במשפט.
 * המשפט נוצר בזמן אמת על ידי AI, והמשתמש צריך להשלים את המילה החסרה.
 * תומך בשני מצבים: תרגול (ללא בדיקת תשובות) ומבחן (כולל טיימר וניקוד).
 */
public class AiQuizActivity extends BaseActivity { // מחלקת האקטיביטי הזו מרחיבה את BaseActivity, כלומר תומכת גם בניווט צד כמו שאר מסכי האפליקציה.

    // משתנים שמייצגים רכיבי ממשק גרפי שונים
    private TextView aiSentenceText, resultText, timerText; // aiSentenceText מציג את המשפט עם המילה החסרה, resultText מציג את התוצאה, timerText מציג את הזמן שנותר.
    private EditText userInput; // שדה שבו המשתמש מזין את המילה החסרה.
    private Button checkAnswerButton, nextQuestionButton; // כפתורים ללחיצה – בדיקת תשובה או מעבר לשאלה הבאה.
    private Switch modeSwitch; // מתג שמאפשר למשתמש לעבור בין מצב תרגול למבחן.

    // משתנים לוגיים ונתונים פנימיים
    private String correctAnswer = ""; // משתנה שמחזיק את המילה הנכונה כפי שהתקבלה מה־AI.
    private boolean isWaitingForResponse = false; // האם כרגע ממתינים לתגובה מה־AI, כדי למנוע שליחת בקשות כפולות.
    private boolean isTestMode = false; // מציין האם המשתמש במצב מבחן (עם טיימר וניקוד) או במצב תרגול (חופשי).
    private CountDownTimer countDownTimer; // אובייקט המייצג את הטיימר בפועל – מופעל רק במצב מבחן.

    private final String apiKey = "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"; // 🔐 מפתח API אישי עבור חיבור למודל OpenRouter (צריך להישמר באופן מאובטח במציאות).

    // משתנים לניקוד ומעקב
    private int score = 0; // סופר את מספר התשובות הנכונות של המשתמש במהלך המבחן הנוכחי.
    private int totalQuestions = 0; // סופר כמה שאלות נשאלו עד כה.
    private static final int MAX_QUESTIONS = 10; // הגבלה על מספר השאלות במבחן – לאחר 10 שאלות מסתיים המבחן.

    @Override
    protected void onCreate(Bundle savedInstanceState) { // מתבצעת בעת פתיחת המסך. כאן אנחנו מאתחלים הכל.
        ThemeHelper.applySavedTheme(this); // מחיל את ערכת הצבעים שנשמרה לפי בחירת המשתמש (מצב כהה/בהיר).
        super.onCreate(savedInstanceState); // קריאה ל־onCreate של מחלקת העל.
        setContentView(R.layout.activity_ai_quiz); // קישור בין קובץ ה־XML לבין מחלקת Java זו – זה מה שמצייר את ממשק המשתמש.

        // חיבורים בין רכיבי UI לבין המשתנים בג'אווה
        aiSentenceText = findViewById(R.id.aiSentenceText); // מחבר את שדה הטקסט שמציג את המשפט מה־AI
        userInput = findViewById(R.id.userInput); // מחבר את שדה הקלט שבו המשתמש מזין את תשובתו
        checkAnswerButton = findViewById(R.id.checkAnswerButton); // מחבר את כפתור בדיקת התשובה
        resultText = findViewById(R.id.resultText); // מחבר את תיבת התוצאה – נכונה/לא נכונה
        nextQuestionButton = findViewById(R.id.nextQuestionButton); // מחבר את כפתור "הבא"
        modeSwitch = findViewById(R.id.modeSwitch); // מחבר את מתג הבחירה בין מצב תרגול למבחן
        timerText = findViewById(R.id.timerText); // מחבר את תצוגת הטיימר
        setupDrawer(); // מפעיל את תפריט הצד שירשנו מ־BaseActivity

        loadNewQuestion(); // מתחיל עם שאלה ראשונה מה־AI

        // מגדיר מה יקרה כשמשנים את מצב המתג (תרגול / מבחן)
        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked; // מעדכן את מצב המבחן לפי מצב המתג
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE); // מציג את הטיימר רק במצב מבחן
            Toast.makeText(this,
                    isTestMode ? "Test Mode Activated" : "Practice Mode Activated",
                    Toast.LENGTH_SHORT).show(); // מציג הודעה קצרה בהתאם למצב
        });

        // בעת לחיצה על כפתור בדיקת תשובה
        checkAnswerButton.setOnClickListener(v -> checkAnswer());

        // בעת לחיצה על כפתור הבא
        nextQuestionButton.setOnClickListener(v -> {
            if (!isWaitingForResponse) {
                loadNewQuestion(); // טוען שאלה חדשה
            } else {
                Toast.makeText(this, "Please wait for the AI to respond...", Toast.LENGTH_SHORT).show(); // מונע שליחה כפולה
            }
        });
    }


    /**
     * טוען שאלה חדשה ע"י שליחת בקשה למנוע ה־AI דרך OpenRouter.
     * מחלק את התשובה לשני חלקים: המשפט עצמו והמילה החסרה.
     */
    private void loadNewQuestion() {
        aiSentenceText.setText("AI is thinking..."); // מציג למשתמש הודעה זמנית בזמן טעינה
        userInput.setText(""); // מנקה את שדה הקלט
        resultText.setVisibility(View.GONE); // מסתיר את תוצאת השאלה הקודמת
        timerText.setVisibility(View.GONE); // מסתיר את הטיימר בינתיים

        if (countDownTimer != null) countDownTimer.cancel(); // עוצר טיימר קודם אם היה פעיל

        if (apiKey == null || apiKey.isEmpty()) { // בדיקה אם המפתח קיים
            Toast.makeText(this, "⚠️ API Key missing. Check your configuration.", Toast.LENGTH_LONG).show(); // הודעת שגיאה
            aiSentenceText.setText("Missing API key."); // מציג הודעת שגיאה
            return; // לא ממשיך בבקשה
        }

        isWaitingForResponse = true; // מסמן שלא ניתן לשלוח בקשה חדשה עדיין
        nextQuestionButton.setEnabled(false); // מבטל זמנית את כפתור 'הבא'

        OkHttpClient client = new OkHttpClient(); // יוצר לקוח רשת לשליחת הבקשה

        JSONObject jsonBody = new JSONObject(); // גוף JSON לבקשה
        try {
            jsonBody.put("model", "mistralai/mistral-7b-instruct"); // קובע את המודל של AI

            JSONArray messages = new JSONArray(); // מערך של הודעות (צ'אט)
            JSONObject message = new JSONObject(); // הודעת המשתמש
            message.put("role", "user"); // תפקיד ההודעה בצ'אט
            message.put("content", "Create an English sentence with a missing word. Write the sentence, then in a new line write 'Missing word:' and specify the missing word."); // הבקשה שלנו ל־AI
            messages.put(message); // מוסיף את ההודעה למערך
            jsonBody.put("messages", messages); // שם את המערך בגוף
        } catch (JSONException e) {
            e.printStackTrace(); // מדפיס שגיאת JSON
            return; // לא שולח בקשה
        }

        // בונה את הבקשה לשרת OpenRouter עם כל ההדרים הרלוונטיים
        Request request = new Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions") // כתובת ה-API
                .addHeader("Authorization", "Bearer " + apiKey) // הוספת מפתח
                .addHeader("Content-Type", "application/json") // מציין שהתוכן הוא JSON
                .addHeader("HTTP-Referer", "https://brainwashwords.com") // מקור הבקשה
                .addHeader("X-Title", "BrainWashWords App") // כותרת הבקשה
                .post(RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"))) // גוף הבקשה בפורמט JSON
                .build();

        // שליחת הבקשה באופן אסינכרוני (ברקע)
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    aiSentenceText.setText("Failed to connect to AI."); // הודעת שגיאה
                    Toast.makeText(AiQuizActivity.this, "❌ Network error", Toast.LENGTH_SHORT).show(); // הודעה
                    isWaitingForResponse = false; // מאפשר שוב שליחה
                    nextQuestionButton.setEnabled(true); // מאפשר לעבור שאלה
                });
                e.printStackTrace(); // מדפיס את השגיאה
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseData = response.body().string(); // קורא את תגובת השרת
                Log.d("AI_RESPONSE", responseData); // מדפיס את התגובה ללוג לצורך דיבוג

                runOnUiThread(() -> {
                    isWaitingForResponse = false; // מסמן שהתקבלה תשובה
                    nextQuestionButton.setEnabled(true); // מפעיל מחדש את הכפתור

                    if (!response.isSuccessful()) { // אם התגובה נכשלה
                        aiSentenceText.setText("Error: " + response.code()); // מציג קוד שגיאה
                        Toast.makeText(AiQuizActivity.this, "❌ AI server error: " + response.code(), Toast.LENGTH_SHORT).show(); // הודעה
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseData); // ממיר את המחרוזת ל־JSON
                        JSONArray choices = json.getJSONArray("choices"); // לוקח את מערך האפשרויות
                        JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message"); // מוציא את ההודעה מתוכו
                        String content = messageObj.getString("content"); // שולף את הטקסט של ההודעה

                        String[] parts = content.split("Missing word:"); // מפצל לשני חלקים: השאלה והתשובה
                        if (parts.length == 2) {
                            String question = parts[0].trim(); // החלק של המשפט
                            correctAnswer = parts[1].trim(); // המילה החסרה
                            aiSentenceText.setText(question); // מציג את השאלה

                            if (isTestMode) startCountdownTimer(); // אם במבחן, מפעיל טיימר
                        } else {
                            aiSentenceText.setText("Invalid AI response."); // אם לא קיבלנו פורמט נכון
                        }
                    } catch (JSONException e) {
                        aiSentenceText.setText("Failed to parse AI response."); // שגיאה בפענוח
                        Log.e("AI_PARSE_ERROR", e.getMessage()); // שגיאה בלוג
                    }
                });
            }
        });
    }

    /**
     * מפעיל טיימר של 15 שניות לשאלה אחת בלבד. אם הזמן נגמר – מפסיד את השאלה.
     */
    private void startCountdownTimer() {
        timerText.setVisibility(View.VISIBLE); // מציג את הטיימר
        timerText.setText("Time left: 15"); // התחלה מ־15 שניות

        countDownTimer = new CountDownTimer(15000, 1000) { // ספירה לאחור 15 שניות, בקפיצות של שנייה
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time left: " + millisUntilFinished / 1000); // עדכון זמן שנותר
            }

            public void onFinish() {
                String userAnswer = userInput.getText().toString().trim(); // קלט המשתמש
                if (userAnswer.isEmpty()) { // אם לא הספיק לענות
                    totalQuestions++; // מוסיף שאלה לספירה
                    resultText.setText("❌ Time's up! The correct answer was: " + correctAnswer); // מציג את התשובה הנכונה
                    resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // צבע אדום
                    resultText.setVisibility(View.VISIBLE); // מציג את תיבת התוצאה
                    checkIfEnd(); // בודק אם סיימנו את המבחן
                }
            }
        }.start(); // התחלת הטיימר
    }

    /**
     * בודק את התשובה של המשתמש מול התשובה הנכונה.
     */
    private void checkAnswer() {
        String userAnswer = userInput.getText().toString().trim(); // שולף את תשובת המשתמש
        if (countDownTimer != null) countDownTimer.cancel(); // מבטל את הטיימר אם היה פעיל

        if (!isTestMode) { // אם במצב תרגול
            Toast.makeText(this, "Practice Mode: Answer not checked", Toast.LENGTH_SHORT).show(); // לא בודק תשובה
            return;
        }

        totalQuestions++; // מעלה את מספר השאלות שנענו

        if (userAnswer.equalsIgnoreCase(correctAnswer)) { // אם התשובה נכונה (לא תלוי באותיות גדולות/קטנות)
            score++; // מוסיף נקודה
            resultText.setText("✔️ Correct!"); // טקסט חיובי
            resultText.setTextColor(getResources().getColor(android.R.color.holo_green_dark)); // צבע ירוק
        } else {
            resultText.setText("❌ Incorrect. The missing word was: " + correctAnswer); // מציג תשובה נכונה
            resultText.setTextColor(getResources().getColor(android.R.color.holo_red_dark)); // צבע אדום
        }

        resultText.setVisibility(View.VISIBLE); // מציג את התוצאה
        checkIfEnd(); // בודק האם המבחן הסתיים
    }

    /**
     * אם הגענו למספר השאלות המרבי – מציג תוצאה, שומר אותה, ומאפס את הספירה.
     */
    private void checkIfEnd() {
        if (totalQuestions >= MAX_QUESTIONS) { // האם סיימנו 10 שאלות
            float successRate = ((float) score / totalQuestions) * 100f; // מחשב אחוז הצלחה
            FirebaseUtils.saveTestResult(this, "AiTest", successRate); // שומר את התוצאה ב־Firebase תחת הקטגוריה 'AiTest'

            Toast.makeText(this, "Test completed! Score: " + score + "/" + totalQuestions, Toast.LENGTH_LONG).show(); // הודעה עם ציון
            score = 0; // מאפס את הניקוד
            totalQuestions = 0; // מאפס את מספר השאלות
        }

        // מעלה שאלה חדשה לאחר השהייה של 2 שניות כדי לאפשר למשתמש לקרוא את הפידבק
        new android.os.Handler().postDelayed(this::loadNewQuestion, 2000);
    }

    /**
     * מנקה את הטיימר מהזיכרון אם האקטיביטי נהרס – כדי למנוע נזילת זיכרון.
     */
    @Override
    protected void onDestroy() {
        if (countDownTimer != null) countDownTimer.cancel(); // מבטל את הטיימר
        super.onDestroy(); // ממשיך בהתנהגות הרגילה של onDestroy
    }
}

