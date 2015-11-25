package com.ruffneck.player.music.queue;

import android.content.Context;

import com.ruffneck.player.music.exception.NoMoreNextSongException;
import com.ruffneck.player.music.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.Music;

public class MusicLoopQueue extends MusicQueue {

    public MusicLoopQueue(Context context) {
        super(context);
    }

    @Override
    public Music next(Music currentMusic) throws NoMoreNextSongException {

        if(musicList.size() == 0)
            return null;

        if (currentMusic == null)
            return musicList.get(0);

        return currentMusic;
    }

    @Override
    public Music previous(Music currentMusic) throws NoMorePreviousSongException {

        if (currentMusic == null)
            return musicList.get(0);

        return currentMusic;
    }
}
