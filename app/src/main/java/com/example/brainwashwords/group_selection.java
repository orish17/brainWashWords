package com.example.brainwashwords;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class group_selection extends AppCompatActivity { // או שם ה-Activity שלך
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private List<Group> groupList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);


        // Initialize data
        groupList = new ArrayList<>();

        // Setup RecyclerView
        setupRecyclerView();

        // Initialize Firestore and load data
        db = FirebaseFirestore.getInstance();
        loadGroups();
    }


    private void setupRecyclerView() {
        // Set layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Initialize and set adapter
        adapter = new GroupAdapter(groupList);
        recyclerView.setAdapter(adapter);


        recyclerView.setHasFixedSize(true);
    }

    private void loadGroups() {
        showLoading(true);

        db.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            groupList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Group group = document.toObject(Group.class);
                                groupList.add(group);
                            }
                            adapter.notifyDataSetChanged();

                            // Show empty state if needed
                            if (groupList.isEmpty()) {
                                showEmptyState(true);
                            } else {
                                showEmptyState(false);
                            }
                        } else {
                            handleError(task.getException());
                        }
                    }
                });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);

        }
    }

    private void showEmptyState(boolean show) {
        View emptyView = findViewById(R.id.emptyView);
        if (emptyView != null) {
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void handleError(Exception e) {
        Toast.makeText(this,
                "Error loading groups: " + e.getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    }