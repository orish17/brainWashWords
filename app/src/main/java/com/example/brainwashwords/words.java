package com.example.brainwashwords;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.util.Log;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_words);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        String groupId = getIntent().getStringExtra("groupId");

        // טוען את המילים מקבוצה מסוימת
        db.collection("workout1_id").document("workout1_id/NynTcSM0VDGksozyTz4Y").collection("words")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Word> wordList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String word = document.getString("word");
                            boolean known = document.getBoolean("known") != null ? document.getBoolean("known") : false;
                            wordList.add(new Word(word, known, document.getId()));
                        }
                        wordAdapter = new WordAdapter(wordList, db, groupId);
                        recyclerView.setAdapter(wordAdapter);
                    } else {

                        Log.e("FirebaseError", "Error fetching words: " + task.getException());
                    }
                });
    }
}