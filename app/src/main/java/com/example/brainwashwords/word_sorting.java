package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_sorting);

        db = FirebaseFirestore.getInstance();
        wordList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WordAdapter(wordList, db, "defaultGroup");
        recyclerView.setAdapter(adapter);

        loadWords();
    }

    private void loadWords() {
        db.collection("groups").document("defaultGroup").collection("words")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String wordText = document.getString("word");
                        String definition = document.getString("definition");
                        boolean isKnown = document.getBoolean("known") != null && document.getBoolean("known");
                        wordList.add(new Word(wordText, isKnown, definition, document.getId(), "defaultGroup"));
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
