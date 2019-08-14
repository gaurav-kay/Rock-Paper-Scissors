package com.example.rockpaperscissors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private Button sendInviteButton;
    private EditText username;
    private ListView listView;

    private ArrayList<String> usernames = new ArrayList<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPendingInvites();

        sendInviteButton = findViewById(R.id.inviteButton);
        listView = findViewById(R.id.listView);
        listView.setAdapter(null);
        username = findViewById(R.id.usernameEditText);

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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Toast.makeText(MainActivity.this, "clicc", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onItemClick: clicc");
            }
        });
    }

    private void getPendingInvites() {
        db.collection("users")
                .document(getCurrentUsername())
                .collection("invites")

                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                usernames.add((String) queryDocumentSnapshot.get("by"));
                            }

                            ArrayAdapter arrayAdapter =
                                    new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, usernames);

                            listView.setAdapter(arrayAdapter);
                        }
                    }
                });
    }

    private void sendInvite(final String username) {
        Map<String, Object> map = new HashMap<>();
        map.put("by", getCurrentUsername());

        // TODO: create room


        // TODO: add firebase admin sdk

        if (true) {
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
        } else {
            Toast.makeText(this, username + " does not exist", Toast.LENGTH_SHORT).show();
        }
    }

//    private boolean checkIfUserExists(String username) {
//        final boolean[] flag = {true};
//        mAuth.signInWithEmailAndPassword(username + "@test.com", "testtest")
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (!task.isSuccessful()) {
//                            try {
//                                throw task.getException();
//                            } catch (FirebaseAuthInvalidUserException e) {
//                                flag[0] = false;
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//        return flag[0];
//    }

    private String getCurrentUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        return currentUser.getEmail().split("@")[0];
    }
}
