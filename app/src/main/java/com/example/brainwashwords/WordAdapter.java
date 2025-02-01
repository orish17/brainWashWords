package com.example.brainwashwords;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {

    private List<Word> wordList;
    private OnDefinitionClickListener listener;
    private FirebaseFirestore db;
    private TextToSpeech tts;
    private boolean isTtsInitialized = false;

    public WordAdapter(List<Word> wordList, Context context, OnDefinitionClickListener listener, FirebaseFirestore db) {
        this.wordList = wordList;
        this.listener = listener;
        this.db = db;

        // Initialize Text-to-Speech
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported.");
                } else {
                    isTtsInitialized = true;
                    Log.d("TTS", "Text-to-Speech initialized successfully.");
                }
            } else {
                Log.e("TTS", "Initialization failed. Status code: " + status);
            }
        });
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
        holder.definitionButton.setOnClickListener(v -> listener.onDefinitionClick(word.getDefinition()));

        // Update word status in Firebase when checkbox is clicked
        holder.wordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (word.getGroupId() != null && word.getId() != null) {
                db.collection("groups")
                        .document(word.getGroupId())
                        .collection("words")
                        .document(word.getId())
                        .update("known", isChecked)
                        .addOnSuccessListener(aVoid -> Log.d("FirestoreDebug", "Update successful"))
                        .addOnFailureListener(e -> Log.e("FirestoreDebug", "Error updating document", e));
            } else {
                Log.e("FirestoreDebug", "Group ID or Word ID is null. Word: " + word.getWord());
            }
        });

        // Handle speak button click
        holder.speakButton.setOnClickListener(v -> {
            if (isTtsInitialized && tts != null && word.getWord() != null && !word.getWord().isEmpty()) {
                int result = tts.speak(word.getWord(), TextToSpeech.QUEUE_FLUSH, null, null);
                if (result == TextToSpeech.ERROR) {
                    Log.e("TTS", "Error while speaking.");
                }
            } else {
                if (!isTtsInitialized) {
                    Log.e("TTS", "Text-to-Speech is not initialized.");
                } else if (tts == null) {
                    Log.e("TTS", "Text-to-Speech engine is null.");
                } else {
                    Log.e("TTS", "Invalid word text.");
                }
            }
        });
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

    public static class WordViewHolder extends RecyclerView.ViewHolder {

        private TextView wordText;
        private Button definitionButton;
        private Button speakButton;
        private CheckBox wordCheckBox;

        public WordViewHolder(@NonNull View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordTextView);
            definitionButton = itemView.findViewById(R.id.definitionButton);
            speakButton = itemView.findViewById(R.id.speakButton);
            wordCheckBox = itemView.findViewById(R.id.wordCheckBox);
        }
    }

    public interface OnDefinitionClickListener {
        void onDefinitionClick(String definition);
    }
}
