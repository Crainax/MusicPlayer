package com.ruffneck.player.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ruffneck.player.R;
import com.ruffneck.player.service.PlayerInterface;
import com.ruffneck.player.service.PlayerService;
import com.ruffneck.player.utils.FormatUtils;


public class PlayActivity extends AppCompatActivity {

    private Button bt_pause;
    private Button bt_next;
    private Button bt_previous;
    private TextView tv_process;

    private SeekBar sb_process;

    private MusicServiceConnection conn;
    private PlayerInterface player;
    private SharedPreferences mPref;

    private ProgressReceiver progressReceiver = new ProgressReceiver();
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

    ;

//    public static PlayHandler mHandler = new PlayHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

//        pa = this;

        //Initialize the SharedPreference.
        mPref = getSharedPreferences("config", MODE_PRIVATE);

        //Initialize the button and all the views.
        initView();

        Intent serviceIntent = new Intent(this, PlayerService.class);
        startService(serviceIntent);
        conn = new MusicServiceConnection();
        bindService(serviceIntent, conn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayerService.ACTION_UPDATE);
        registerReceiver(progressReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(progressReceiver);
    }

    /**
     * Establish the connection to the service.
     */
    class MusicServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            player = (PlayerInterface) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

    }


    private void initView() {
        bt_pause = (Button) findViewById(R.id.bt_pause);

        if (mPref.getInt("state", PlayerService.STATE_STOP) == PlayerService.STATE_PLAYING)
            bt_pause.setText("暂停");
        else
            bt_pause.setText("播放");

        bt_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mPref.getInt("state", PlayerService.STATE_STOP)) {
                    case PlayerService.STATE_PLAYING:
                        player.pause();
                        bt_pause.setText("播放");
                        break;
                    default:
                        player.play();
                        bt_pause.setText("暂停");
                        break;
                }
            }
        });

        bt_next = (Button) findViewById(R.id.bt_next);

        bt_previous = (Button) findViewById(R.id.bt_next);

        sb_process = (SeekBar) findViewById(R.id.sb_process);

        sb_process.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekTo(seekBar.getProgress());
            }
        });

        tv_process = (TextView) findViewById(R.id.tv_process);
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
    class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            switch (action) {
                case PlayerService.ACTION_UPDATE:
                    //Gain the date from the Sender.
                    Bundle data = intent.getExtras();
                    int duration = data.getInt("duration");
                    int position = data.getInt("position");

//                    System.out.println("ProgressReceiver.onReceive");
//                    System.out.println("position = " + position);
                    //update the UI.
                    sb_process.setProgress(position);
                    sb_process.setMax(duration);
                    tv_process.setText(getString(R.string.play_process, FormatUtils.formatTime(position),
                            FormatUtils.formatTime(duration)));
                    break;
            }
        }

    }
}
