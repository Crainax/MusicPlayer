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

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service implements PlayerInterface {

    /**
     * How long does the service send a broadcast.
     */
    public static final int DELAY_SEND = 1000;
    MediaPlayer mediaPlayer;
    private SharedPreferences mPref;

    public static final String ACTION_UPDATE = "com.ruffneck.player.UPDATE";

    //The mediaPlayer's state is stop , need to be start;
    public static final int STATE_STOP = 1;

    //The mediaPlayer's state is pause , need to be continue;
    public static final int STATE_PAUSE = 2;

    //The mediaPlayer's state is playing ,
    public static final int STATE_PLAYING = 3;
    private Timer timer;

    private boolean isInit = false;

//    private static int state = STATE_STOP;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    class MusicBinder extends Binder implements PlayerInterface {

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
        mPref.edit().putInt("state", STATE_PLAYING).apply();
        Uri myUri = Uri.fromFile(new File("sdcard/1.mp3"));
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(PlayerService.this, "出现异常", Toast.LENGTH_SHORT).show();
        }
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        mPref.edit().putInt("state", STATE_PAUSE).apply();
    }


    @Override
    public void continuePlay() {
        mediaPlayer.start();
        mPref.edit().putInt("state", STATE_PLAYING).apply();
    }

    @Override
    public void seekTo(int process) {
        mediaPlayer.seekTo(process);
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
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        mPref.edit().putInt("state", STATE_STOP).apply();
        System.out.println("PlayerService.onDestroy");
        timer.cancel();
        timer = null;
    }

    public void addTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(ACTION_UPDATE);
                Bundle bundle = new Bundle();
                bundle.putInt("duration", mediaPlayer.getDuration());
                bundle.putInt("position", mediaPlayer.getCurrentPosition());
                intent.putExtras(bundle);
//                System.out.println("PlayerService.run");
//                System.out.println("mediaPlayer = " + mediaPlayer.getCurrentPosition());
                sendBroadcast(intent);
            }
        }, 100, DELAY_SEND);
    }


}
