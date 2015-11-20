package com.ruffneck.player.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.ruffneck.player.exception.NoMoreNextSongException;
import com.ruffneck.player.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.music.queue.MusicArrayQueue;
import com.ruffneck.player.music.queue.MusicQueue;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service implements Playable, Skipable {

    /**
     * How long does the service send a broadcast.
     */
    public static final int DELAY_SEND = 1000;
    private MediaPlayer mediaPlayer;
    private SharedPreferences mPref;

    public static final String ACTION_UPDATE_POSITION = "com.ruffneck.player.UPDATE_POSITION";
    public static final String ACTION_UPDATE_DURATION = "com.ruffneck.player.UPDATE_DURATION";
    public static final String ACTION_SKIP_SONG = "com.ruffneck.player.SKIP_SONG";
    public static final String ACTION_PLAY = "com.ruffneck.player.PLAY";
    public static final String ACTION_PAUSE = "com.ruffneck.player.PAUSE";
    public static final String ACTION_CONTINUE_PLAY = "com.ruffneck.player.CONTINUE_PLAY";

    //This field is used to add to the receiver's intent filter.
    public static final String[] actionList = new String[]{
            ACTION_SKIP_SONG, ACTION_UPDATE_DURATION, ACTION_UPDATE_POSITION,
            ACTION_PLAY,ACTION_PAUSE,ACTION_CONTINUE_PLAY};

    private Music music = null;

    private MusicQueue musicQueue = new MusicArrayQueue(this);
    //The mediaPlayer's state is stop , need to be start;
//    public static final int STATE_STOP = 1;

    //The mediaPlayer's state is pause , need to be continue;
//    public static final int STATE_PAUSE = 2;

    //The mediaPlayer's state is playing ,
//    public static final int STATE_PLAYING = 3;
    private Timer timer;

    private boolean isInit = false;

//    private static int state = STATE_STOP;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }


    class MusicBinder extends Binder implements Playable, Skipable {

        @Override
        public void play() {

            //判断是要继续播放还是要重新开始播放.
//            switch (mPref.getInt("state", STATE_STOP)) {
//                case STATE_STOP:
            PlayerService.this.play();
//                    break;
//                case STATE_PAUSE:
//                    continuePlay();
//                    break;
//            }
        }

        @Override
        public void pause() {
            PlayerService.this.pause();
        }

        @Override
        public void continuePlay() {
            PlayerService.this.continuePlay();
        }

        @Override
        public void seekTo(int process) {
            PlayerService.this.seekTo(process);
        }

        @Override
        public boolean isPlaying() {
            return PlayerService.this.isPlaying();
        }

        @Override
        public boolean isInit() {
            return PlayerService.this.isInit();
        }

        @Override
        public Music getMusic() {
            return music;
        }


        @Override
        public void Skip(Music music) {
            PlayerService.this.Skip(music);
        }

        @Override
        public void next() throws NoMoreNextSongException {
            PlayerService.this.next();
        }

        @Override
        public void previous() throws NoMorePreviousSongException {
            PlayerService.this.previous();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mPref = getSharedPreferences("config", MODE_PRIVATE);
    }

    /*    public static int getState() {
            return state;
        }*/
    @Override
    public void play() {
        addTimer();
        isInit = true;
        //"/storage/emulated/0/kgmusic/download/Hardwell - Hardwell On Air 160.mp3"
        String playUrl = "/storage/emulated/0/kgmusic/download/Hardwell - Hardwell On Air 160.mp3";
        if (music != null) playUrl = music.getUrl();
        Uri myUri = Uri.fromFile(new File(playUrl));

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(PlayerService.this, "出现异常", Toast.LENGTH_SHORT).show();
        }
        mediaPlayer.start();

        //Notify all the receiver that the duration has changed!
        mPref.edit().putInt("duration", mediaPlayer.getDuration()).apply();
        sendBroadcast(new Intent(ACTION_UPDATE_DURATION));

        sendBroadcast(new Intent(ACTION_PLAY));
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
//        mPref.edit().putInt("state", STATE_PAUSE).apply();
        sendBroadcast(new Intent(ACTION_PAUSE));
    }


    @Override
    public void continuePlay() {
        mediaPlayer.start();
//        mPref.edit().putInt("state", STATE_PLAYING).apply();
        sendBroadcast(new Intent(ACTION_CONTINUE_PLAY));
    }

    @Override
    public void seekTo(int process) {
        mediaPlayer.seekTo(process);
        mPref.edit().putInt("position", process).apply();
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public boolean isInit() {
        return isInit;
    }

    @Override
    public Music getMusic() {
        return music;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
//        mPref.edit().putInt("state", STATE_STOP).apply();
        System.out.println("PlayerService.onDestroy");
        timer.cancel();
        timer = null;
    }

    public void addTimer() {
        if (timer != null)
            timer.cancel();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int position = mPref.getInt("position", 0);
                int CurrentPosition = mediaPlayer.getCurrentPosition();
                if (position != CurrentPosition) {
                    mPref.edit().putInt("position", CurrentPosition).apply();
                    Intent intent = new Intent(ACTION_UPDATE_POSITION);
//                    Bundle bundle = new Bundle();
//                    bundle.putInt("duration", mediaPlayer.getDuration());
//                    bundle.putInt("position", mediaPlayer.getCurrentPosition());
//                    intent.putExtras(bundle);
//                System.out.println("PlayerService.run");
//                System.out.println("mediaPlayer = " + mediaPlayer.getCurrentPosition());
                    sendBroadcast(intent);
                }
            }
        }, 100, DELAY_SEND);
    }


    @Override
    public void Skip(Music music) {
        PlayerService.this.music = music;
        mediaPlayer.reset();
        play();

        //Notify activity that need to update the music's information.
        mPref.edit().putInt("position", 0).apply();
        Intent intent = new Intent(ACTION_SKIP_SONG);
        Bundle bundle = new Bundle();
        bundle.putParcelable("music", music);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    @Override
    public void next() throws NoMoreNextSongException {
            Skip(musicQueue.next(music));
    }

    @Override
    public void previous() throws NoMorePreviousSongException {
            Skip(musicQueue.previous(music));
    }
}
