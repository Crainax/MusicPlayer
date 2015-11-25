package com.ruffneck.player.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ruffneck.player.R;
import com.ruffneck.player.activity.recyclerview.DividerItemDecoration;
import com.ruffneck.player.activity.recyclerview.MusicListAdapter;
import com.ruffneck.player.music.Comparator.DateComparator;
import com.ruffneck.player.music.Comparator.DurationComparator;
import com.ruffneck.player.music.Comparator.NameComparator;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.music.MusicLoader;
import com.ruffneck.player.music.exception.NoMoreNextSongException;
import com.ruffneck.player.music.queue.MusicArrayQueue;
import com.ruffneck.player.music.queue.MusicLoopQueue;
import com.ruffneck.player.music.queue.MusicRandomQueue;
import com.ruffneck.player.receiver.ProgressReceiver;
import com.ruffneck.player.service.CallBackServiceConnection;
import com.ruffneck.player.service.Playable;
import com.ruffneck.player.service.PlayerService;
import com.ruffneck.player.service.Skipable;
import com.ruffneck.player.utils.FormatUtils;
import com.ruffneck.player.utils.RuntimeUtils;
import com.ruffneck.player.utils.SnackBarUtils;
import com.ruffneck.player.view.MusicDetailView;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rv_list;

    private MainReceiver mainReceiver = new MainReceiver();
    private SeekBar sb_process;
    private SharedPreferences mPref;
    private Playable playable;
    private MusicServiceConnection conn;
    private TextView tv_bottom_song_name;
    private TextView tv_bottom_artist;
    private Button bt_pause;

    private View.OnClickListener playOnClickListener;
    private View.OnClickListener nextOnClickListener;

    private SeekBar.OnSeekBarChangeListener sbChangedListener;
    private Skipable skipable;
    private MusicLoader musicLoader;
    private MusicListAdapter musicAdapter;

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

        LinearLayout stateBar = (LinearLayout) findViewById(R.id.statebar);
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
        Button bt_next = (Button) stateBar.findViewById(R.id.bt_bottom_next);
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

        musicAdapter = new MusicListAdapter(musicList);
        rv_list.setAdapter(musicAdapter);


        musicAdapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onClickListener(View v, int position) {
//                Toast.makeText(MainActivity.this, "click" + position, Toast.LENGTH_SHORT).show();
                skipable.Skip(musicLoader.getMusicList().get(position));
            }

            @Override
            public void onLongClickListener(View v, final int position) {
//                Toast.makeText(MainActivity.this, "longClick" + position, Toast.LENGTH_SHORT).show();
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v, Gravity.END);

                getMenuInflater().inflate(R.menu.popup_longclick_music, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_play:
                                skipable.Skip(musicLoader.getMusicList().get(position));
                                break;
                            case R.id.action_delete:
                                deleteMusic(musicLoader.getMusicList().get(position));
                                break;
                            case R.id.action_detail:
                                showPopupDetailWindow(musicLoader.getMusicList().get(position));
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }


    /**
     * Delete the specific music.
     *
     * @param music the music need to be deleted.
     */
    private void deleteMusic(final Music music) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (playable.getMusic().equals(music)) {
            SnackBarUtils.showStringSnackBar(rv_list, "Delete Failed - The music is playing!", Snackbar.LENGTH_LONG);
            return;
        }

        builder.setTitle("删除歌曲");
        builder.setMessage("你确认要删除\"" + music.getTitle() + "\"吗?");

        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    musicLoader.getMusicList().remove(music);
                    musicAdapter.notifyDataSetChanged();
                    //send the broadcast to notify system update the media database.
                    File file = new File(music.getUrl());
                    if (file.delete()) {
                        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        intent.setData(Uri.fromFile(file));
                        sendBroadcast(intent);
                    }
                    SnackBarUtils.showStringSnackBar(rv_list, "Delete Succeed!", Snackbar.LENGTH_LONG);


            }
        });
        builder.setNegativeButton("取消", null);

        builder.show();

    }

    /**
     * Initialize the toolbar as a actionbar,and set its title.
     */
    private void initToolbar() {

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setTitle("Music List");
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Put the outside item into this method to handle it centrally.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_mode:
                showPopupModeWindow();
                break;
            case R.id.action_sequence:
                showPopupSequenceWindow();
                break;
            case R.id.action_play:
                Toast.makeText(MainActivity.this, "sadfasdf", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Show the music info detail in the popupWindow sliding from the bottom.
     */
    private void showPopupDetailWindow(Music music) {

        View view = getLayoutInflater().inflate(R.layout.popup_music_detail, null);

        ((MusicDetailView)view.findViewById(R.id.mdv_song_name)).setContent(music.getTitle());
        ((MusicDetailView)view.findViewById(R.id.mdv_album)).setContent(music.getAlbum());
        ((MusicDetailView)view.findViewById(R.id.mdv_artist)).setContent(music.getArtist());
        ((MusicDetailView)view.findViewById(R.id.mdv_duration)).setContent(FormatUtils.formatTime(music.getDuration()));
        ((MusicDetailView)view.findViewById(R.id.mdv_size)).setContent(FormatUtils.formatSize(music.getSize()));
        ((MusicDetailView)view.findViewById(R.id.mdv_url)).setContent(music.getUrl());

        PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);

        //Set the popupWindow's params.
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(getWindow().getDecorView(),
                Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,0);

    }

    /**
     * Show the popupWindow that indicating the sequence.
     */
    private void showPopupSequenceWindow() {
        View view = getLayoutInflater().inflate(R.layout.popup_sequence, null);

        final PopupWindow popup = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                , true);

        //When click the button tag.
        TextView tv_sequence_name = (TextView) view.findViewById(R.id.tv_sequence_name);
        TextView tv_sequence_date = (TextView) view.findViewById(R.id.tv_sequence_date);
        TextView tv_sequence_duration = (TextView) view.findViewById(R.id.tv_sequence_duration);

        tv_sequence_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicLoader.setComparator(new NameComparator());
                musicAdapter.notifyDataSetChanged();
                popup.dismiss();
                SnackBarUtils.showStringSnackBar(rv_list,
                        ((TextView) v).getText().toString(), Snackbar.LENGTH_SHORT);

            }
        });
        tv_sequence_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicLoader.setComparator(new DateComparator());
                musicAdapter.notifyDataSetChanged();
                popup.dismiss();
                SnackBarUtils.showStringSnackBar(rv_list,
                        ((TextView) v).getText().toString(), Snackbar.LENGTH_SHORT);
            }
        });
        tv_sequence_duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicLoader.setComparator(new DurationComparator());
                musicAdapter.notifyDataSetChanged();
                popup.dismiss();
                SnackBarUtils.showStringSnackBar(rv_list,
                        ((TextView) v).getText().toString(), Snackbar.LENGTH_SHORT);
            }
        });

        //Set the popupWindow's params
        popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_popup));
/*
        popup.setTouchable(true);
        popup.setOutsideTouchable(true);
        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
*/

//        Show the popup Arch in the toolbar.
//                popup.showAsDropDown(toolbar);
        popup.showAsDropDown(toolbar, 0, 0, Gravity.END);

/*
        popup.showAtLocation(getWindow().getDecorView(),
                Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 0);*/
    }

    /**
     * Show the popupWindow that indicating the play mode.
     */
    private void showPopupModeWindow() {
        View view = getLayoutInflater().inflate(R.layout.popup_mode, null);

        final PopupWindow popup = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                , true);

        //When click the button tag.
        view.findViewById(R.id.tv_mode_array).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipable.setQueue(new MusicArrayQueue(MainActivity.this));
                popup.dismiss();
                SnackBarUtils.showStringSnackBar(rv_list,
                        ((TextView) v).getText().toString(), Snackbar.LENGTH_SHORT);
            }
        });
        view.findViewById(R.id.tv_mode_loop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipable.setQueue(new MusicLoopQueue(MainActivity.this));
                popup.dismiss();
                SnackBarUtils.showStringSnackBar(rv_list,
                        ((TextView) v).getText().toString(), Snackbar.LENGTH_SHORT);
            }
        });
        view.findViewById(R.id.tv_mode_random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipable.setQueue(new MusicRandomQueue(MainActivity.this));
                popup.dismiss();
                SnackBarUtils.showStringSnackBar(rv_list,
                        ((TextView) v).getText().toString(), Snackbar.LENGTH_SHORT);
            }
        });

/*        popup.setTouchable(true);
        popup.setOutsideTouchable(true);
        popup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });*/

        //Set the popupWindow's params
        popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_popup));
        //Show the popup Arch in the toolbar.
//                popup.showAsDropDown(toolbar);
        popup.showAsDropDown(toolbar, 0, 0, Gravity.END);
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
