package com.example.week5daily1.view;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import android.widget.MediaController.MediaPlayerControl;

import com.example.week5daily1.controller.RecyclerAdapter;
import com.example.week5daily1.services.MediaPlayService;
import com.example.week5daily1.controller.MediaPlayerControllerController;
import com.example.week5daily1.R;
import com.example.week5daily1.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl, RecyclerAdapter.SongClickListener {

    private MediaPlayService mediaPlayService;
    private Intent intent;
    private ArrayList<Song> songs;
    private ListView songListView;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private MediaPlayerControllerController musicController;

    private boolean serviceBound = false;
    private boolean globalPaused = false;
    private boolean musicPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for read external storage on devices with Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                return;
            }}

        initRecyclerView();

        sortSongs();

        setController();
    }

    private void sortSongs() {
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song o1, Song o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        songs = new ArrayList<>();
        getAvailableSongs();
        recyclerAdapter = new RecyclerAdapter(songs, this);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (intent == null) {
            intent = new Intent(this, MediaPlayService.class);
            bindService(intent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        globalPaused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(globalPaused){
            setController();
            globalPaused = false;
        }
    }

    @Override
    protected void onStop() {
        musicController.hide();
        super.onStop();
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayService.MusicBinder binder = (MediaPlayService.MusicBinder) service;

            mediaPlayService = binder.getService();
            mediaPlayService.setList(songs);
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    // Communication with the recycler view
    @Override
    public void onSongClicked(View view) {
        mediaPlayService.setSong(Integer.parseInt(view.getTag().toString()));
        mediaPlayService.playSong();
        if(musicPaused){
            setController();
            musicPaused = false;
        }
        musicController.show(0);
    }

    @Override
    protected void onDestroy() {
        stopService(intent);
        mediaPlayService = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.finish) {
                stopService(intent);
                mediaPlayService = null;
                System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    public void getAvailableSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri contentUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(contentUri, null, null, null, null);

        // Check if there are song_item in the device
        if (cursor != null && cursor.moveToFirst()) {
            int titleLabel = cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idLabel = cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID);
            int artistLabel = cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                long id = cursor.getLong(idLabel);
                String title = cursor.getString(titleLabel);
                String artist = cursor.getString(artistLabel);
                songs.add(new Song(id, title, artist));
            } while (cursor.moveToNext());
        }
    }

    private void setController(){
        musicController = new MediaPlayerControllerController(this);
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        musicController.setMediaPlayer(this);
        musicController.setAnchorView(findViewById(R.id.recyclerView));
        musicController.setEnabled(true);
    }

    private void playNext(){
        mediaPlayService.playNext();
        if(musicPaused){
            setController();
            musicPaused =false;
        }
        musicController.show(0);
    }

    private void playPrev(){
        mediaPlayService.playPrevious();
        if(musicPaused){
            setController();
            musicPaused =false;
        }
        musicController.show(0);
    }

    @Override
    public void start() {
        mediaPlayService.go();
    }

    @Override
    public void pause() {
        musicPaused = true;
        mediaPlayService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(mediaPlayService !=null && serviceBound && mediaPlayService.isPlaying())
        return mediaPlayService.getDuration();
  else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(mediaPlayService !=null && serviceBound && mediaPlayService.isPlaying())
        return mediaPlayService.getPosition();
  else return 0;
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(mediaPlayService !=null && serviceBound)
        return mediaPlayService.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
