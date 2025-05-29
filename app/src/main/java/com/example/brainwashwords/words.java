package com.example.brainwashwords;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class words extends BaseActivity {

    private RecyclerView recyclerView;
    private WordAdapter wordAdapter;
    private List<Word> wordList;

    private LinearLayout definitionContainer;
    private TextView definitionTextView;
    private Handler handler = new Handler();

    private String workoutName;
    private String userId;

    private FirebaseFirestore firestore;
    private FirebaseDatabase realtimeDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);
        setupDrawer();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("uid", null);
        if (userId == null) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        workoutName = getIntent().getStringExtra("workoutName");
        if (workoutName == null || workoutName.isEmpty()) {
            workoutName = "workout1";
        }

        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        wordList = new ArrayList<>();
        wordAdapter = new WordAdapter(wordList, this::showTranslation, this::onCheckboxChanged);
        recyclerView.setAdapter(wordAdapter);

        definitionContainer = findViewById(R.id.definition_container);
        definitionTextView = findViewById(R.id.definition_text_view);

        loadWords();

        Button doneButton = findViewById(R.id.btnDoneSortin);
        doneButton.setOnClickListener(v -> {
            Toast.makeText(this, "Sorting saved!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void loadWords() {
        CollectionReference wordsRef = firestore.collection("groups").document(workoutName).collection("words");
        DatabaseReference knownRef = realtimeDb.getReference("users")
                .child(userId)
                .child("knownWords")
                .child(workoutName); // ⬅️ נכון יותר

        wordsRef.get().addOnSuccessListener(wordSnapshot -> {
            knownRef.get().addOnSuccessListener(knownSnapshot -> {
                Map<String, Boolean> knownMap = new HashMap<>();
                for (DataSnapshot snap : knownSnapshot.getChildren()) {
                    knownMap.put(snap.getKey(), snap.getValue(Boolean.class));
                }

                wordList.clear();
                for (QueryDocumentSnapshot doc : wordSnapshot) {
                    String id = doc.getId();
                    String word = doc.getString("word");
                    String definition = doc.getString("definition");
                    boolean isKnown = knownMap.getOrDefault(id, false);

                    wordList.add(new Word(word, isKnown, definition, id, workoutName));
                }

                wordAdapter.notifyDataSetChanged();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load words", Toast.LENGTH_SHORT).show();
        });
    }


    private void onCheckboxChanged(Word word, boolean isChecked) {
        DatabaseReference ref = realtimeDb.getReference("users")
                .child(userId)
                .child("knownWords")
                .child(word.getGroupId()) // הקבוצה
                .child(word.getId());     // המילה
        ref.setValue(isChecked);
    }


    private void showTranslation(String translation) {
        definitionTextView.setText(translation);
        definitionContainer.setVisibility(View.VISIBLE);
        handler.removeCallbacksAndMessages(null);
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
