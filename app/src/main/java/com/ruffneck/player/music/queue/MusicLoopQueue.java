package com.ruffneck.player.music.queue;

import android.content.Context;

import com.ruffneck.player.exception.NoMoreNextSongException;
import com.ruffneck.player.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.Music;

public class MusicLoopQueue extends MusicQueue {

    public MusicLoopQueue(Context context) {
        super(context);
    }

    @Override
    public Music next(Music currentMusic) throws NoMoreNextSongException {

        return currentMusic;
    }

    @Override
    public Music previous(Music currentMusic) throws NoMorePreviousSongException {


        return currentMusic;
    }
}
