package com.ruffneck.player.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ruffneck.player.R;
import com.ruffneck.player.activity.MainActivity;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.music.MusicLoader;
import com.ruffneck.player.music.exception.NoMoreNextSongException;
import com.ruffneck.player.music.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.queue.MusicArrayQueue;
import com.ruffneck.player.music.queue.MusicQueue;
import com.ruffneck.player.task.LoadImageTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerService extends Service implements Playable, Skipable {

    /**
     * How long does the service send a broadcast.
     */
    public static final int DELAY_SEND = 1000;
    private static final int NOTIFICATION_ID = 0x123;
    private MediaPlayer mediaPlayer;
    private SharedPreferences mPref;

    public static final String ACTION_UPDATE_POSITION = "com.ruffneck.player.UPDATE_POSITION";
    public static final String ACTION_UPDATE_DURATION = "com.ruffneck.player.UPDATE_DURATION";
    public static final String ACTION_SKIP_SONG = "com.ruffneck.player.SKIP_SONG";
    public static final String ACTION_PLAY = "com.ruffneck.player.PLAY";
    public static final String ACTION_PAUSE = "com.ruffneck.player.PAUSE";
    public static final String ACTION_CONTINUE_PLAY = "com.ruffneck.player.CONTINUE_PLAY";

    // The Bottom action including the continue play action.
    public static final String ACTION_NOTIFY_PLAY = "com.ruffneck.player.NOTIFY_PLAY";
    public static final String ACTION_NOTIFY_CLOSE = "com.ruffneck.player.NOTIFY_CLOSE";
    public static final String ACTION_NOTIFY_NEXT = "com.ruffneck.player.ACTION_NOTIFY_NEXT";
    public static final String ACTION_NOTIFY_PREVIOUS = "com.ruffneck.player.ACTION_NOTIFY_PREVIOUS";
    public static final String ACTION_NOTIFY_PAUSE = "com.ruffneck.player.ACTION_NOTIFY_PAUSE";

    //This field is used to add to the receiver's intent filter.(In the Activities)
    public static final String[] actionList = new String[]{
            ACTION_SKIP_SONG, ACTION_UPDATE_DURATION, ACTION_UPDATE_POSITION,
            ACTION_PLAY, ACTION_PAUSE, ACTION_CONTINUE_PLAY};

    private Music music = null;
    private MusicLoader musicLoader;

    private MusicQueue musicQueue;
    //The mediaPlayer's state is stop , need to be start;
//    public static final int STATE_STOP = 1;

    //The mediaPlayer's state is pause , need to be continue;
//    public static final int STATE_PAUSE = 2;

    //The mediaPlayer's state is playing ,
//    public static final int STATE_PLAYING = 3;
    private Timer timer;

    private boolean isInit = false;
    private NotificationManager notificationManager;
    private NotifyReceiver notifyReceiver;

    private MusicBinder mBinder = new MusicBinder();

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            try {
                next();
            } catch (NoMoreNextSongException e) {
                Toast.makeText(PlayerService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

//    private static int state = STATE_STOP;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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

        @Override
        public void setQueue(MusicQueue musicQueue) {
            PlayerService.this.setQueue(musicQueue);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mPref = getSharedPreferences("config", MODE_PRIVATE);

        musicLoader = MusicLoader.getInstance(this);
        musicQueue = new MusicArrayQueue(this);


        initMusic();

        initNotification();

        //Start the telephony calling listener.
        Intent intent = new Intent(this, TelephonyService.class);
        startService(intent);
    }

    private void initMusic() {

        int musicId = mPref.getInt("music", -1);
        if (musicId != -1) {
            music = musicLoader.findMusicById(musicId);
        } else {
            try {
                music = musicQueue.next(null);
            } catch (NoMoreNextSongException ignored) {

            }
        }

    }

    /**
     * Initialize the Notification to control the play state.
     */
    private void initNotification() {

        //Start the Receiver that receive the notification's action.
        notifyReceiver = new NotifyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NOTIFY_CLOSE);
        filter.addAction(ACTION_NOTIFY_NEXT);
        filter.addAction(ACTION_NOTIFY_PAUSE);
        filter.addAction(ACTION_NOTIFY_PLAY);
        filter.addAction(ACTION_NOTIFY_PREVIOUS);
        registerReceiver(notifyReceiver, filter);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        resetNotificationView();

    }

    /**
     * Refresh Notification's View.
     */
    private void resetNotificationView() {

        if (!isInit) return;

        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(this);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //Set the notification's View by the current music state.
        final RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        if (music != null) {
            remoteViews.setTextViewText(R.id.tv_notify_artist, music.getArtist());
            remoteViews.setTextViewText(R.id.tv_notify_song_name, music.getTitle());


        }


        if (isPlaying()) {
            remoteViews.setImageViewResource(R.id.bt_notify_play, R.drawable.ic_pause_white_24dp);
            setRemoteViewsOnclick(remoteViews, ACTION_NOTIFY_PAUSE, R.id.bt_notify_play);
        } else {
            remoteViews.setImageViewResource(R.id.bt_notify_play, R.drawable.ic_play_arrow_white_24dp);
            setRemoteViewsOnclick(remoteViews, ACTION_NOTIFY_PLAY, R.id.bt_notify_play);
        }

        //Set the PendingIntent binding with the Button.
        setRemoteViewsOnclick(remoteViews, ACTION_NOTIFY_CLOSE, R.id.bt_notify_close);
        setRemoteViewsOnclick(remoteViews, ACTION_NOTIFY_NEXT, R.id.bt_notify_next);
        setRemoteViewsOnclick(remoteViews, ACTION_NOTIFY_PREVIOUS, R.id.bt_notify_previous);

        if (music != null)
            builder.setTicker("Playing :" + music.getTitle());

        final Notification notify = builder.setStyle(style)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
//                .setTicker("Playing : " + music.getTitle())
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .build();

        notify.bigContentView = remoteViews;

        if (music != null) {
            //Update the singer's image.
            LoadImageTask task = new LoadImageTask(this);
            task.setUpdateCallBack(new LoadImageTask.UpdateCallBack() {
                @Override
                public void onUpdate(Music music) {
                    Picasso.with(PlayerService.this)
                            .load(music.getImgUrl())
                            .centerCrop()
                            .resizeDimen(R.dimen.notification_icon_width_height, R.dimen.notification_icon_width_height)
                            .into(remoteViews, R.id.iv_notify, NOTIFICATION_ID, notify);
                }
            });

            task.setUpdateFailCallBack(new LoadImageTask.UpdateFailCallBack() {
                @Override
                public void onUpdateFail(Music music) {
                    Picasso.with(PlayerService.this)
                            .load(R.drawable.placeholder_civ_singer)
                            .centerCrop()
                            .resizeDimen(R.dimen.notification_icon_width_height, R.dimen.notification_icon_width_height)
                            .into(remoteViews, R.id.iv_notify, NOTIFICATION_ID, notify);
                }
            });
            task.execute(music);
        }


        notificationManager.notify(NOTIFICATION_ID, notify);
    }


    /**
     * Set the remoteViews' view the listener.
     *
     * @param remoteViews       the remoteViews.
     * @param actionNotifyClose the action string field.
     * @param viewId            the view's Id need to be set
     */
    private void setRemoteViewsOnclick(RemoteViews remoteViews, String actionNotifyClose, int viewId) {
        Intent intent = new Intent(actionNotifyClose);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(viewId, pendingIntent);
    }

    /*    public static int getState() {
            return state;
        }*/
    @Override
    public void play() {


        isInit = true;
        //"/storage/emulated/0/kgmusic/download/Hardwell - Hardwell On Air 160.mp3"
        String playUrl;
        if (music != null)
            playUrl = music.getUrl();
        else
            try {
                next();
                playUrl = music.getUrl();
            } catch (NoMoreNextSongException e) {
                Toast.makeText(PlayerService.this, "No more song!", Toast.LENGTH_SHORT).show();
                return;
            }

        Uri myUri = Uri.fromFile(new File(playUrl));

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(mPref.getInt("position", 0));
            mediaPlayer.setOnCompletionListener(onCompletionListener);

        } catch (IOException e) {
            Toast.makeText(PlayerService.this, "出现异常", Toast.LENGTH_SHORT).show();
        }
        mediaPlayer.start();

        //Save the current song in the sharePreference in order to reload the song when restart!
        mPref.edit().putInt("music", music.getId()).apply();

        //Notify all the receiver that the duration has changed!
        mPref.edit().putInt("duration", mediaPlayer.getDuration()).apply();
        sendBroadcast(new Intent(ACTION_UPDATE_DURATION));
        sendBroadcast(new Intent(ACTION_PLAY));

        addTimer();
        //Reset the notification.
        resetNotificationView();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
//        mPref.edit().putInt("state", STATE_PAUSE).apply();
        sendBroadcast(new Intent(ACTION_PAUSE));
        resetNotificationView();
    }


    @Override
    public void continuePlay() {
        mediaPlayer.start();
//        mPref.edit().putInt("state", STATE_PLAYING).apply();
        sendBroadcast(new Intent(ACTION_CONTINUE_PLAY));
        resetNotificationView();
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
        unregisterReceiver(notifyReceiver);
        notificationManager.cancel(NOTIFICATION_ID);
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
//        mPref.edit().putInt("state", STATE_STOP).apply();
//        System.out.println("PlayerService.onDestroy");
        timer.cancel();
        timer = null;


        //stop the telephony calling listener.
        Intent intent = new Intent(this, TelephonyService.class);
        stopService(intent);
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

        if (timer != null)
            timer.cancel();

        //Notify activity that need to update the music's information.
        mPref.edit().putInt("position", 0).apply();
        Intent intent = new Intent(ACTION_SKIP_SONG);
        Bundle bundle = new Bundle();
        bundle.putParcelable("music", music);
        intent.putExtras(bundle);
        sendBroadcast(intent);

        play();
    }

    @Override
    public void next() throws NoMoreNextSongException {
        Skip(musicQueue.next(music));
    }

    @Override
    public void previous() throws NoMorePreviousSongException {
        Skip(musicQueue.previous(music));
    }

    @Override
    public void setQueue(MusicQueue musicQueue) {
        this.musicQueue = musicQueue;
    }

    private class NotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_NOTIFY_CLOSE:
                    onDestroy();
                    notificationManager.cancel(NOTIFICATION_ID);
                    isInit = false;
                    android.os.Process.killProcess(android.os.Process.myPid());
                    break;
                case ACTION_NOTIFY_NEXT:
                    try {
                        next();
                    } catch (NoMoreNextSongException e) {
                        Toast.makeText(PlayerService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ACTION_NOTIFY_PREVIOUS:
                    try {
                        previous();
                    } catch (NoMorePreviousSongException e) {
                        Toast.makeText(PlayerService.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ACTION_NOTIFY_PLAY:
                    if (isInit)
                        continuePlay();
                    else
                        play();
                    break;
                case ACTION_NOTIFY_PAUSE:
                    pause();
                    break;
            }
        }

    }
}
