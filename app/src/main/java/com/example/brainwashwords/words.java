package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class words extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private List<Word> wordList;
    private ProgressBar progressBar;

    private static final String TAG = "WordsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize word list
        wordList = new ArrayList<>();

        // Setup RecyclerView
        setupRecyclerView();

        // Load words
        String groupId = getIntent().getStringExtra("groupId");
        if (groupId == null) {
            groupId = "workout1"; // Default fallback
        }
        loadWords(groupId);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        wordAdapter = new WordAdapter(wordList, db, "workout1");
        recyclerView.setAdapter(wordAdapter);
    }

    private void loadWords(String groupId) {
        showLoading(true);

        db.collection("groups").document(groupId).collection("words")
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);

                    if (task.isSuccessful() && task.getResult() != null) {
                        wordList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String word = document.getString("word");
                            boolean known = document.getBoolean("known") != null ?
                                    document.getBoolean("known") : false;
                            String definition = document.getString("definition") != null ?
                                    document.getString("definition") : "No definition available";

                            if (word == null || word.isEmpty()) {
                                Log.e(TAG, "Skipping document with missing 'word' field: " + document.getId());
                                continue;
                            }

                            wordList.add(new Word(word, known, definition, document.getId(), groupId));

                        }
                        wordAdapter.notifyDataSetChanged();

                        // Show empty state if needed
                        showEmptyState(wordList.isEmpty());
                    } else {
                        Log.e(TAG, "Error fetching words: ", task.getException());
                        Toast.makeText(this, "Error loading words.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState(boolean show) {
        View emptyView = findViewById(R.id.emptyView);
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (recyclerView != null) {
                recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        }
    }
}
