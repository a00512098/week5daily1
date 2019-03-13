package com.example.week5daily1.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.week5daily1.R;
import com.example.week5daily1.model.Song;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    ArrayList<Song> songs;
    Context context;
    SongClickListener songClickListener;

    public RecyclerAdapter(ArrayList<Song> songs, Context context) {
        this.songs = songs;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.song_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Song song = songs.get(i);
        viewHolder.songLabel.setText(song.getTitle());
        viewHolder.artistLabel.setText(song.getArtist());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songClickListener = (SongClickListener) context;
                v.setTag(i);
                songClickListener.onSongClicked(v);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView songLabel;
        TextView artistLabel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songLabel = itemView.findViewById(R.id.song_title);
            artistLabel = itemView.findViewById(R.id.song_artist);
        }
    }

    public interface SongClickListener {
        void onSongClicked(View view);
    }
}
