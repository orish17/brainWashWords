package com.example.brainwashwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WordSortingActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private WordAdapter adapter;
    private List<Word> wordList = new ArrayList<>();
    private FirebaseFirestore db;

    private String workoutName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);

        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_word_sorting);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        setupDrawer();

        workoutName = getIntent().getStringExtra("workoutName");
        if (workoutName == null) {
            workoutName = "workout1"; // ברירת מחדל
        }

        loadWords(workoutName);
    }

    private void loadWords(String workoutName) {
        db.collection("groups").document(workoutName).collection("words")
                .get()
                .addOnSuccessListener(query -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        String wordText = doc.getString("word");
                        String definition = doc.getString("definition");
                        Boolean known = doc.getBoolean("known");

                        if (wordText != null) {
                            wordList.add(new Word(wordText, known != null && known, definition, doc.getId(), workoutName));
                        }
                    }

                    if (wordList.isEmpty()) {
                        Toast.makeText(this, "No words available for this workout.", Toast.LENGTH_LONG).show();
                    }

                    adapter = new WordAdapter(wordList, db, this, translation -> {
                        Toast.makeText(WordSortingActivity.this, "Definition: " + translation, Toast.LENGTH_SHORT).show();
                    });


                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load words.", Toast.LENGTH_SHORT).show();
                });
    }
}
