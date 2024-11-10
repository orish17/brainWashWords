package com.example.brainwashwords;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WordSortingAdapter extends RecyclerView.Adapter<WordSortingAdapter.WordViewHolder> {
    private List<Word> wordList;
    private OnWordActionListener listener;

    public interface OnWordActionListener {
        void onWordMoved(Word word, String newWorkout);
    }

    public WordSortingAdapter(List<Word> wordList, OnWordActionListener listener) {
        this.wordList = wordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_sorting, parent, false);
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
        Spinner workoutSpinner;

        WordViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordText);
            workoutSpinner = itemView.findViewById(R.id.workoutSpinner);
        }

        void bind(final Word word) {
            wordText.setText(word.getWord());

            // Load available workouts into spinner
            FirebaseFirestore.getInstance().collection("workouts")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<String> workouts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            workouts.add(document.getId());
                        }

                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                                itemView.getContext(),
                                android.R.layout.simple_spinner_item,
                                workouts);
                        spinnerAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item);
                        workoutSpinner.setAdapter(spinnerAdapter);

                        // Set current workout selection if it exists
                        String currentWorkout = word.getCurrentWorkout();
                        if (currentWorkout != null && workouts.contains(currentWorkout)) {
                            int position = workouts.indexOf(currentWorkout);
                            workoutSpinner.setSelection(position);
                        }

                        workoutSpinner.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent,
                                                               View view,
                                                               int position,
                                                               long id) {
                                        String selectedWorkout = workouts.get(position);
                                        String currentWorkout = word.getCurrentWorkout();
                                        if (currentWorkout == null || !selectedWorkout.equals(currentWorkout)) {
                                            listener.onWordMoved(word, selectedWorkout);
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {}
                                });
                    });
        }
    }
}