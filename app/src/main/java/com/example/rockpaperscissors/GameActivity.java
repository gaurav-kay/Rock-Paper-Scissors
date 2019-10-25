package com.example.rockpaperscissors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Source;

import java.util.HashMap;

import javax.annotation.Nullable;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private Bundle bundle;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Button rockButton, paperButton, scissorButton;
    private TextView scoreTextView, opponentMoveTextView, yourMoveTextView;

    private String roomId = null;
    private Long gameState = null;
    private boolean userPlayed = false;
    private String opponentMove = null, userMove = null;
    private String opponentUsername = null, currentUsername = null;
    private Long prevCurrentUserScore = Long.parseLong("0");
    private Long prevOtherUserScore = Long.parseLong("0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        bundle = getIntent().getExtras();

        roomId = bundle.getString("roomId");
        opponentUsername = bundle.getString("opponentUsername");
        currentUsername = bundle.getString("hostUsername");

        addGameStateListener();

        rockButton = findViewById(R.id.rock_button);
        paperButton = findViewById(R.id.paper_button);
        scissorButton = findViewById(R.id.scissor_button);
        scoreTextView = findViewById(R.id.score_text_view);
        opponentMoveTextView = findViewById(R.id.other_user_turn_text_view);
        yourMoveTextView = findViewById(R.id.user_turn_text_view);

        rockButton.setOnClickListener(clickHandler());
        paperButton.setOnClickListener(clickHandler());
        scissorButton.setOnClickListener(clickHandler());
    }

    private View.OnClickListener clickHandler() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == rockButton) {
                    userMove = "rock";
                } else if (view == paperButton) {
                    userMove = "paper";
                } else if (view == scissorButton) {
                    userMove = "scissor";
                }

                userPlayed = true;

                rockButton.setClickable(false);
                paperButton.setClickable(false);
                scissorButton.setClickable(false);

                yourMoveTextView.setText(userMove);

                updateWithUserTurn(userMove);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        addGameStateListener();
    }

    private void addGameStateListener() {
        if (roomId != null) {
            db.collection("rooms")
                    .document(roomId)

                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            prevCurrentUserScore = (Long) documentSnapshot.get(currentUsername + "Score");
                            prevOtherUserScore = (Long) documentSnapshot.get(opponentUsername + "Score");

                            if ((documentSnapshot.get("gameState")).toString().equals(String.valueOf(0))) {
                                gameState = 0L;

                                askUserToPlayTurn();
                            } else if ((documentSnapshot.get("gameState")).toString().equals(String.valueOf(1))) {
                                gameState = 1L;

                                if (userPlayed == false) {
                                    updateForOtherUserPlayed();

                                    opponentMove = (String) documentSnapshot.get(opponentUsername + "Turn");  // saving to use for score calculation
                                } else {
                                    waitForOtherUserTurn(documentSnapshot.get(getCurrentUsername() + "Turn"));

                                    // presently, no update turn feature
                                }
                            } else if ((documentSnapshot.get("gameState")).toString().equals(String.valueOf(2))) {
                                gameState = 2L;

                                opponentMove = (String) documentSnapshot.get(opponentUsername + "Turn");
                                calculateScores();
                                updateForRoundFinish();
                            }
                        }
                    });
        }
    }

    private void updateForRoundFinish() {
        // pass 
    }

    private void calculateScores() {
        db.collection("rooms")
                .document(roomId)

                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String getUserMove = (String) documentSnapshot.get(currentUsername + "Turn");
                        String getOpponentMove = (String) documentSnapshot.get(opponentUsername + "Turn");

                        boolean currentUserWins = false;
                        if (getUserMove.equals(getOpponentMove)) {
                            currentUserWins = Boolean.parseBoolean(null);
                        } else if (getUserMove.equals("rock") && getOpponentMove.equals("scissor")) {
                            currentUserWins = true;
                        } else if (getUserMove.equals("paper") && getOpponentMove.equals("rock")) {
                            currentUserWins = true;
                        } else if (getUserMove.equals("scissor") && getOpponentMove.equals("paper")) {
                            currentUserWins = true;
                        }

                        if (currentUserWins == Boolean.parseBoolean(null)) {

                        }
                        if (currentUserWins) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("gameState", 0);
                            map.put(currentUsername + "Score", prevCurrentUserScore + 1);
                            map.put(opponentUsername + "Score", prevOtherUserScore);

                            prevCurrentUserScore += 1;
                            currentUserWins = false;

                            Log.d(TAG, "onSuccess: I WON: Scores: me, other" + prevCurrentUserScore + " " + prevOtherUserScore);

                            db.collection("rooms")
                                    .document(roomId)

                                    .update(map);
                        } else {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("gameState", 0);
                            map.put(opponentUsername + "Score", prevOtherUserScore + 1);
                            map.put(currentUsername + "Score", prevCurrentUserScore);

                            prevOtherUserScore += 1;
                            currentUserWins = false;

                            Log.d(TAG, "onSuccess: I LOST: Scores: me, other" + prevCurrentUserScore + " " + prevOtherUserScore);

                            db.collection("rooms")
                                    .document(roomId)

                                    .update(map);
                        }

                        String s = currentUsername + ": " + prevCurrentUserScore + ", " + opponentUsername + ": " + prevOtherUserScore;
                        scoreTextView.setText(s);

                        updateForRoundFinish();
                    }
                });
//        boolean currentUserWins = false;
//        if (userMove.equals(opponentMove)) {
//            currentUserWins = false;
//        } else if (userMove.equals("rock") && opponentMove.equals("scissor")) {
//            currentUserWins = true;
//        } else if (userMove.equals("paper") && opponentMove.equals("rock")) {
//            currentUserWins = true;
//        } else if (userMove.equals("scissor") && opponentMove.equals("paper")) {
//            currentUserWins = true;
//        }
//
//        if (currentUserWins) {
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("gameState", 0);
//            map.put(currentUsername + "Score", prevCurrentUserScore + 1);
//            prevCurrentUserScore += 1;
//
//            db.collection("rooms")
//                    .document(roomId)
//
//                    .update(map);
//        } else {
//            HashMap<String, Object> map = new HashMap<>();
//            map.put("gameState", 0);
//            map.put(opponentUsername + "Score", prevOtherUserScore + 1);
//            prevOtherUserScore += 1;
//
//            db.collection("rooms")
//                    .document(roomId)
//
//                    .update(map);
//        }
//
//        scoreTextView.setText(currentUsername + ": " + prevCurrentUserScore + ", " + opponentUsername + ": " + prevOtherUserScore);
    }

    private void waitForOtherUserTurn(Object move) {
        yourMoveTextView.setText("Your Turn: " + move);

        // set loading screen maybe
    }

    

    private void updateForOtherUserPlayed() {
        opponentMoveTextView.setText("Opponent has played");

        // can only think of this happening till now
    }

    private void askUserToPlayTurn() {
        yourMoveTextView.setText("Play your Move");

        rockButton.setClickable(true);
        paperButton.setClickable(true);
        scissorButton.setClickable(true);
    }

    private void updateWithUserTurn(final String move) {
        db.collection("rooms")
                .document(roomId)
                .get()

                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if ((documentSnapshot.get("gameState")).toString().equals(String.valueOf(1))) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("gameState", 2);
                            map.put(getCurrentUsername() + "Turn", move);

                            db.collection("rooms")
                                    .document(roomId)

                                    .update(map);
                        } else if ((documentSnapshot.get("gameState")).toString().equals(String.valueOf(0))) {
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("gameState", 1);
                            map.put(getCurrentUsername() + "Turn", move);

                            db.collection("rooms")
                                    .document(roomId)

                                    .update(map);
                        }
                    }
                });

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameState", 1);
        map.put(getCurrentUsername() + "Turn", move);

        db.collection("rooms")
                .document(roomId)

                .update(map);
    }

    private String getCurrentUsername() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        return currentUser.getEmail().split("@")[0];
    }
}
