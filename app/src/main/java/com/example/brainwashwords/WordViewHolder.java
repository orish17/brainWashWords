package com.example.brainwashwords;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

class WordViewHolder extends RecyclerView.ViewHolder {
    TextView wordText;
    CheckBox wordCheckBox;
    Button showDefinitionButton;

    WordViewHolder(View itemView) {
        super(itemView);
        wordText = itemView.findViewById(R.id.wordTextView);
        wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
        showDefinitionButton = itemView.findViewById(R.id.definitionButton); // חיבור הכפתור
    }
}
