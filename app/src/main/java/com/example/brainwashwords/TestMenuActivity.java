package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * TestMenuActivity displays the main menu for selecting the type of quiz/test.
 * It includes validation to ensure that the user has marked at least 4 known words before proceeding.
 */
public class TestMenuActivity extends BaseActivity {

    // כפתורים לכל אחד מסוגי המבחנים
    Button btnMultipleChoice, btnFillInBlank, btnAudioTest, btnAiQuiz, btnStt;

    // חיבור למסד הנתונים של Firebase
    private FirebaseFirestore db;

    // משתנה עזר שמכיל את מספר המילים המסומנות כ-"known"
    private int knownWordsCount = 0;

    /**
     * Called when the activity is starting.
     * Sets up UI elements, drawer navigation, and listeners for test selection.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // החלת מצב תאורה נבחר (כהה/בהיר)
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_menu);
        setupDrawer(); // תפריט צד

        // השמת כפתורים לפי מזהי ה־XML
        btnMultipleChoice = findViewById(R.id.btnMultipleChoice);
        btnFillInBlank = findViewById(R.id.btnFillInBlank);
        btnAudioTest = findViewById(R.id.btnAudioTest);
        btnAiQuiz = findViewById(R.id.btnAiQuiz);
        btnStt = findViewById(R.id.btnStt);

        db = FirebaseFirestore.getInstance(); // אתחול גישה ל-Firestore

        /**
         * Listener שמופעל כשנלחץ כפתור מבחן.
         * קודם בודק האם יש לפחות 4 מילים מסומנות כ-known.
         * אם כן, מפנה למסך המבחן המתאים.
         */
        View.OnClickListener protectedClickListener = view -> {
            // אפקט אנימציה לחיצה
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_click_scale);
            view.startAnimation(anim);

            // טעינת מילים מ-Firebase ובדיקת כמה מסומנות כ-known
            db.collection("groups").document("workout1").collection("words")
                    .get()
                    .addOnSuccessListener(result -> {
                        int count = 0;
                        for (QueryDocumentSnapshot doc : result) {
                            Boolean known = doc.getBoolean("known");
                            if (Boolean.TRUE.equals(known)) {
                                count++;
                            }
                        }

                        // פחות מ־4 מילים = לא מאפשר מבחן
                        if (count < 4) {
                            Toast.makeText(this, "You must mark at least 4 known words before taking a test!", Toast.LENGTH_LONG).show();
                        } else {
                            // זיהוי סוג המבחן לפי מזהה הכפתור
                            Class<?> activityClass = null;
                            int viewId = view.getId();

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

                            // אם מצאנו אקטיביטי מתאים – מפעילים אותו
                            if (activityClass != null) {
                                startActivity(new Intent(this, activityClass));
                            }
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error checking known words!", Toast.LENGTH_SHORT).show()
                    );
        };

        // השמת הליסטנר לכל כפתור מבחן
        btnMultipleChoice.setOnClickListener(protectedClickListener);
        btnFillInBlank.setOnClickListener(protectedClickListener);
        btnAudioTest.setOnClickListener(protectedClickListener);
        btnAiQuiz.setOnClickListener(protectedClickListener);
        btnStt.setOnClickListener(protectedClickListener);
    }
}
