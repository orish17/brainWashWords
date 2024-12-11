package com.example.brainwashwords;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private static final String TAG = "WordAdapter";
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_word_item, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);
        holder.wordTextView.setText(word.getWord());
        holder.wordCheckBox.setChecked(word.isKnown());

        // עדכון המילה ב-Firebase כאשר CheckBox משתנה
        holder.wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setKnown(isChecked);
            updateWordInFirestore(word);
        });

        // טיפול בלחיצה על כפתור הצגת הפירוש
        holder.definitionButton.setOnClickListener(v -> {
            // יצירת עותק חדש של הפירוש
            final String finalDefinition = word.getDefinition();

            Log.d(TAG, "Definition for word: " + word.getWord() + ", Definition: " + finalDefinition);

            Activity activity = (Activity) holder.itemView.getContext();
            TextView definitionTextView = activity.findViewById(R.id.definition_text_view);
            LinearLayout definitionContainer = activity.findViewById(R.id.definition_container);

            if (finalDefinition == null || finalDefinition.isEmpty()) {
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "No definition available", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            if (definitionTextView != null && definitionContainer != null) {
                activity.runOnUiThread(() -> {
                    definitionTextView.setText(finalDefinition);
                    definitionContainer.setVisibility(View.VISIBLE);
                });

                new Handler().postDelayed(() -> {
                    activity.runOnUiThread(() -> definitionContainer.setVisibility(View.GONE));
                }, 5000);
            } else {
                Log.e(TAG, "Definition TextView or Container not found in layout.");
            }
        });




    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    private void updateWordInFirestore(Word word) {
        db.collection("groups")
                .document(groupId)
                .collection("words")
                .document(word.getId())
                .update("known", word.isKnown())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Word updated successfully: " + word.getWord()))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating word: " + word.getWord(), e));
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView;
        CheckBox wordCheckBox;
        Button definitionButton;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
            definitionButton = itemView.findViewById(R.id.definitionButton);
        }
    }
}
