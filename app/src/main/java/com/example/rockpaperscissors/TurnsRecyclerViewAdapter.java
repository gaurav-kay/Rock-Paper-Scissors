package com.example.rockpaperscissors;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class TurnsRecyclerViewAdapter extends RecyclerView.Adapter<TurnsRecyclerViewAdapter.TurnViewHolder> {

    ArrayList<HashMap<String, Object>> turns;
    boolean server;

    public TurnsRecyclerViewAdapter(ArrayList<HashMap<String, Object>> turns, boolean server) {
        this.turns = turns;
        this.server = server;
    }

    @NonNull
    @Override
    public TurnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_turn_view, parent, false);

        return new TurnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TurnViewHolder turnViewHolder, int position) {
        if (server) {
            turnViewHolder.yourMoveTextView.setText((String) turns.get(position).get("serverMove"));
            turnViewHolder.opponentMoveTextView.setText((String) turns.get(position).get("opponentMove"));
            turnViewHolder.yourScoreTextView.setText(String.valueOf((Long) turns.get(position).get("serverScore")));
            turnViewHolder.opponentScoreTextView.setText(String.valueOf((Long) turns.get(position).get("opponentScore")));

            if ((turns.get(position).get("serverMove")).equals(turns.get(position).get("opponentMove"))) {
                turnViewHolder.yourScoreTextView.setTextColor(Color.rgb(0, 0, 255));
                turnViewHolder.opponentScoreTextView.setTextColor(Color.rgb(0, 0, 255));
            } else {
                if ((boolean) turns.get(position).get("isServerWin")) {
                    turnViewHolder.yourScoreTextView.setTextColor(Color.rgb(0, 255, 0));
                    turnViewHolder.opponentScoreTextView.setTextColor(Color.rgb(255, 0, 0));
                } else {
                    turnViewHolder.yourScoreTextView.setTextColor(Color.rgb(255, 0, 0));
                    turnViewHolder.opponentScoreTextView.setTextColor(Color.rgb(0, 255, 0));
                }
            }
        } else {
            turnViewHolder.yourMoveTextView.setText((String) turns.get(position).get("opponentMove"));
            turnViewHolder.opponentMoveTextView.setText((String) turns.get(position).get("serverMove"));
            turnViewHolder.yourScoreTextView.setText(String.valueOf((Long) turns.get(position).get("opponentScore")));
            turnViewHolder.opponentScoreTextView.setText(String.valueOf((Long) turns.get(position).get("serverScore")));

            if ((turns.get(position).get("serverMove")).equals(turns.get(position).get("opponentMove"))) {
                turnViewHolder.yourScoreTextView.setTextColor(Color.rgb(0, 0, 255));
                turnViewHolder.opponentScoreTextView.setTextColor(Color.rgb(0, 0, 255));
            } else {
                if ((boolean) turns.get(position).get("isServerWin")) {
                    turnViewHolder.opponentScoreTextView.setTextColor(Color.rgb(0, 255, 0));
                    turnViewHolder.yourScoreTextView.setTextColor(Color.rgb(255, 0, 0));
                } else {
                    turnViewHolder.opponentScoreTextView.setTextColor(Color.rgb(255, 0, 0));
                    turnViewHolder.yourScoreTextView.setTextColor(Color.rgb(0, 255, 0));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return turns.size();
    }

    public class TurnViewHolder extends RecyclerView.ViewHolder {

        TextView yourMoveTextView, opponentMoveTextView, yourScoreTextView, opponentScoreTextView;

        public TurnViewHolder(@NonNull View itemView) {
            super(itemView);

            yourMoveTextView = itemView.findViewById(R.id.item_your_move_text_view);
            opponentMoveTextView = itemView.findViewById(R.id.item_opponent_move_text_view);
            yourScoreTextView = itemView.findViewById(R.id.item_your_score_text_view);
            opponentScoreTextView = itemView.findViewById(R.id.item_opponent_score_text_view);
        }
    }
}
