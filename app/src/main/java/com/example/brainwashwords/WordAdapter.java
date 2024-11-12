package com.example.brainwashwords;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private List<Word> wordList;
    private FirebaseFirestore db;
    private String groupId;
    private Context context;

    public WordAdapter(List<Word> wordList, FirebaseFirestore db, String groupId) {
        this.wordList = wordList;
        this.db = db;
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_word_item, parent, false);
        context = parent.getContext();
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);

        // מציג את שם המילה
        holder.wordTextView.setText(word.getWord());

        // עדכון ה-CheckBox בהתאם למצב המילה
        holder.wordCheckBox.setChecked(word.isKnown());

        // לוגיקה עבור CheckBox
        holder.wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setKnown(isChecked);
            updateWordInFirestore(word);
        });

        // לוגיקה עבור כפתור להצגת הפירוש
        holder.definitionButton.setOnClickListener(v -> {
            String definition = word.getDefinition();
            Log.d("DefinitionDebug", "Definition for " + word.getWord() + ": " + definition);
            if (definition != null && !definition.isEmpty()) {
                Toast.makeText(context, "Definition: " + definition, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No definition available.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWordInFirestore(Word word) {
        db.collection("groups").document(groupId)
                .collection("words").document(word.getId())
                .update("known", word.isKnown())
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update Firestore", e));
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;
        CheckBox wordCheckBox;
        Button definitionButton;

        public WordViewHolder(View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
            definitionButton = itemView.findViewById(R.id.definitionButton);
        }
    }
}
