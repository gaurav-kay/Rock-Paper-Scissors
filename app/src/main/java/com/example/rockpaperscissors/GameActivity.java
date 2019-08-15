package com.example.rockpaperscissors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private Bundle bundle = getIntent().getExtras();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        String roomId = bundle.getString("roomId");


    }
}
