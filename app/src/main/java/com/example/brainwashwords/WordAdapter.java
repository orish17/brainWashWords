package com.example.brainwashwords;

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
                .inflate(R.layout.item_word_sorting, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.wordText.setText(word.getWord());
        holder.knownCheckBox.setChecked(word.isKnown());

        holder.knownCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setKnown(isChecked);
            updateWordInFirestore(word);
        });
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

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordText;
        CheckBox knownCheckBox;

        WordViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordText);
            knownCheckBox = itemView.findViewById(R.id.knownCheckBox);
        }
    }
}