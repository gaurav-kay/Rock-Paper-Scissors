package com.example.rockpaperscissors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;

import javax.annotation.Nullable;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Button rockButton, paperButton, scissorButton;
    private TextView scoreTextView, opponentMoveTextView, yourMoveTextView, outcomeTextView;

    private String roomId, opponentUsername, currentUsername;
    private boolean server;
    private String playerPlayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle bundle = getIntent().getExtras();

        roomId = bundle.getString("roomId");

        if ((boolean) bundle.get("server")) {
            server = true;
            playerPlayed = "serverPlayed";
            setUpServer();
        } else {
            server = false;
            playerPlayed = "opponentPlayed";
        }

        addRoomListener();  // non server only listens

        // read normally (?)

        rockButton = findViewById(R.id.rock_button);
        paperButton = findViewById(R.id.paper_button);
        scissorButton = findViewById(R.id.scissor_button);
        scoreTextView = findViewById(R.id.score_text_view);
        opponentMoveTextView = findViewById(R.id.other_user_turn_text_view);
        yourMoveTextView = findViewById(R.id.user_turn_text_view);
        outcomeTextView = findViewById(R.id.outcome_text_view);

        rockButton.setOnClickListener(clickHandler());
        paperButton.setOnClickListener(clickHandler());
        scissorButton.setOnClickListener(clickHandler());
    }

    private void setUpServer() {
        if (server) {
            Log.d(TAG, "setUpServer: SET UP BY " + Build.MANUFACTURER + " " + Build.MODEL);

            db.collection("rooms")
                    .document(roomId)

                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if ((boolean) documentSnapshot.get("serverPlayed") && (boolean) documentSnapshot.get("opponentPlayed")) {
                                // both have played
                                String serverMove = (String) documentSnapshot.get("serverPlayedMove");
                                String opponentMove = (String) documentSnapshot.get("opponentPlayedMove");

                                HashMap<String, Object> updateMap = new HashMap<>();
                                if (!serverMove.equals(opponentMove)) {
                                    if (isServerWin(serverMove, opponentMove)) {
                                        updateMap.put("serverScore", (Long) documentSnapshot.get("serverScore") + 1L);
                                    } else {
                                        updateMap.put("opponentScore", (Long) documentSnapshot.get("opponentScore") + 1L);
                                    }
                                }

                                updateMap.put("serverPlayed", false);
                                updateMap.put("opponentPlayed", false);

                                db.collection("rooms")
                                        .document(roomId)

                                        .update(updateMap);
                            }
                        }
                    });
        }
    }

    private boolean isServerWin(String serverMove, String opponentMove) {
        return serverMove.equals("paper") && opponentMove.equals("rock") ||
                serverMove.equals("rock") && opponentMove.equals("scissor") ||
                serverMove.equals("scissor") && opponentMove.equals("paper");
    }

    private void addRoomListener() {  // client side
        db.collection("rooms")
                .document(roomId)

                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (server && (boolean) documentSnapshot.get("opponentPlayed")) {
                            updateForOtherUserPlayed((String) documentSnapshot.get("opponentPlayedMove"));
                        } else if (!server && (boolean) documentSnapshot.get("serverPlayed")) {
                            updateForOtherUserPlayed((String) documentSnapshot.get("serverPlayedMove"));
                        }

                        if (documentSnapshot.exists()) {
                            updateScores(
                                    (Long) documentSnapshot.get("serverScore"),
                                    (Long) documentSnapshot.get("opponentScore"),
                                    (String) documentSnapshot.get("serverUsername"),
                                    (String) documentSnapshot.get("opponentUsername")
                            );
                        }

                        if (!((boolean) documentSnapshot.get("serverPlayed") || (boolean) documentSnapshot.get("opponentPlayed"))) {
                            // both haven't played
                            resetForNextRound(
                                    (String) documentSnapshot.get("serverPlayedMove"),
                                    (String) documentSnapshot.get("opponentPlayedMove"),
                                    (String) documentSnapshot.get("serverUsername"),
                                    (String) documentSnapshot.get("opponentUsername")
                            );
                        }
                    }
                });
    }

    private void resetForNextRound(String serverPlayedMove, String opponentPlayedMove, String serverUsername, String opponentUsername) {
        if (serverPlayedMove.equals(opponentPlayedMove)) {
            outcomeTextView.setText("It's a draw!");
        } else {
            if (server) {
                outcomeTextView.setText(opponentUsername + " chose " + opponentPlayedMove);
            } else {
                outcomeTextView.setText(serverUsername + " chose " + serverPlayedMove);
            }
        }

        rockButton.setClickable(true);
        paperButton.setClickable(true);
        scissorButton.setClickable(true);

        yourMoveTextView.setText("Play your move");
        opponentMoveTextView.setText("Opponent is yet to play");
    }

    private void updateScores(Long serverScore, Long opponentScore, String serverUsername, String opponentUsername) {
        Log.d(TAG, "updateScores: SCORE UPDATEEEE");

        if (server) {  // only to ensure that the current player's name is first
            scoreTextView.setText(serverUsername + ": " + serverScore + ", " + opponentUsername + ": " + opponentScore);
        } else {
            scoreTextView.setText(opponentUsername + ": " + opponentScore + ", " + serverUsername + ": " + serverScore);
        }
    }

    private void updateForOtherUserPlayed(String move) {
        opponentMoveTextView.setText("Opponent has played");
    }

    private View.OnClickListener clickHandler() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userMove = null;
                if (view == rockButton) {
                    userMove = "rock";
                } else if (view == paperButton) {
                    userMove = "paper";
                } else if (view == scissorButton) {
                    userMove = "scissor";
                }

                rockButton.setClickable(false);
                paperButton.setClickable(false);
                scissorButton.setClickable(false);

                yourMoveTextView.setText(userMove);

                updateWithUserTurn(userMove);
            }
        };
    }

    private void updateWithUserTurn(String userMove) {
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put(playerPlayed + "Move", userMove);
        updateMap.put(playerPlayed, true);

        db.collection("rooms")
                .document(roomId)

                .update(updateMap);
    }

    private String getCurrentUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        return currentUser.getEmail().split("@")[0];
    }
}

// basically the server and opponent act as player 1 and player 2. check is done at client side.
// TODO: some buttons need double click? track w debugg