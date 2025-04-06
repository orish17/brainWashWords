package com.example.brainwashwords;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class home extends AppCompatActivity {
    private AppCompatButton button, about, testYourself;
    private boolean isSorted;
    private TextView usernameDisplay;
    private DatabaseReference usersRef;
    private String username;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        initializeViews();
        setupAlertDialog();




        usernameDisplay = findViewById(R.id.usernameTextView3);
        button = findViewById(R.id.button); // Initialize button
        about = findViewById(R.id.button3); // Initialize about
        testYourself = findViewById(R.id.button1); // Initialize testYourself

        isSorted = false;

        username = getIntent().getStringExtra("USERNAME");
        if (username != null) {
            usernameDisplay.setText("Welcome, " + username);
            loadUsernameFromFirebase();

        }
        setupClickListeners();
    }

    private void initializeViews() {
        usernameDisplay = findViewById(R.id.usernameTextView3);
        button = findViewById(R.id.button);
        about = findViewById(R.id.button3);
        testYourself = findViewById(R.id.button1);
    }

    private void setupAlertDialog() {
        builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Wait a minute");
        builder.setMessage("You have to sort the words first");
        builder.setNegativeButton("OK, got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }
    private void setupClickListeners() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this, group_selection.class));
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(home.this, about.class));
            }
        });

        testYourself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSorted) {  // Fixed comparison operator
                    builder.show();  // Show the dialog
                }
            }
        });
    }

    private void loadUsernameFromFirebase() {
                // Assuming we want to find the user by their username
                usersRef.orderByChild("name").equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        User user = userSnapshot.getValue(User.class);
                                        if (user != null) {
                                            usernameDisplay.setText("Welcome, " + user.getName());
                                            // You can also access other user data here
                                            // String email = user.getEmail();
                                        }
                                        break; // Just take the first match
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(home.this, "Error loading user data",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this , signup.class);
                startActivity(intent);
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, com.example.brainwashwords.about.class);
                startActivity(intent);
            }
        });

        testYourself.setOnClickListener(new View.OnClickListener() {//alert dialog code
            @Override
            public void onClick(View v) {
                if (!isSorted) {
                    builder.setCancelable(true);
                    builder.setTitle("wait a minute");
                    builder.setMessage("you have to sort the words first");

                    builder.setNegativeButton("ok,got it!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                }
            }
        });
    }
    }