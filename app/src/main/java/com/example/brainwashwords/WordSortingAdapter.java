package com.example.brainwashwords;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WordSortingAdapter extends RecyclerView.Adapter<WordSortingAdapter.WordViewHolder> {

    private List<Word> wordList;
    private OnWordActionListener listener;
    private List<String> workouts = new ArrayList<>(); // שמירת רשימת Workouts בזיכרון

    public interface OnWordActionListener {
        void onWordMoved(Word word, String newWorkout);
    }

    public WordSortingAdapter(List<Word> wordList, OnWordActionListener listener) {
        this.wordList = wordList;
        this.listener = listener;
        loadWorkouts(); // טען את רשימת ה-Workouts פעם אחת
    }

    private void loadWorkouts() {
        FirebaseFirestore.getInstance().collection("workouts")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        workouts.add(document.getId());
                    }
                    notifyDataSetChanged(); // עדכן את ה-Adapter אם יש נתונים חדשים
                })
                .addOnFailureListener(e -> {
                    // טיפול במקרה של שגיאה בטעינת Workouts
                    workouts.clear();
                });
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_word_item, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.bind(word);
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordText;
        CheckBox wordCheckBox;
        Button showDefinitionButton;
        Spinner workoutSpinner;

        WordViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordTextView);
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
            showDefinitionButton = itemView.findViewById(R.id.definitionButton);
            workoutSpinner = itemView.findViewById(R.id.workoutSpinner);
        }

        void bind(final Word word) {
            wordText.setText(word.getWord());
            wordCheckBox.setChecked(word.isKnown());
            wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> word.setKnown(isChecked));

            showDefinitionButton.setOnClickListener(v -> {
                String definition = word.getDefinition();
                Log.d("Definition Button", "Definition for word: " + definition); // לוג לבדיקה
                if (definition != null && !definition.isEmpty()) {
                    Toast.makeText(itemView.getContext(), "פירוש: " + definition, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(itemView.getContext(), "אין פירוש זמין", Toast.LENGTH_SHORT).show();
                }
            });




            // טען Workouts ל-Spinner רק אם הרשימה זמינה
            if (!workouts.isEmpty()) {
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                        itemView.getContext(),
                        android.R.layout.simple_spinner_item,
                        workouts);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                workoutSpinner.setAdapter(spinnerAdapter);

                // Set current workout selection if it exists
                String currentWorkout = word.getCurrentWorkout();
                if (currentWorkout != null && workouts.contains(currentWorkout)) {
                    int position = workouts.indexOf(currentWorkout);
                    workoutSpinner.setSelection(position);
                }

                workoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedWorkout = workouts.get(position);
                        if (!selectedWorkout.equals(word.getCurrentWorkout())) {
                            listener.onWordMoved(word, selectedWorkout);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        }

    }
}
