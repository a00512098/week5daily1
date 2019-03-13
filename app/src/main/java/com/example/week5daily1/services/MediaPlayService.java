package com.example.week5daily1.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.week5daily1.R;
import com.example.week5daily1.model.Song;
import com.example.week5daily1.view.MainActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MediaPlayService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioManager.OnAudioFocusChangeListener{

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songs;
    private int position;
    private final IBinder binder = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFICATION_ID = 520;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mediaPlayer.getCurrentPosition() > 0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(getString(R.string.playing))
                .setContentText(songTitle);
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        position = 0;
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void initMusicPlayer() {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> listOfSongs) {
        songs = listOfSongs;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

    }

    public class MusicBinder extends Binder {
        public MediaPlayService getService() {
            return MediaPlayService.this;
        }
    }

    public void playSong() {
        mediaPlayer.reset();
        Song playSong = songs.get(position);
        songTitle = playSong.getTitle();
        long currentSong = playSong.getId();

        Uri songUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currentSong);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), songUri);
        }
        catch (IOException e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        mediaPlayer.prepareAsync();
    }

    public void setSong(int songIndex) {
        position = songIndex;
    }

    public int getPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer(){
        mediaPlayer.pause();
    }

    public void seek(int position){
        mediaPlayer.seekTo(position);
    }

    public void go(){
        mediaPlayer.start();
    }

    public void playPrevious(){
        position--;
        if(position < 0) {
            position = songs.size() - 1;
        }
        playSong();
    }

    public void playNext(){
        position++;
        if(position >= songs.size()) {
            position =0;
        }
        playSong();
    }
}
