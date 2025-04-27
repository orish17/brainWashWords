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

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    public interface OnDefinitionClickListener {
        void onDefinitionClicked(String translation);
    }

    private List<Word> wordList;
    private FirebaseFirestore db;
    private TextToSpeech tts;
    private boolean isTtsReady = false;
    private OnDefinitionClickListener callback;

    public WordAdapter(List<Word> wordList, FirebaseFirestore db, Context context, OnDefinitionClickListener callback) {
        this.wordList = wordList;
        this.db = db;
        this.callback = callback;

        // Initialize TextToSpeech
        this.tts = new TextToSpeech(context, status -> {
            Log.d("TTS", "TTS onInit status: " + status);
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                Log.d("TTS", "TTS setLanguage result: " + result);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true;
                    Log.d("TTS", "TTS is ready!");
                } else {
                    Log.e("TTS", "Language not supported or missing data.");
                }
            } else {
                Log.e("TTS", "Initialization failed.");
            }
        }); // <-- סוגרים פה יפה
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
        holder.wordTextView.setText(word.getWord());

        holder.definitionButton.setOnClickListener(v -> {
            String definition = word.getDefinition();
            if (callback != null && definition != null) {
                callback.onDefinitionClicked(definition);
            }
        });

        holder.speakButton.setOnClickListener(v -> {
            if (!isTtsReady) {
                Toast.makeText(holder.itemView.getContext(), "🔈 Text-to-Speech not ready", Toast.LENGTH_SHORT).show();
                return;
            }
            if (word.getWord() != null) {
                Bundle params = new Bundle();
                params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
                tts.speak(word.getWord(), TextToSpeech.QUEUE_FLUSH, params, "wordID");
            }
        });

        holder.wordCheckBox.setChecked(word.isKnown());
        holder.wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            word.setKnown(isChecked);
            updateWordInFirestore(word);
        });
    }

    private void updateWordInFirestore(Word word) {
        db.collection("groups")
                .document(word.getGroupId())
                .collection("words")
                .document(word.getId())
                .update("known", word.isKnown());
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }

    public void releaseResources() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        TextView wordTextView, definitionTextView;
        Button definitionButton, speakButton;
        CheckBox wordCheckBox;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordTextView = itemView.findViewById(R.id.wordTextView);
            definitionTextView = itemView.findViewById(R.id.definition_text_view);
            definitionButton = itemView.findViewById(R.id.definitionButton);
            speakButton = itemView.findViewById(R.id.speakButton);
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
        }
    }
}
