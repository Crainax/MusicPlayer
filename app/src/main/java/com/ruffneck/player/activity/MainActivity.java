package com.ruffneck.player.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ruffneck.player.R;
import com.ruffneck.player.activity.recyclerview.DividerItemDecoration;
import com.ruffneck.player.activity.recyclerview.MusicListAdapter;
import com.ruffneck.player.exception.NoMoreNextSongException;
import com.ruffneck.player.music.Comparator.DateComparator;
import com.ruffneck.player.music.Comparator.DurationComparator;
import com.ruffneck.player.music.Comparator.NameComparator;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.music.MusicLoader;
import com.ruffneck.player.receiver.ProgressReceiver;
import com.ruffneck.player.service.CallBackServiceConnection;
import com.ruffneck.player.service.Playable;
import com.ruffneck.player.service.PlayerService;
import com.ruffneck.player.service.Skipable;
import com.ruffneck.player.utils.RuntimeUtils;
import com.ruffneck.player.utils.SnackBarUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private RecyclerView rv_list;
    private LinearLayout stateBar;

    private MainReceiver mainReceiver = new MainReceiver();
    private SeekBar sb_process;
    private SharedPreferences mPref;
    private Playable playable;
    private MusicServiceConnection conn;
    private TextView tv_bottom_song_name;
    private TextView tv_bottom_artist;
    private Button bt_pause;
    private Button bt_next;

    private View.OnClickListener playOnClickListener;
    private View.OnClickListener nextOnClickListener;

    private SeekBar.OnSeekBarChangeListener sbChangedListener;
    private Skipable skipable;
    private MusicLoader musicLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPref = getSharedPreferences("config", MODE_PRIVATE);

        musicLoader = MusicLoader.getInstance(this);

        //Start and bind the PlayService to get the Accessibility to use the service's methods.
        startAndBindService();

        initListener();

        //Find out all the views by Id.
        initViews();

        //Initialize the Toolbar
        initToolbar();

        initRecyclerView();
    }

    /**
     * Initialize all the listener.
     */
    private void initListener() {
        //Judge from the service state to set the button's text.
        playOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the service isn't running , and you need to start and play.
                if (!playable.isInit()) {
                    playable.play();
                } else if (playable.isPlaying()) {
                    //If you need to pause.
                    playable.pause();
                } else {
                    //this brunch meaning you need to continue to play the music that you pause previously.
                    playable.continuePlay();
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
                    SnackBarUtils.showExceptionSnackBar(rv_list, e, Snackbar.LENGTH_SHORT);
                }
            }
        };

        //the SeekBar's onChangedListener.
        sbChangedListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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

        registerReceiver(mainReceiver, filter);

        //refresh the UI including the button
        if (playable != null) {
            conn.boundCallback();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mainReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    /**
     * Find out all the views by Id.
     */
    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rv_list = (RecyclerView) findViewById(R.id.recycler_view);

        stateBar = (LinearLayout) findViewById(R.id.statebar);
        stateBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PlayActivity.class));
            }
        });
        sb_process = (SeekBar) stateBar.findViewById(R.id.sb_bottom_process);
        tv_bottom_song_name = (TextView) stateBar.findViewById(R.id.tv_bottom_song_name);
        tv_bottom_artist = (TextView) stateBar.findViewById(R.id.tv_bottom_artist);
        bt_pause = (Button) stateBar.findViewById(R.id.bt_bottom_pause);
        bt_next = (Button) stateBar.findViewById(R.id.bt_bottom_next);
        bt_next.setOnClickListener(nextOnClickListener);
        bt_pause.setOnClickListener(playOnClickListener);
        sb_process.setOnSeekBarChangeListener(sbChangedListener);
    }


    /**
     * Initialize the recyclerView.
     */
    private void initRecyclerView() {

        //Get the list of music information from the device.
        List<Music> musicList = MusicLoader.getInstance(this).getMusicList();
//        System.out.println("musicList = " + musicList);

        rv_list.setHasFixedSize(true);
        rv_list.setLayoutManager(new LinearLayoutManager(this));
        rv_list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        MusicListAdapter musicAdapter = new MusicListAdapter(musicList);
        rv_list.setAdapter(musicAdapter);

        musicAdapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onClickListener(View v, int position) {
//                Toast.makeText(MainActivity.this, "click" + position, Toast.LENGTH_SHORT).show();
                skipable.Skip(MusicLoader.getInstance(MainActivity.this).getMusicList().get(position));
            }

            @Override
            public void onLongClickListener(View v, int position) {
                Toast.makeText(MainActivity.this, "longClick" + position, Toast.LENGTH_SHORT).show();
            }
        });

    }


    /**
     * Initialize the toolbar as a actionbar,and set its title.
     */
    private void initToolbar() {

        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setTitle("Music List");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_mode:
                break;
            case R.id.action_sequence:


                View view = getLayoutInflater().inflate(R.layout.popup_sequence, null);

                final PopupWindow popup = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        , true);

                //When click the button tag.
                view.findViewById(R.id.tv_sequence_name).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        musicLoader.setComparator(new NameComparator());
                        Toast.makeText(MainActivity.this, "haha", Toast.LENGTH_SHORT).show();

                        popup.dismiss();
                    }
                });
                view.findViewById(R.id.tv_sequence_date).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        musicLoader.setComparator(new DateComparator());
                        popup.dismiss();
                    }
                });
                view.findViewById(R.id.tv_sequence_duration).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        musicLoader.setComparator(new DurationComparator());
                        popup.dismiss();
                    }
                });

                //Set the popupWindow's params
                popup.setTouchable(true);
                popup.setOutsideTouchable(true);
                popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_popup));
                popup.setTouchInterceptor(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return false;
                    }
                });

                //Show the popup Arch in the toolbar.
//                popup.showAsDropDown(toolbar);
                popup.showAsDropDown(toolbar,0,0, Gravity.RIGHT);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshProgress() {
        int position = mPref.getInt("position", 0);
        int duration = mPref.getInt("duration", 0);
        sb_process.setMax(duration);
        sb_process.setProgress(position);
    }

    class MainReceiver extends ProgressReceiver {

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
            Music music = intent.getExtras().getParcelable("music");

            refreshMusicInfo(music);
        }

        @Override
        public void onUpdatePosition(Intent intent) {
            int position = mPref.getInt("position", 0);
            sb_process.setProgress(position);
        }

        @Override
        public void onUpdateDuration(Intent intent) {
            int duration = mPref.getInt("duration", 0);
            sb_process.setMax(duration);
        }
    }

    /**
     * Set the StateBar's information by the music received by the service.
     *
     * @param music
     */
    private void refreshMusicInfo(Music music) {
        if (music != null) {
            tv_bottom_artist.setText(music.getArtist());
            tv_bottom_song_name.setText(music.getTitle());
        }
    }

    private class MusicServiceConnection implements CallBackServiceConnection {

        @Override
        public void boundCallback() {
            if (RuntimeUtils.isServiceRunning(MainActivity.this, PlayerService.class))
                if (playable.isPlaying())
                    bt_pause.setText(getString(R.string.bt_pause));
                else
                    bt_pause.setText(getString(R.string.bt_play));
            refreshProgress();
            refreshMusicInfo(playable.getMusic());
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playable = (Playable) service;
            skipable = (Skipable) service;
            boundCallback();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}
