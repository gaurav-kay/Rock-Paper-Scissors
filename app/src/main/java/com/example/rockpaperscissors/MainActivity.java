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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private Button sendInviteButton;
    private EditText username;
    private ListView listView;

    private ArrayList<String> usernames = new ArrayList<>();
    private ArrayList<String> listViewDetails = new ArrayList<>();

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
                joinRoomAsOpponent(usernames.get(i));

                Log.d(TAG, "onItemClick: " + usernames.get(i));
            }
        });
    }

    private void sendInvite(final String username) {
        Map<String, Object> map = new HashMap<>();
        map.put("by", getCurrentUsername());
        map.put("at", new SimpleDateFormat("h:mm a", Locale.ENGLISH).format(new Date()));

        // TODO: improve roomID generation
        final String roomId = getCurrentUsername() + username + new Date().getTime();

        createRoomAsHost(username, roomId);

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
                            newActivityIntent.putExtra("roomId", roomId);

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

    private String createRoomAsHost(String opponentUsername, String roomId) {
        Log.d(TAG, "createRoomAsHost: " + roomId);

        Map<String, Object> roomDetails = new HashMap<>();
        roomDetails.put("host", getCurrentUsername());
        roomDetails.put("opponent", opponentUsername);
        roomDetails.put("hostMove", "");
        roomDetails.put("opponentMove", "");
        roomDetails.put("startTime", String.valueOf(new Date().getTime()));
        roomDetails.put("isFull", false);
        roomDetails.put("isGameOver", false);
        roomDetails.put("hostScore", 0);
        roomDetails.put("opponentScore", 0);
        roomDetails.put("roomId", roomId);

        Log.d(TAG, "createRoomAsHost: " + roomDetails.values() + " " + roomDetails.keySet());

        db.collection("rooms")
                .document(roomId)
                .set(roomDetails);


        Map<String, Object> userDetailsUpdate = new HashMap<>();
        userDetailsUpdate.put("roomId", roomId);
        userDetailsUpdate.put("isInGame", true);
        db.collection("users")
                .document(getCurrentUsername())
                .update(userDetailsUpdate);
        db.collection("users")
                .document(opponentUsername)
                .update(userDetailsUpdate);

        return roomId;
    }

    private void joinRoomAsOpponent(String hostUsername) {
        db.collection("users")
                .document(hostUsername)
                .get()

                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {
                        db.collection("rooms")
                                .document((String) documentSnapshot.get("roomId"))
                                .update("isFull", true)

                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Intent startNewActivityIntent =
                                                new Intent(MainActivity.this, GameActivity.class);
                                        startNewActivityIntent.putExtra("roomId", (String) documentSnapshot.get("roomId"));

                                        startActivity(startNewActivityIntent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: ROOM UPDATION JOINROOMASOPPONENT FAILED" + e.toString());
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: JOINROOMASOPPONENT GET FAILIRE" + e.toString());
                    }
                });


        db.collection("users")
                .document(getCurrentUsername())
                .collection("invites")
                .whereEqualTo("by", hostUsername)
                .get()

                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            queryDocumentSnapshot.getReference().delete();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: removing invites failure");
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
                            usernames.clear();
                            listViewDetails.clear();
                            listView.setAdapter(null);

                            for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                                usernames.add((String) queryDocumentSnapshot.get("by"));
                                listViewDetails.add(queryDocumentSnapshot.get("by") + " at " + queryDocumentSnapshot.get("at"));
                            }

                            ArrayAdapter arrayAdapter =
                                    new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listViewDetails);

                            listView.setAdapter(arrayAdapter);
                        }
                    }
                });
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

    @Override
    protected void onStart() {
        super.onStart();

        listView = findViewById(R.id.listView);
        getPendingInvites();
    }
}
