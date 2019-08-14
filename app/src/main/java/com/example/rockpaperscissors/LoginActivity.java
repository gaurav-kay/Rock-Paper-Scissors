package com.example.rockpaperscissors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    protected FirebaseUser user;

    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        TextView registerTextView = findViewById(R.id.register_text_view);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.register_button);

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterUserActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        if (usernameEditText.getText().toString().trim().equals("") || passwordEditText.getText().toString().equals("")) {
            Toast.makeText(this, "Enter both username and password", Toast.LENGTH_SHORT).show();
        } else {
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(usernameEditText.getText().toString() + "@test.com", passwordEditText.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            user =  mAuth.getCurrentUser();

                            Log.d(TAG, "onComplete: SIGN IN SUCCESSFUL");
                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this, "Authentication failed, check credentials", Toast.LENGTH_SHORT).show();

                            Log.d(TAG, "onFailure: SIGN IN FAILURE" + e.toString());
                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
