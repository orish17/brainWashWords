package com.example.brainwashwords;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class word_sorting extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private WordSortingAdapter adapter;
    private List<Word> wordList;
    private String currentWorkout;
    private Spinner workoutSpinner;
    private static final String TAG = "WordSortingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_sorting);

        // Initialize Firestore and UI elements
        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        workoutSpinner = findViewById(R.id.workoutSpinner);
        wordList = new ArrayList<>();

        setupRecyclerView();
        loadWorkouts();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WordSortingAdapter(wordList, (word, newWorkout) -> moveWordToWorkout(word, newWorkout));
        recyclerView.setAdapter(adapter);
    }

    private void loadWorkouts() {
        db.collection("groups")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> workouts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        workouts.add(document.getId());
                    }

                    if (!workouts.isEmpty()) {
                        setupWorkoutSpinner(workouts);
                        currentWorkout = workouts.get(0);
                        loadWordsForWorkout(currentWorkout);
                    } else {
                        Toast.makeText(this, "No workouts found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading workouts", e);
                    Toast.makeText(this, "Error loading workouts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupWorkoutSpinner(List<String> workouts) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, workouts);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        workoutSpinner.setAdapter(spinnerAdapter);

        workoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWorkout = workouts.get(position);
                if (!selectedWorkout.equals(currentWorkout)) {
                    currentWorkout = selectedWorkout;
                    loadWordsForWorkout(currentWorkout);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadWordsForWorkout(String workoutId) {
        Log.d(TAG, "Loading words for workout: " + workoutId);
        wordList.clear();

        db.collection("groups").document(workoutId).collection("words")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // לוג לכל המסמך
                        Log.d("Firestore", "Loaded document data: " + document.getData());

                        // טוען את המילה והפירוש
                        String wordText = document.getString("word");
                        String definition = document.getString("definition");
                        boolean known = document.getBoolean("known") != null ? document.getBoolean("known") : false;

                        Log.d(TAG, "Loaded word: " + wordText + ", definition: " + definition);

                        if (wordText == null || wordText.isEmpty()) {
                            Log.d(TAG, "Word field is missing or empty for document: " + document.getId());
                            continue;
                        }

                        // יוצר את אובייקט ה-Word
                        Word word = new Word(wordText, known, definition, document.getId(), workoutId);
                        Log.d(TAG, "Word object created: " + word.getWord() + ", definition: " + word.getDefinition());
                        wordList.add(word);
                    }

                    adapter.notifyDataSetChanged();
                    if (wordList.isEmpty()) {
                        Toast.makeText(this, "No words in this workout", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading words for workout: " + workoutId, e);
                    Toast.makeText(this, "Error loading words: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void moveWordToWorkout(Word word, String newWorkout) {
        Map<String, Object> wordData = new HashMap<>();
        wordData.put("word", word.getWord());
        wordData.put("known", word.isKnown());

        db.collection("groups")
                .document(word.getCurrentWorkout())
                .collection("words")
                .document(word.getId())
                .delete()
                .addOnSuccessListener(aVoid -> db.collection("groups")
                        .document(newWorkout)
                        .collection("words")
                        .add(wordData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Word moved successfully", Toast.LENGTH_SHORT).show();
                            loadWordsForWorkout(currentWorkout);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error adding word to new workout", e);
                            Toast.makeText(this, "Error moving word: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting word from current workout", e);
                    Toast.makeText(this, "Error moving word: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
