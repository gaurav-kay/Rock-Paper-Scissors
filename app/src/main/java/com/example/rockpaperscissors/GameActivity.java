package com.example.rockpaperscissors;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import javax.annotation.Nullable;

public class GameActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private Bundle bundle = getIntent().getExtras();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button rockButton, paperButton, scissorButton;
    private TextView scoreTextView, opponentMoveTextView, yourMoveTextView;

    private String roomId = null;
    private Integer gameState = null;
    private boolean userPlayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        roomId = bundle.getString("roomId");

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
                    // turn played: rock
                } else if (view == paperButton) {

                } else if (view == scissorButton) {

                }
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
                            if (documentSnapshot.get("gameState").equals(0)) {
                                gameState = 0;

                                askUserToPlayTurn();
                                disableButtons();
                            } else if (documentSnapshot.get("gameState").equals(1)) {
                                gameState = 1;

                                if (userPlayed == false) {
                                    updateForOtherUserPlayed();
                                } else {
                                    waitForOtherUserTurn();
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

    private void askUserToPlayTurn() {
        yourMoveTextView.setText("Play your Move");

    }
}
