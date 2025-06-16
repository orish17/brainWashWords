package com.example.brainwashwords;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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

import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying a list of words in a RecyclerView.
 * Each word item includes options to see the definition, hear pronunciation, and mark as known.
 */
public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    /** Listener for when the definition button is clicked. */
    public interface OnDefinitionClickListener {
        void onDefinitionClicked(String translation); // מאזין להצגת פירוש
    }

    /** Listener for when the checkbox (known/unknown) is changed. */
    public interface OnCheckboxChangedListener {
        void onCheckboxChanged(Word word, boolean isChecked); // מאזין לשינוי checkbox
    }

    private List<Word> wordList; // רשימת המילים
    private TextToSpeech tts; // מנוע טקסט לדיבור
    private boolean isTtsReady = false; // דגל האם TTS מוכן

    private OnDefinitionClickListener definitionCallback; // callback עבור פירוש
    private OnCheckboxChangedListener checkboxCallback; // callback עבור checkbox

    /**
     * Constructor for WordAdapter.
     */
    public WordAdapter(List<Word> wordList,
                       OnDefinitionClickListener definitionCallback,
                       OnCheckboxChangedListener checkboxCallback) {
        this.wordList = wordList; // אתחול רשימת מילים
        this.definitionCallback = definitionCallback; // אתחול callback לפירוש
        this.checkboxCallback = checkboxCallback; // אתחול callback ל-checkbox
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_word_item, parent, false); // יצירת תצוגה ממבנה XML

        Context context = parent.getContext(); // קונטקסט
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US); // קביעת שפה
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED; // בדיקה אם השפה נתמכת
            } else {
                Log.e("TTS", "Initialization failed."); // שגיאת אתחול TTS
            }
        });

        return new WordViewHolder(view); // מחזיר ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = wordList.get(position); // קבלת מילה לפי מיקום
        holder.wordTextView.setText(word.getWord()); // הצגת המילה על המסך

        holder.definitionButton.setOnClickListener(v -> {
            String definition = word.getDefinition(); // קבלת פירוש
            if (definitionCallback != null && definition != null) {
                definitionCallback.onDefinitionClicked(definition); // שליחת הפירוש לקליינט
            }
        });

        holder.speakButton.setOnClickListener(v -> {
            if (!isTtsReady) {
                Toast.makeText(holder.itemView.getContext(), "🔈 Text-to-Speech not ready", Toast.LENGTH_SHORT).show(); // TTS לא מוכן
                return;
            }
            if (word.getWord() != null) {
                Bundle params = new Bundle();
                params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f); // עוצמת קול
                tts.speak(word.getWord(), TextToSpeech.QUEUE_FLUSH, params, "wordID"); // השמעת מילה
            }
        });

        holder.wordCheckBox.setChecked(word.isKnown()); // עדכון מצב סימון תיבה

        holder.wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setKnown(isChecked); // עדכון אובייקט המילה
            if (checkboxCallback != null) {
                checkboxCallback.onCheckboxChanged(word, isChecked); // שליחת callback
            }
        });
    }

    @Override
    public int getItemCount() {
        return wordList.size(); // מספר פריטים ברשימה
    }

    /**
     * סוגר את מנוע ה־TTS כדי לחסוך משאבים כשלא בשימוש.
     */
    public void releaseResources() {
        if (tts != null) {
            tts.stop(); // עצירת דיבור
            tts.shutdown(); // סגירת מנוע
        }
    }

    /**
     * ViewHolder שמחזיק את כל הרכיבים של כל פריט ברשימת המילים.
     */
    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView, definitionTextView; // תיבות טקסט למילה ולפירוש
        Button definitionButton, speakButton; // כפתורים לפירוש והשמעה
        CheckBox wordCheckBox; // תיבת סימון אם המילה מוכרת

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView); // הצגת מילה
            definitionTextView = itemView.findViewById(R.id.definition_text_view); // טקסט לפירוש
            definitionButton = itemView.findViewById(R.id.definitionButton); // כפתור פירוש
            speakButton = itemView.findViewById(R.id.speakButton); // כפתור שמיעה
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox); // checkbox
        }
    }
}
