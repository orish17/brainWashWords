package com.example.brainwashwords;

import android.content.SharedPreferences; // לשמירת מידע על המשתמש
import android.os.Bundle; // לניהול מצב האקטיביטי
import android.os.Handler; // להפעלה מושהית של פעולות
import android.view.View; // הצגת רכיבים על המסך
import android.widget.Button; // כפתור
import android.widget.LinearLayout; // תיבת תצוגה של אלמנטים בטור
import android.widget.TextView; // הצגת טקסט
import android.widget.Toast; // הודעות קופצות

import androidx.recyclerview.widget.LinearLayoutManager; // ניהול תצוגה אנכית של רשימה
import androidx.recyclerview.widget.RecyclerView; // תצוגת רשימה מתקדמת

import com.google.firebase.database.*; // מסד נתונים בזמן אמת
import com.google.firebase.firestore.*; // מסד נתונים Firestore

import java.util.*; // עבור רשימות ומפות

public class words extends BaseActivity {

    private RecyclerView recyclerView; // תצוגת רשימת מילים
    private WordAdapter wordAdapter; // מתאם להצגת מילים
    private List<Word> wordList; // רשימת המילים עצמן

    private LinearLayout definitionContainer; // תיבה לתצוגת פירוש
    private TextView definitionTextView; // טקסט של הפירוש
    private Handler handler = new Handler(); // להפעלה מושהית של הסתרת פירוש

    private String workoutName; // שם קבוצת המילים
    private String userId; // מזהה המשתמש המחובר

    private FirebaseFirestore firestore; // גישה למסד Firestore
    private FirebaseDatabase realtimeDb; // גישה ל־Realtime Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this); // החלת מצב כהה/בהיר
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words); // הצגת תצוגת המסך
        setupDrawer(); // הצגת תפריט צד

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE); // טעינת מזהה משתמש
        userId = prefs.getString("uid", null); // קבלת UID
        if (userId == null) { // אם אין UID
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show(); // הודעה על שגיאה
            finish(); // יציאה מהמסך
            return;
        }

        workoutName = getIntent().getStringExtra("workoutName"); // טעינת שם קבוצת מילים מה-intent
        if (workoutName == null || workoutName.isEmpty()) {
            workoutName = "workout1"; // ברירת מחדל
        }

        firestore = FirebaseFirestore.getInstance(); // התחברות ל־Firestore
        realtimeDb = FirebaseDatabase.getInstance(); // התחברות ל־Realtime

        recyclerView = findViewById(R.id.recyclerView); // קישור לרשימת המילים
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // תצוגה אנכית

        wordList = new ArrayList<>(); // אתחול רשימת מילים ריקה
        wordAdapter = new WordAdapter(wordList, this::showTranslation, this::onCheckboxChanged); // יצירת מתאם עם callbacks
        recyclerView.setAdapter(wordAdapter); // הצגת המילים על המסך

        definitionContainer = findViewById(R.id.definition_container); // תיבה לתצוגת פירוש
        definitionTextView = findViewById(R.id.definition_text_view); // טקסט להצגת הפירוש

        loadWords(); // טעינת המילים מהמסד

        Button doneButton = findViewById(R.id.btnDoneSortin); // כפתור סיום מיון
        doneButton.setOnClickListener(v -> {
            Toast.makeText(this, "Sorting saved!", Toast.LENGTH_SHORT).show(); // הודעת הצלחה
            finish(); // יציאה מהמסך
        });
    }

    // טוען את כל המילים מהמסד לפי הקבוצה שנבחרה
    private void loadWords() {
        CollectionReference wordsRef = firestore.collection("groups").document(workoutName).collection("words");
        DatabaseReference knownRef = realtimeDb.getReference("users")
                .child(userId)
                .child("knownWords")
                .child(workoutName); // מיקום הנתונים בריל־טיים

        wordsRef.get().addOnSuccessListener(wordSnapshot -> {
            knownRef.get().addOnSuccessListener(knownSnapshot -> {
                Map<String, Boolean> knownMap = new HashMap<>(); // מפת מילות known
                for (DataSnapshot snap : knownSnapshot.getChildren()) {
                    knownMap.put(snap.getKey(), snap.getValue(Boolean.class)); // מילוי מפה עם known
                }

                wordList.clear(); // ניקוי רשימה קיימת
                for (QueryDocumentSnapshot doc : wordSnapshot) {
                    String id = doc.getId(); // מזהה מילה
                    String word = doc.getString("word"); // הטקסט עצמו
                    String definition = doc.getString("definition"); // פירוש
                    boolean isKnown = knownMap.getOrDefault(id, false); // האם מוכרת

                    wordList.add(new Word(word, isKnown, definition, id, workoutName)); // הוספה לרשימה
                }

                wordAdapter.notifyDataSetChanged(); // רענון תצוגה
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load words", Toast.LENGTH_SHORT).show(); // כשל בטעינה
        });
    }

    // נקרא כשהמשתמש מסמן או מבטל סימון של מילה כ־known
    private void onCheckboxChanged(Word word, boolean isChecked) {
        DatabaseReference ref = realtimeDb.getReference("users")
                .child(userId)
                .child("knownWords")
                .child(word.getGroupId())
                .child(word.getId());
        ref.setValue(isChecked); // שמירת הסימון במסד
    }

    // מציג את הפירוש של מילה ומעלים אותו לאחר 5 שניות
    private void showTranslation(String translation) {
        definitionTextView.setText(translation); // הצגת הטקסט
        definitionContainer.setVisibility(View.VISIBLE); // הצגת התיבה
        handler.removeCallbacksAndMessages(null); // ביטול פעולות קודמות
        handler.postDelayed(() -> definitionContainer.setVisibility(View.GONE), 5000); // הסתרה לאחר 5 שניות
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wordAdapter != null) {
            wordAdapter.releaseResources(); // שחרור TTS כשנכנסים למסך אחר
        }
    }
}
