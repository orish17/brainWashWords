package com.example.brainwashwords;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
    private LinearLayout definitionContainer;
    private TextView definitionText;
    private FirebaseFirestore db;
    private List<Word> wordList;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        // התחברות לרכיבי UI
        recyclerView = findViewById(R.id.recyclerView);
        definitionContainer = findViewById(R.id.definition_container);
        definitionText = findViewById(R.id.definition_text_view);

        db = FirebaseFirestore.getInstance();
        wordList = new ArrayList<>();

        setupRecyclerView();
        fetchWords();
    }

    private void setupRecyclerView() {
        WordAdapter adapter = new WordAdapter(wordList, this, this::showDefinition, db);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void fetchWords() {
        db.collection("groups").document("workout1").collection("words")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        wordList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Word word = document.toObject(Word.class);
                            wordList.add(word);
                        }
                        recyclerView.getAdapter().notifyDataSetChanged();
                        Log.d("FirestoreDebug", "Words loaded: " + wordList.size());
                    } else {
                        Log.e("FirestoreDebug", "Error fetching words: ", task.getException());
                    }
                });
    }


    private void showDefinition(String definition) {
        definitionText.setText(definition); // הצגת הפירוש
        definitionContainer.setVisibility(View.VISIBLE); // הצגת התיבה
        handler.postDelayed(() -> definitionContainer.setVisibility(View.GONE), 5000); // הסתרת התיבה
    }
}
