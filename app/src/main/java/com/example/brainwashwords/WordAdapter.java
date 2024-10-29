package com.example.brainwashwords;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private List<Word> wordList;
    private FirebaseFirestore db;
    private String groupId;

    // Constructor
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
        Word currentWord = wordList.get(position);

        // בדיקה אם המילה תקינה
        if (currentWord != null && currentWord.getWord() != null) {
            holder.wordTextView.setText(currentWord.getWord());
        } else {
            holder.wordTextView.setText("Unknown word");
        }

        // בדיקה אם הסטטוס ידוע או לא
        holder.knownCheckBox.setChecked(currentWord != null && currentWord.isKnown());

        // האזנה לשינוי סטטוס
        holder.knownCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentWord != null && currentWord.getId() != null) {
                // עדכון הסטטוס ב-Firestore
                db.collection("groups").document(groupId)
                        .collection("words").document(currentWord.getId())
                        .update("known", isChecked)
                        .addOnSuccessListener(aVoid -> {
                            // הצלחה בעדכון
                        })
                        .addOnFailureListener(e -> {
                            // טיפול בשגיאה
                            e.printStackTrace();
                        });
            }
        });

        // טיפול ב-imageView וב-textView נוספים אם הם בשימוש
        holder.workoutNum.setText("Word #" + (position + 1)); // לדוגמה למספר סדרתי
        holder.imageView.setImageResource(R.drawable.workout_1); // הצב תמונה ברירת מחדל (התאם את שם התמונה למקור שלך)
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        public TextView wordTextView;
        public CheckBox knownCheckBox;
        public ImageView imageView;
        public TextView workoutNum;

        public WordViewHolder(View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            knownCheckBox = itemView.findViewById(R.id.knownCheckBox);
            imageView = itemView.findViewById(R.id.imageView5);
        }
    }
}
