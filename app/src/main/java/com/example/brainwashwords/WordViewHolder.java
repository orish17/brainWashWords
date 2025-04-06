package com.example.brainwashwords;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WordViewHolder extends RecyclerView.ViewHolder {
    TextView wordTextView;
    Button speakButton;
    Button definitionButton; // הוסף את זה

    public WordViewHolder(@NonNull View itemView) {
        super(itemView);
        wordTextView = itemView.findViewById(R.id.wordTextView);
        speakButton = itemView.findViewById(R.id.speakButton);
        definitionButton = itemView.findViewById(R.id.definitionButton); // הגדר אותו כאן
    }
}

