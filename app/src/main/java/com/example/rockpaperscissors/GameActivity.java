package com.example.rockpaperscissors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

    private Bundle bundle = getIntent().getExtras();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Button rockButton, paperButton, scissorButton;
    private TextView scoreTextView, opponentMoveTextView, yourMoveTextView;

    private String roomId = null;
    private Integer gameState = null;
    private boolean userPlayed = false;
    private String opponentMove = null, userMove = null;
    private String opponentUsername = null, currentUsername = null;
    private Integer prevCurrentUserScore = 0, prevOtherUserScore = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
                            prevCurrentUserScore = (Integer) documentSnapshot.get(currentUsername + "Score");
                            prevOtherUserScore = (Integer) documentSnapshot.get(opponentUsername + "Score");

                            if (documentSnapshot.get("gameState").equals(0)) {
                                gameState = 0;

                                askUserToPlayTurn();
                            } else if (documentSnapshot.get("gameState").equals(1)) {
                                gameState = 1;

                                if (userPlayed == false) {
                                    updateForOtherUserPlayed();

                                    opponentMove = (String) documentSnapshot.get(opponentUsername + "Turn");  // saving to use for score calculation
                                } else {
                                    waitForOtherUserTurn(documentSnapshot.get(getCurrentUsername() + "Turn"));

                                    // presently, no update turn feature
                                }
                            } else if (documentSnapshot.get("gameState").equals(2)) {
                                gameState = 2;

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
        boolean currentUserWins = false;
        if (userMove.equals(opponentMove)) {
            currentUserWins = false;
        } else if (userMove.equals("rock") && opponentMove.equals("scissor")) {
            currentUserWins = true;
        } else if (userMove.equals("paper") && opponentMove.equals("rock")) {
            currentUserWins = true;
        } else if (userMove.equals("scissor") && opponentMove.equals("paper")) {
            currentUserWins = true;
        }

        if (currentUserWins) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("gameState", 0);
            map.put(currentUsername + "Score", prevCurrentUserScore + 1);
            prevCurrentUserScore += 1;

            db.collection("rooms")
                    .document(roomId)

                    .update(map);
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("gameState", 0);
            map.put(opponentUsername + "Score", prevOtherUserScore + 1);
            prevOtherUserScore += 1;

            db.collection("rooms")
                    .document(roomId)

                    .update(map);
        }

        scoreTextView.setText(currentUsername + ": " + prevCurrentUserScore + ", " + opponentUsername + ": " + prevOtherUserScore);
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

    private void updateWithUserTurn(String move) {
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
