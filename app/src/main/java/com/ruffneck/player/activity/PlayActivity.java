package com.ruffneck.player.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ruffneck.player.R;
import com.ruffneck.player.exception.NoMoreNextSongException;
import com.ruffneck.player.exception.NoMorePreviousSongException;
import com.ruffneck.player.receiver.ProgressReceiver;
import com.ruffneck.player.service.CallBackServiceConnection;
import com.ruffneck.player.service.Playable;
import com.ruffneck.player.service.PlayerService;
import com.ruffneck.player.service.Skipable;
import com.ruffneck.player.utils.FormatUtils;
import com.ruffneck.player.utils.RuntimeUtils;
import com.ruffneck.player.utils.SnackBarUtils;


public class PlayActivity extends AppCompatActivity {

    private Button bt_pause;
    private Button bt_next;
    private Button bt_previous;
    private TextView tv_process;

    private SeekBar sb_process;

    private MusicServiceConnection conn;
    private Playable playable;
    private Skipable skipable;
    private SharedPreferences mPref;

    private PlayReceiver progressReceiver = new PlayReceiver();

    private View.OnClickListener playOnClickListener;
    private View.OnClickListener nextOnClickListener;
    private View.OnClickListener PreviousOnclickListener;

    private SeekBar.OnSeekBarChangeListener sbChangedListener;
    private View parentView;
    //    private static PlayActivity pa;

/*    public static class PlayHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {

            Bundle data = msg.getData();
            int duration = data.getInt("duration") / 1000;
            int position = data.getInt("position") / 1000;

            sb_process.setProgress(position);
            sb_process.setMax(duration);

            int posSecond = position % 60;
            int posMin = position / 60;
            int durSecond = duration % 60;
            int durMin = duration / 60;

            tv_process.setText(pa.getString(R.string.play_process, posMin,
                    posSecond, durMin, durSecond));

            super.handleMessage(msg);
        }

    }*/

//    public static PlayHandler mHandler = new PlayHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

//        pa = this;

        //Initialize the SharedPreference.
        mPref = getSharedPreferences("config", MODE_PRIVATE);

        startAndBindService();

        //Initialize all the listener.
        initListener();
        //Initialize the button and all the views.
        initView();


    }

    /**
     * Initialize all the listener.
     */
    private void initListener() {

        //Judge from the service state to set the button's text.
        playOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pause = getString(R.string.bt_pause);
                String play = getString(R.string.bt_play);
                //if the service isn't running , and you need to start and play.
                if (!playable.isInit()) {
                    playable.play();
                    bt_pause.setText(pause);
                } else if (playable.isPlaying()) {
                    //If you need to pause.
                    playable.pause();
                    bt_pause.setText(play);
                } else {
                    //this brunch meaning you need to continue to play the music that you pause previously.
                    playable.continuePlay();
                    bt_pause.setText(pause);
                }


            }
        };

        //Control the state of the current music.
        nextOnClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    skipable.next();
                } catch (NoMoreNextSongException e) {
                    SnackBarUtils.showExceptionSnackBar(parentView, e, Snackbar.LENGTH_SHORT);
                }
            }
        };
        PreviousOnclickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    skipable.previous();
                } catch (NoMorePreviousSongException e) {
                    SnackBarUtils.showExceptionSnackBar(parentView, e, Snackbar.LENGTH_SHORT);
                }
            }
        };

        //the SeekBar's onChangedListener.
        sbChangedListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println("PlayActivity.onProgressChanged");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                System.out.println("PlayActivity.onStartTrackingTouch");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println("PlayActivity.onStopTrackingTouch");
                playable.seekTo(seekBar.getProgress());
                refreshProgress();
            }
        };

    }


    /**
     * Start and bind the PlayService to get the Accessibility to use the service's methods.
     */
    private void startAndBindService() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        startService(serviceIntent);
        conn = new MusicServiceConnection();
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        String[] actionList = PlayerService.actionList;

        for (String action : actionList) {
            filter.addAction(action);
        }

        registerReceiver(progressReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(progressReceiver);
    }

    /**
     * Establish the connection to the service.
     */
    class MusicServiceConnection implements CallBackServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playable = (Playable) service;
            skipable = (Skipable) service;
            boundCallback();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void boundCallback() {
            //refresh the button's text rely on the media playable's state.
            if (RuntimeUtils.isServiceRunning(PlayActivity.this, PlayerService.class))
                if (playable.isPlaying())
                    bt_pause.setText(getString(R.string.bt_pause));
                else
                    bt_pause.setText(getString(R.string.bt_play));
            refreshProgress();
        }
    }


    private void initView() {

        parentView = findViewById(R.id.parent_play);

        bt_pause = (Button) findViewById(R.id.bt_pause);
        bt_pause.setOnClickListener(playOnClickListener);


        bt_next = (Button) findViewById(R.id.bt_next);
        bt_next.setOnClickListener(nextOnClickListener);
        bt_previous = (Button) findViewById(R.id.bt_previous);
        bt_previous.setOnClickListener(PreviousOnclickListener);


        sb_process = (SeekBar) findViewById(R.id.sb_process);
        sb_process.setOnSeekBarChangeListener(sbChangedListener);

        tv_process = (TextView) findViewById(R.id.tv_process);

        refreshProgress();
    }

    /**
     * refresh the seekBar and the textView for the time rely on the sharedPreference.
     * <br /><br />
     * <strong>when you find that the UI doesn't fresh because the Receiver receiving the intent
     * during it's interval,you can invoke this method.</strong>
     */
    private void refreshProgress() {
        //refresh the seekBar and the textView for the time rely on the sharedPreference.
        int position = mPref.getInt("position", 0);
        int duration = mPref.getInt("duration", 0);
        sb_process.setMax(duration);
        sb_process.setProgress(position);
        tv_process.setText(getString(R.string.play_process, FormatUtils.formatTime(position),
                FormatUtils.formatTime(duration)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        Toast.makeText(PlayActivity.this, "Destroy", Toast.LENGTH_SHORT).show();
        unbindService(conn);
    }


    /**
     * The Receiver is used to update the current progress.
     */
    private class PlayReceiver extends ProgressReceiver {

        @Override
        public void onContinuePlay(Intent intent) {

            String pause = getString(R.string.bt_pause);
            bt_pause.setText(pause);

        }

        @Override
        public void onPause(Intent intent) {
            String play = getString(R.string.bt_play);
            bt_pause.setText(play);
        }

        @Override
        public void onPlay(Intent intent) {

            String pause = getString(R.string.bt_pause);
            bt_pause.setText(pause);
        }

        @Override
        public void onSkipSong(Intent intent) {
            refreshProgress();
        }

        @Override
        public void onUpdatePosition(Intent intent) {
            int position = mPref.getInt("position", 0);
            int duration = mPref.getInt("duration", 0);
            sb_process.setProgress(position);
            tv_process.setText(getString(R.string.play_process, FormatUtils.formatTime(position),
                    FormatUtils.formatTime(duration)));
        }

        @Override
        public void onUpdateDuration(Intent intent) {
            int position = mPref.getInt("position", 0);
            int duration = mPref.getInt("duration", 0);
            sb_process.setMax(duration);
            tv_process.setText(getString(R.string.play_process, FormatUtils.formatTime(position),
                    FormatUtils.formatTime(duration)));
        }

    }
}
