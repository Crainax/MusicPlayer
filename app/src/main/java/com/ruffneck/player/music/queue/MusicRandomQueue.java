package com.ruffneck.player.music.queue;

import android.content.Context;

import com.ruffneck.player.music.exception.NoMoreNextSongException;
import com.ruffneck.player.music.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.Music;

import java.util.Random;

public class MusicRandomQueue extends MusicQueue {

    public MusicRandomQueue(Context context) {
        super(context);
    }

    @Override
    public Music next(Music currentMusic) throws NoMoreNextSongException {
        int size = musicList.size();
        if(size == 0)
            throw new NoMoreNextSongException("No more next song!");

        int index = new Random().nextInt(size);

        return musicList.get(index);
    }

    @Override
    public Music previous(Music currentMusic) throws NoMorePreviousSongException {

        int size = musicList.size();
        if(size == 0)
            throw new NoMorePreviousSongException("No more next song!");

        int index = new Random().nextInt(size);

        return musicList.get(index);
    }
}
