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

    /**
     * Listener for when the definition button is clicked.
     */
    public interface OnDefinitionClickListener {
        void onDefinitionClicked(String translation);
    }

    /**
     * Listener for when the checkbox (known/unknown) is changed.
     */
    public interface OnCheckboxChangedListener {
        void onCheckboxChanged(Word word, boolean isChecked);
    }

    private List<Word> wordList;
    private TextToSpeech tts;
    private boolean isTtsReady = false;

    private OnDefinitionClickListener definitionCallback;
    private OnCheckboxChangedListener checkboxCallback;

    /**
     * Constructor for WordAdapter.
     *
     * @param wordList           List of words to display
     * @param definitionCallback Callback for definition click
     * @param checkboxCallback   Callback for checkbox change
     */
    public WordAdapter(List<Word> wordList,
                       OnDefinitionClickListener definitionCallback,
                       OnCheckboxChangedListener checkboxCallback) {
        this.wordList = wordList;
        this.definitionCallback = definitionCallback;
        this.checkboxCallback = checkboxCallback;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יוצרים View חדש מתוך קובץ ה־XML שמייצג כל פריט ברשימת המילים
        // המטרה: להציג עיצוב אחיד לכל מילה שנשלפת מתוך הרשימה
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_word_item, parent, false);

        // אתחול של מנוע Text-to-Speech כדי לאפשר למשתמש לשמוע את המילים
        // המטרה: להפוך את חוויית הלימוד לאודיטורית ולא רק ויזואלית
        Context context = parent.getContext();
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // הגדרת שפת הדיבור – אנגלית אמריקאית
                // המטרה: לוודא שהמילה תבוטא באופן נכון וברור
                int result = tts.setLanguage(Locale.US);
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
            } else {
                // אם יש שגיאה באתחול, כותבים ללוג
                Log.e("TTS", "Initialization failed.");
            }
        });

        // מחזירים ViewHolder עם הרכיבים של המילה הנוכחית
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        // שולפים את האובייקט Word לפי המיקום ברשימה
        // המטרה: לקבל את המילה שצריך להציג למשתמש
        Word word = wordList.get(position);

        // מציגים את המילה על המסך בתיבת הטקסט
        // המטרה: שהמשתמש יראה את המילה שהוא לומד
        holder.wordTextView.setText(word.getWord());

        // כפתור "definition" – בעת לחיצה תשלח את הפירוש דרך callback
        // המטרה: לאפשר למשתמש להבין את משמעות המילה בלחיצה
        holder.definitionButton.setOnClickListener(v -> {
            String definition = word.getDefinition();
            if (definitionCallback != null && definition != null) {
                // שולחים את ההגדרה חזרה למסך הראשי להצגה
                definitionCallback.onDefinitionClicked(definition);
            }
        });

        // כפתור השמעה – קורא בקול את המילה בעזרת TTS
        // המטרה: לעזור למשתמש לשמוע את ההגייה הנכונה של המילה
        holder.speakButton.setOnClickListener(v -> {
            if (!isTtsReady) {
                // אם TTS לא מוכן – נציג הודעה מתאימה
                Toast.makeText(holder.itemView.getContext(), "🔈 Text-to-Speech not ready", Toast.LENGTH_SHORT).show();
                return;
            }
            if (word.getWord() != null) {
                // הגדרת פרמטרים להשמעה – כולל עוצמת קול
                Bundle params = new Bundle();
                params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
                // השמעת המילה
                tts.speak(word.getWord(), TextToSpeech.QUEUE_FLUSH, params, "wordID");
            }
        });

        // קובע אם תיבת הסימון מסומנת לפי המידע מאובייקט המילה
        // המטרה: לשקף אם המשתמש כבר סימן את המילה כ"מוכרת"
        holder.wordCheckBox.setChecked(word.isKnown());

        // מאזין לשינוי ב־checkbox – מעדכן את ה־Word ושולח callback
        // המטרה: לשמור את הבחירה של המשתמש (אם המילה מוכרת או לא)
        holder.wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setKnown(isChecked);
            if (checkboxCallback != null) {
                checkboxCallback.onCheckboxChanged(word, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        // מחזיר את מספר המילים ברשימה
        // המטרה: לקבוע כמה פריטים יוצגו ב־RecyclerView
        return wordList.size();
    }

    /**
     * סוגר את מנוע ה־TTS כדי לחסוך משאבים כשלא בשימוש.
     * המטרה: מניעת דליפת זיכרון ושימוש מיותר במנוע הדיבור
     */
    public void releaseResources() {
        if (tts != null) {
            tts.stop();      // עוצר דיבור פעיל
            tts.shutdown();  // משחרר את המשאב לגמרי
        }
    }

    /**
     * ViewHolder שמחזיק את כל הרכיבים של כל פריט ברשימת המילים.
     * המטרה: לקשר בין הקוד ל־Views במסך של כל מילה
     */
    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView, definitionTextView;
        Button definitionButton, speakButton;
        CheckBox wordCheckBox;

        /**
         * מאחסן הפניות ל־Views מתוך layout של כל פריט.
         *
         * @param itemView התצוגה של הפריט
         */
        public WordViewHolder(@NonNull View itemView) {
            super(itemView);

            // רכיב להצגת המילה עצמה
            wordTextView = itemView.findViewById(R.id.wordTextView);

            // רכיב אופציונלי להצגת הפירוש בתחתית (אם משתמשים בו)
            definitionTextView = itemView.findViewById(R.id.definition_text_view);

            // כפתור להצגת פירוש
            definitionButton = itemView.findViewById(R.id.definitionButton);

            // כפתור להשמעת המילה בקול
            speakButton = itemView.findViewById(R.id.speakButton);

            // תיבת סימון לסימון המילה כ"מוכרת"
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
        }
    }
}
