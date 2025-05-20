package com.example.brainwashwords;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class home extends BaseActivity {

    private AppCompatButton button, testYourself;
    private TextView usernameDisplay;
    private DatabaseReference usersRef;
    private FirebaseFirestore firestore;
    private String username;

    private boolean isSorted = false;
    private AlertDialog.Builder builder;

    private ProgressBar progressBar;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        firestore = FirebaseFirestore.getInstance();

        initializeViews();
        setupAlertDialog();
        setupDrawer();

        username = getIntent().getStringExtra("USERNAME");
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
    }

    private void checkIfSorted() {
        firestore.collection("groups").document("workout1").collection("words")
                .get()
                .addOnSuccessListener(result -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : result) {
                        Boolean known = doc.getBoolean("known");
                        if (Boolean.TRUE.equals(known)) {
                            count++;
                        }
                    }
                    isSorted = count >= 4;
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
        firestore.collection("groups")
                .get()
                .addOnSuccessListener(groupResult -> {
                    AtomicInteger total = new AtomicInteger(0);
                    AtomicInteger known = new AtomicInteger(0);
                    AtomicInteger remainingGroups = new AtomicInteger(groupResult.size());

                    if (remainingGroups.get() == 0) {
                        progressText.setText("No groups found");
                        return;
                    }

                    for (QueryDocumentSnapshot groupDoc : groupResult) {
                        String groupId = groupDoc.getId();

                        firestore.collection("groups").document(groupId).collection("words")
                                .get()
                                .addOnSuccessListener(wordResult -> {
                                    for (QueryDocumentSnapshot wordDoc : wordResult) {
                                        Boolean isKnown = wordDoc.getBoolean("known");
                                        total.incrementAndGet();
                                        if (Boolean.TRUE.equals(isKnown)) {
                                            known.incrementAndGet();
                                        }
                                    }

                                    if (remainingGroups.decrementAndGet() == 0) {
                                        int percent = (total.get() == 0) ? 0 : (known.get() * 100 / total.get());
                                        progressBar.setProgress(percent);
                                        progressText.setText("Known words: " + percent + "%");
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    progressText.setText("Failed to load words");
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressText.setText("Failed to load groups");
                });
    }
}
