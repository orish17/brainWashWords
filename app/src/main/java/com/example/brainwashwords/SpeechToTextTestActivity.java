package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechToTextTestActivity extends BaseActivity {

    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private TextView timerText, resultText, feedbackText;
    private Button playButton, listenButton;
    private String currentWord = "example"; // אפשר לשנות לדינמי מאוחר יותר
    private boolean isTestMode = false;
    private Switch modeSwitch;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text_test);

        timerText = findViewById(R.id.timerText);
        resultText = findViewById(R.id.resultText);
        feedbackText = findViewById(R.id.feedbackText);
        playButton = findViewById(R.id.playButton);
        listenButton = findViewById(R.id.listenButton);
        modeSwitch = findViewById(R.id.modeSwitch);
        setupDrawer();

        setupTTS();
        setupSpeechRecognizer();

        playButton.setOnClickListener(v -> speakWord(currentWord));

        listenButton.setOnClickListener(v -> {
            if (isTestMode) {
                startTimerAndListen();
            } else {
                startListening();
            }
        });

        modeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            timerText.setVisibility(isTestMode ? View.VISIBLE : View.GONE);
            Toast.makeText(this, isTestMode ? "Test Mode Activated" : "Practice Mode Activated", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void speakWord(String word) {
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) {
                feedbackText.setText("Error recognizing speech. Try again.");
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    resultText.setText("You said: " + spokenText);
                    if (spokenText.equalsIgnoreCase(currentWord)) {
                        feedbackText.setText("✅ Correct!");
                    } else {
                        feedbackText.setText("❌ Try again");
                    }
                }
            }

            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startTimerAndListen() {
        timerText.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Speak in: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerText.setText("Listening...");
                startListening();
            }
        }.start();
    }

    private void startListening() {
        timerText.setVisibility(View.GONE);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
        speechRecognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}
