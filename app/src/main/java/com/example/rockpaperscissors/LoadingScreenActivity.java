package com.example.rockpaperscissors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingScreenActivity extends AppCompatActivity {

    private Intent currentActivityIntent = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textView = findViewById(R.id.textView);
        String textViewText = "Waiting for " + currentActivityIntent.getStringExtra("username") + "'s response";
        textView.setText(textViewText);
    }
}
