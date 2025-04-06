package com.example.brainwashwords;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class words extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private List<Word> wordList;
    private FirebaseFirestore db;

    // תיבת הפירוש
    private LinearLayout definitionContainer;
    private TextView definitionTextView;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // התחלת רשימות ו-Firebase
        wordList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // הגדרת התיבה לתרגום
        definitionContainer = findViewById(R.id.definition_container);
        definitionTextView = findViewById(R.id.definition_text_view);

        // טעינת מילים
        loadWordsFromFirebase();

        // הגדרת האדפטר
        wordAdapter = new WordAdapter(wordList, db, this, this::showTranslation);
        recyclerView.setAdapter(wordAdapter);
    }

    private void loadWordsFromFirebase() {
        db.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String wordText = document.getString("word");
                        String definition = document.getString("definition");
                        boolean isKnown = document.getBoolean("known") != null && document.getBoolean("known");

                        if (wordText != null) {
                            wordList.add(new Word(wordText, isKnown, definition, document.getId(), "workout1"));
                        }
                    }
                    wordAdapter.notifyDataSetChanged();
                });
    }

    private void showTranslation(String translation) {
        definitionTextView.setText(translation);
        definitionContainer.setVisibility(View.VISIBLE);

        // הסתרה אחרי 5 שניות
        handler.removeCallbacksAndMessages(null); // ביטול הופעות קודמות
        handler.postDelayed(() -> definitionContainer.setVisibility(View.GONE), 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wordAdapter != null) {
            wordAdapter.releaseResources();
        }
    }
}
