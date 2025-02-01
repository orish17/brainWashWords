package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;

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
    private FirebaseFirestore db;
    private List<Word> wordList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_sorting);

        recyclerView = findViewById(R.id.recyclerView);
        db = FirebaseFirestore.getInstance();
        wordList = new ArrayList<>();

        setupRecyclerView();
        fetchWords();
    }

    private void setupRecyclerView() {
        adapter = new WordAdapter(wordList, this, this::showDefinition, db);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchWords() {
        db.collection("words").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                wordList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Word word = document.toObject(Word.class);
                    wordList.add(word);
                }
                Log.d("FirebaseDebug", "Loaded words: " + wordList.size());
                adapter.notifyDataSetChanged();
            } else {
                Log.e("FirebaseDebug", "Error fetching words: ", task.getException());
            }
        });
    }


    private void showDefinition(String definition) {
        Log.d("WordSorting", "Definition: " + definition);
        // הוסף כאן לוגיקה להצגת הפירוש בממשק המשתמש
    }
}
