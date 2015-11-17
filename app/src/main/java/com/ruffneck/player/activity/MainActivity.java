package com.ruffneck.player.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.ruffneck.player.R;
import com.ruffneck.player.activity.recyclerview.DividerItemDecoration;
import com.ruffneck.player.activity.recyclerview.MusicListAdapter;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.music.MusicLoader;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ActionBar actionBar;
    private RecyclerView rv_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Find out all the views by Id.
        initViews();

        //Initialize the Toolbar
        initToolbar();

        initRecyclerView();
    }

    /**
     * Find out all the views by Id.
     */
    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        rv_list = (RecyclerView) findViewById(R.id.recycler_view);
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


}
