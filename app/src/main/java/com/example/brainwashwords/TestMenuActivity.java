package com.example.brainwashwords; // הגדרת שם החבילה

// ייבוא מחלקות של אנדרואיד לצורך אינטנט, עיצוב, אנימציות ותצוגה
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity; // פעילות בסיסית

// ייבוא Firestore לשליפת מילים
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * TestMenuActivity מציג את תפריט הבחירה של סוג מבחן.
 * מוודא שהמשתמש סימן לפחות 4 מילים כ־known לפני שמאפשר להתחיל מבחן.
 */
public class TestMenuActivity extends BaseActivity { // ירושה מ־BaseActivity לצורך תפריט צד

    // הגדרת כפתורים לכל סוג מבחן
    Button btnMultipleChoice, btnFillInBlank, btnAudioTest, btnAiQuiz, btnStt;

    // אובייקט התחברות למסד הנתונים Firestore
    private FirebaseFirestore db;

    // משתנה עזר לזיהוי כמה מילים מסומנות כ־known
    private int knownWordsCount = 0;

    /**
     * נקרא כאשר המסך נוצר (onCreate).
     * מבצע אתחול עיצוב, קישור כפתורים, ותפריט צד.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // החלת ערכת עיצוב (כהה/בהיר)
        super.onCreate(savedInstanceState); // קריאה למחלקת־אם
        setContentView(R.layout.activity_test_menu); // הגדרת קובץ XML כ־layout
        setupDrawer(); // הפעלת תפריט הצד

        // קישור כל כפתור למזהה שלו בקובץ XML
        btnMultipleChoice = findViewById(R.id.btnMultipleChoice); // כפתור מבחן רב־ברירה
        btnFillInBlank = findViewById(R.id.btnFillInBlank);       // כפתור מבחן השלמה
        btnAudioTest = findViewById(R.id.btnAudioTest);           // כפתור מבחן שמיעה
        btnAiQuiz = findViewById(R.id.btnAiQuiz);                 // כפתור מבחן AI
        btnStt = findViewById(R.id.btnStt);                       // כפתור מבחן דיבור

        db = FirebaseFirestore.getInstance(); // אתחול חיבור למסד הנתונים Firestore

        // Listener אחיד לכל כפתור מבחן – עם בדיקה מוקדמת
        View.OnClickListener protectedClickListener = view -> {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_click_scale); // אנימציית לחיצה
            view.startAnimation(anim); // הפעלת האנימציה

            // שליפת כל המילים מהקבוצה workout1
            db.collection("groups").document("workout1").collection("words")
                    .get()
                    .addOnSuccessListener(result -> {
                        int count = 0; // מונה מילים מסומנות
                        for (QueryDocumentSnapshot doc : result) {
                            Boolean known = doc.getBoolean("known"); // שליפת ערך known
                            if (Boolean.TRUE.equals(known)) {
                                count++; // אם סומן – העלה את המונה
                            }
                        }

                        // בדיקה אם יש לפחות 4 מילים ידועות
                        if (count < 4) {
                            Toast.makeText(this,
                                    "You must mark at least 4 known words before taking a test!",
                                    Toast.LENGTH_LONG).show(); // הצגת הודעת שגיאה
                        } else {
                            // התחלת המבחן לפי סוג הכפתור שנלחץ
                            Class<?> activityClass = null; // מחלקה שנפעיל

                            int viewId = view.getId(); // מזהה הכפתור

                            // התאמה בין מזהה הכפתור למחלקת המבחן
                            if (viewId == R.id.btnMultipleChoice)
                                activityClass = MultipleChoiceActivity.class;
                            else if (viewId == R.id.btnFillInBlank)
                                activityClass = FillInBlankActivity.class;
                            else if (viewId == R.id.btnAudioTest)
                                activityClass = AudioTestActivity.class;
                            else if (viewId == R.id.btnAiQuiz)
                                activityClass = AiQuizActivity.class;
                            else if (viewId == R.id.btnStt)
                                activityClass = SpeechToTextTestActivity.class;

                            // אם זוהתה פעילות מתאימה – להפעיל אותה
                            if (activityClass != null) {
                                startActivity(new Intent(this, activityClass)); // מעבר למסך מבחן
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Error checking known words!", Toast.LENGTH_SHORT).show()); // שגיאה בגישה למסד הנתונים
        };

        // שיוך ה־listener לכל כפתור מבחן
        btnMultipleChoice.setOnClickListener(protectedClickListener);
        btnFillInBlank.setOnClickListener(protectedClickListener);
        btnAudioTest.setOnClickListener(protectedClickListener);
        btnAiQuiz.setOnClickListener(protectedClickListener);
        btnStt.setOnClickListener(protectedClickListener);
    }
}
