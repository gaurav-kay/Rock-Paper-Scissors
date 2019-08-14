package com.example.rockpaperscissors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button sendInviteButton;
    private EditText username;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendInviteButton = findViewById(R.id.inviteButton);
        sendInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, "Enter a username", Toast.LENGTH_SHORT).show();
                } else {
                    sendInvite(username.getText().toString());
                }
            }
        });


    }

    private void sendInvite(final String username) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Map<String, Object> map = new HashMap<>();
        map.put("by", currentUser.getEmail().split("@")[0]);

        db.collection("users")
                .document(username)
                .collection("invites")
                .document(String.valueOf(new Date().getTime()))
                .set(map)

                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent newActivityIntent = new Intent(MainActivity.this, LoadingScreenActivity.class);
                        newActivityIntent.putExtra("username", username);

                        startActivity(newActivityIntent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        sendInvite(username);
                    }
                });
    }
}
