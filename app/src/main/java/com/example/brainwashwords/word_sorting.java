package com.example.brainwashwords;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class word_sorting extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private List<Word> wordList;
    private FirebaseFirestore db;
    private TextView translationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_sorting);

        translationView = findViewById(R.id.definition_text_view);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        wordList = new ArrayList<>();
        loadWordsFromFirebase();

        adapter = new WordAdapter(wordList, db, this, this::showTranslation);
        recyclerView.setAdapter(adapter);
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
                    adapter.notifyDataSetChanged();
                });
    }

    private void showTranslation(String translation) {
        translationView.setText(translation);
        translationView.setVisibility(View.VISIBLE);

        // הסתרת התרגום לאחר 5 שניות
        new Handler().postDelayed(() -> translationView.setVisibility(View.GONE), 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.releaseResources();
        }
    }
}
