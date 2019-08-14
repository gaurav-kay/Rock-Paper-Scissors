package com.example.rockpaperscissors;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterUserActivity extends AppCompatActivity {

    private static final String TAG = "TAG";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    protected FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private EditText usernameEditText, nameEditText;
    private EditText passwordEditText, confirmPasswordEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        final TextView logInTextView = findViewById(R.id.sign_in_text_view);
        usernameEditText = findViewById(R.id.username);
        nameEditText = findViewById(R.id.name);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        Button registerButton = findViewById(R.id.register_button);
        progressBar = findViewById(R.id.progressBar);

        logInTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterUserActivity.this, LoginActivity.class));
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    private void makeDatabaseChild() {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("name", nameEditText.getText().toString());
        userDetails.put("username", usernameEditText.getText().toString());

        db.collection("users").document(user.getEmail().split("@")[0]).set(userDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RegisterUserActivity.this, "Registered", Toast.LENGTH_LONG).show();

                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(RegisterUserActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);

                        Log.d(TAG, "DATABASE FAILURE onFailure: " + e.toString());
                    }
                });
    }

    private void registerUser() {
        // TODO: Check same username clashes

        if (usernameEditText.getText().toString().trim().equals("") || passwordEditText.getText().toString().equals("") || confirmPasswordEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_LONG).show();
        } else {
            if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show();
                Log.d(TAG, "registerUser: Password don't match");
            } else {
                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(usernameEditText.getText().toString() + "@test.com", passwordEditText.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                mAuth = FirebaseAuth.getInstance();
                                user = mAuth.getCurrentUser();

                                makeDatabaseChild();

                                Log.d(TAG, "USER CREATION SUCCESSFUL: " + authResult.getUser().getEmail());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(RegisterUserActivity.this, "USER CREATION failed", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "onFailure: " + e.toString());
                            }
                        });
            }
        }
    }
}

