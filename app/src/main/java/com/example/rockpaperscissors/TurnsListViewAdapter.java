package com.example.rockpaperscissors;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class TurnsListViewAdapter extends BaseAdapter {

    Context context;
    ArrayList<HashMap<String, Object>> turns;
    boolean server;

    public TurnsListViewAdapter(Context context, ArrayList<HashMap<String, Object>> turns, boolean server) {
        this.context = context;
        this.turns = turns;
        this.server = server;
    }

    @Override
    public int getCount() {
        return turns.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (true) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.layout_turn_view, parent, false);

            if (server) {
                ((TextView) convertView.findViewById(R.id.item_your_move_text_view))
                        .setText((String) turns.get(position).get("serverMove"));
                ((TextView) convertView.findViewById(R.id.item_opponent_move_text_view))
                        .setText((String) turns.get(position).get("opponentMove"));
                ((TextView) convertView.findViewById(R.id.item_your_score_text_view))
                        .setText(String.valueOf((Long) turns.get(position).get("serverScore")));
                ((TextView) convertView.findViewById(R.id.item_opponent_score_text_view))
                        .setText(String.valueOf((Long) turns.get(position).get("opponentScore")));

                if ((boolean) turns.get(position).get("isServerWin")) {
                    ((TextView) convertView.findViewById(R.id.item_your_score_text_view))
                            .setTextColor(Color.rgb(0, 255, 0));
                    ((TextView) convertView.findViewById(R.id.item_opponent_score_text_view))
                            .setTextColor(Color.rgb(255, 0, 0));
                }
            } else {
                ((TextView) convertView.findViewById(R.id.item_your_move_text_view))
                        .setText((String) turns.get(position).get("opponentMove"));
                ((TextView) convertView.findViewById(R.id.item_opponent_move_text_view))
                        .setText((String) turns.get(position).get("serverMove"));
                ((TextView) convertView.findViewById(R.id.item_your_score_text_view))
                        .setText(String.valueOf((Long) turns.get(position).get("opponentScore")));
                ((TextView) convertView.findViewById(R.id.item_opponent_score_text_view))
                        .setText(String.valueOf((Long) turns.get(position).get("serverScore")));

                if ((boolean) turns.get(position).get("isServerWin")) {
                    ((TextView) convertView.findViewById(R.id.item_opponent_score_text_view))
                            .setTextColor(Color.rgb(0, 255, 0));
                    ((TextView) convertView.findViewById(R.id.item_your_score_text_view))
                            .setTextColor(Color.rgb(255, 0, 0));
                }
            }
        }
        return convertView;
    }
}
