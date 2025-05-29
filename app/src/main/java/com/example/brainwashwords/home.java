package com.example.brainwashwords;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.database.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class home extends BaseActivity {

    private AppCompatButton button, testYourself;
    private TextView usernameDisplay;
    private DatabaseReference usersRef;
    private String username;

    private boolean isSorted = false;
    private AlertDialog.Builder builder;

    private ProgressBar progressBar;
    private TextView progressText;
    private String userId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applySavedTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firestore = FirebaseFirestore.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getString("uid", null);
        username = prefs.getString("username", "User");

        initializeViews();
        setupAlertDialog();
        setupDrawer();

        if (username != null) {
            usernameDisplay.setText("Welcome, " + username);
            loadUsernameFromFirebase();
        }

        updateProgressBar();
        setupClickListeners();
    }

    private void initializeViews() {
        usernameDisplay = findViewById(R.id.usernameTextView3);
        testYourself = findViewById(R.id.button1);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        button = findViewById(R.id.button);
    }

    private void setupAlertDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Wait a minute");
        builder.setMessage("You have to sort the words first");
        builder.setNegativeButton("OK, got it!", (dialog, which) -> dialog.cancel());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfSorted();
        updateProgressBar();
    }

    private void checkIfSorted() {
        if (userId == null) return;

        DatabaseReference knownRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("knownWords");

        knownRef.get().addOnSuccessListener(snapshot -> {
            int knownCount = 0;
            for (DataSnapshot groupSnap : snapshot.getChildren()) {
                for (DataSnapshot wordSnap : groupSnap.getChildren()) {
                    Boolean value = wordSnap.getValue(Boolean.class);
                    if (Boolean.TRUE.equals(value)) knownCount++;
                }
            }
            isSorted = knownCount >= 4;
            updateTestButtonState(isSorted);
        });
    }

    private void updateTestButtonState(boolean enabled) {
        testYourself.setEnabled(enabled);
        testYourself.setTextColor(enabled ? Color.WHITE : Color.GRAY);
        if (enabled) {
            testYourself.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() ->
                            testYourself.animate().scaleX(1f).scaleY(1f).setDuration(150));
        }
    }

    private void setupClickListeners() {
        button.setOnClickListener(v -> startActivity(new Intent(home.this, group_selection.class)));
        testYourself.setOnClickListener(v -> {
            if (!isSorted) {
                builder.show();
            } else {
                startActivity(new Intent(home.this, TestMenuActivity.class));
            }
        });
    }

    private void loadUsernameFromFirebase() {
        usersRef.orderByChild("name").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                User user = userSnapshot.getValue(User.class);
                                if (user != null) {
                                    String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getName();
                                    usernameDisplay.setText("Welcome, " + displayName);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(home.this, "Error loading user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProgressBar() {
        if (userId == null) return;

        DatabaseReference knownRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("knownWords");

        firestore.collection("groups")
                .get()
                .addOnSuccessListener(groupResult -> {
                    knownRef.get().addOnSuccessListener(knownSnapshot -> {
                        AtomicInteger totalWords = new AtomicInteger();
                        AtomicInteger knownCount = new AtomicInteger();
                        AtomicInteger remaining = new AtomicInteger(groupResult.size());

                        if (groupResult.isEmpty()) {
                            progressText.setText("No groups found");
                            progressBar.setProgress(0);
                            return;
                        }

                        for (QueryDocumentSnapshot groupDoc : groupResult) {
                            String groupId = groupDoc.getId();

                            firestore.collection("groups")
                                    .document(groupId)
                                    .collection("words")
                                    .get()
                                    .addOnSuccessListener(wordResult -> {
                                        for (QueryDocumentSnapshot wordDoc : wordResult) {
                                            totalWords.incrementAndGet();

                                            Boolean isKnown = knownSnapshot
                                                    .child(groupId)
                                                    .child(wordDoc.getId())
                                                    .getValue(Boolean.class);

                                            if (Boolean.TRUE.equals(isKnown)) {
                                                knownCount.incrementAndGet();
                                            }
                                        }

                                        if (remaining.decrementAndGet() == 0) {
                                            int total = totalWords.get();
                                            int known = knownCount.get();
                                            if (total > 0) {
                                                int percent = (int) ((known * 100.0f) / total);
                                                progressBar.setProgress(percent);
                                                progressText.setText("Known words: " + percent + "%");
                                            } else {
                                                progressText.setText("No words found");
                                                progressBar.setProgress(0);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (remaining.decrementAndGet() == 0) {
                                            progressText.setText("Failed to load words");
                                            progressBar.setProgress(0);
                                        }
                                    });
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    progressText.setText("Failed to load progress");
                    progressBar.setProgress(0);
                });
    }
}
