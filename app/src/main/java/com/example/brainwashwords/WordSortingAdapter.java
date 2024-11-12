package com.example.brainwashwords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WordSortingAdapter extends RecyclerView.Adapter<WordSortingAdapter.WordViewHolder> {

    private List<Word> wordList;
    private OnWordActionListener listener;

    // ממשק לפעולות מיוחדות (אם נדרש)
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
                .inflate(R.layout.activity_word_item, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position);

        // מציג את המילה
        holder.wordText.setText(word.getWord());

        // לוגיקה עבור CheckBox
        holder.wordCheckBox.setChecked(word.isKnown());
        holder.wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setKnown(isChecked); // מעדכן את הסטטוס של המילה
        });

        // לוגיקה עבור כפתור להצגת פירוש
        holder.showDefinitionButton.setOnClickListener(v -> {
            String definition = word.getDefinition(); // פירוש המילה
            if (definition != null && !definition.isEmpty()) {
                Toast.makeText(holder.itemView.getContext(), "פירוש: " + definition, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(holder.itemView.getContext(), "אין פירוש זמין", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    // מחלקת ViewHolder
    class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordText;
        CheckBox wordCheckBox;
        Button showDefinitionButton;

        WordViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordTextView);
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
            showDefinitionButton = itemView.findViewById(R.id.definitionButton); // מזהה הכפתור
        }
    }
}
