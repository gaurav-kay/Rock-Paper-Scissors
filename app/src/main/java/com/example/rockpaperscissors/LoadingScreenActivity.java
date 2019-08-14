package com.example.rockpaperscissors;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class LoadingScreenActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        bundle = getIntent().getExtras();

        Log.d(TAG, "onCreate: in loading screen activity");

        ProgressBar progressBar = findViewById(R.id.progressBar);
        TextView textView = findViewById(R.id.textView);
        Button cancelButton = findViewById(R.id.button);

        String textViewText = "Waiting for " + bundle.getString("username") + "'s response";
        textView.setText(textViewText);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoadingScreenActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        bundle = getIntent().getExtras();

        db.collection("rooms")
                .document((String) bundle.get("roomId"))

                .addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if ((boolean) documentSnapshot.get("isFull")) {
                            Intent newActivityIntent = new Intent(LoadingScreenActivity.this, GameActivity.class);
                            newActivityIntent.putExtra("roomId", (String) bundle.get("roomId"));

                            startActivity(newActivityIntent);
                            finish();
                        }
                    }
                });
    }
}
