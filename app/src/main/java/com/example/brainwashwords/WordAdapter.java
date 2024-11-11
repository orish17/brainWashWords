package com.example.brainwashwords;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private List<Word> wordList;
    private FirebaseFirestore db;
    private String groupId;

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
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        if (holder.wordTextView != null) {
            holder.wordTextView.setText(word.getWord());
        } else {
            Log.e(TAG, "TextView is null in onBindViewHolder for position: " + position);
        }
    }


    private void updateWordInFirestore(Word word) {
        db.collection("groups").document(groupId)
                .collection("words").document(word.getId())
                .update("known", word.isKnown())
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }


    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;
        CheckBox wordCheckBox;

        public WordViewHolder(View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
        }
    }
}