package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    private Button doneButton;

    private LinearLayout definitionContainer;
    private TextView definitionTextView;
    private android.os.Handler handler = new android.os.Handler();

    private String workoutId; // נוסיף משתנה לזיהוי הקבוצה שנבחרה

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        wordList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // קבלת ה-workoutId שנשלח מהמסך הקודם
        workoutId = getIntent().getStringExtra("WORKOUT_ID");
        if (workoutId == null || workoutId.isEmpty()) {
            workoutId = "workout1"; // ברירת מחדל
        }

        definitionContainer = findViewById(R.id.definition_container);
        definitionTextView = findViewById(R.id.definition_text_view);

        loadWordsFromFirebase();

        wordAdapter = new WordAdapter(wordList, db, this, this::showTranslation);
        recyclerView.setAdapter(wordAdapter);

        Button doneButton = findViewById(R.id.btnDoneSorting);
        doneButton.setOnClickListener(v -> {
            Toast.makeText(this, "Sorting saved! You can now take a test.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, home.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadWordsFromFirebase() {
        db.collection("groups").document(workoutId).collection("words")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    wordList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String wordText = document.getString("word");
                        String definition = document.getString("definition");
                        boolean isKnown = document.getBoolean("known") != null && document.getBoolean("known");

                        if (wordText != null) {
                            wordList.add(new Word(wordText, isKnown, definition, document.getId(), workoutId));
                        }
                    }
                    wordAdapter.notifyDataSetChanged();
                });
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
