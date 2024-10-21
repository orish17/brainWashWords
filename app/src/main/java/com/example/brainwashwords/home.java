package com.example.brainwashwords;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class home extends AppCompatActivity {
    androidx.appcompat.widget.AppCompatButton button, about;//to the next page
    boolean isSorted;//alert dialog
    androidx.appcompat.widget.AppCompatButton testYourself;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        button.findViewById(R.id.button);
        about.findViewById(R.id.button3);
        AlertDialog.Builder builder = new AlertDialog.Builder(home.this);
        isSorted=false;






        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this, signup.class);
                startActivity(intent);
            }
        });
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(home.this,com.example.brainwashwords.about.class);
                startActivity(intent);
            }
        });

        testYourself.setOnClickListener(new View.OnClickListener() {//alert dialog code
            @Override
            public void onClick(View v) {
                if(isSorted=false){
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