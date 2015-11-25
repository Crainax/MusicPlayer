package com.ruffneck.player.music.queue;

import android.content.Context;

import com.ruffneck.player.music.exception.NoMoreNextSongException;
import com.ruffneck.player.music.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.music.MusicLoader;

import java.util.List;

public abstract class MusicQueue {

    protected List<Music> musicList;

    public MusicQueue(Context context){
        musicList = MusicLoader.getInstance(context).getMusicList();
    }

    public abstract Music next(Music currentMusic) throws NoMoreNextSongException;


    public abstract Music previous(Music currentMusic) throws NoMorePreviousSongException;


}
