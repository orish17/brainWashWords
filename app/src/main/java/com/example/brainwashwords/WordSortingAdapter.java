package com.example.brainwashwords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WordSortingAdapter extends RecyclerView.Adapter<WordSortingAdapter.WordViewHolder> {

    private List<Word> wordList;
    private FirebaseFirestore db;
    private String groupId;

    public WordSortingAdapter(List<Word> wordList, FirebaseFirestore db, String groupId) {
        this.wordList = wordList;
        this.db = db;
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_word_item, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.wordText.setText(word.getWord());

        // Add additional logic for Spinner if needed
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordText;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordTextView);
        }
    }
}
