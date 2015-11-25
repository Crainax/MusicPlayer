package com.ruffneck.player.music.queue;

import android.content.Context;

import com.ruffneck.player.music.exception.NoMoreNextSongException;
import com.ruffneck.player.music.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.Music;

public class MusicArrayQueue extends MusicQueue {

    public MusicArrayQueue(Context context) {
        super(context);
    }

    @Override
    public Music next(Music currentMusic) throws NoMoreNextSongException {
        int index;
        if (currentMusic == null)
            index = -1;
        else
            index = musicList.indexOf(currentMusic);

        Music music = null;
//        if(index == -1){
//            System.out.println("index =-1!");
//        }
        try {
            music = musicList.get(index + 1);
        } catch (IndexOutOfBoundsException e) {
            throw new NoMoreNextSongException("No more next song!");
        }

        return music;
    }

    @Override
    public Music previous(Music currentMusic) throws NoMorePreviousSongException {

        int index;
        if (currentMusic == null)
            index = -1;
        else
            index = musicList.indexOf(currentMusic);

        Music music = null;
//        if(index == -1){
//            System.out.println("index =-1!");
//        }
        try {
            music = musicList.get(index - 1);
        } catch (IndexOutOfBoundsException e) {
            throw new NoMorePreviousSongException("No more previous song!");
        }


        return music;
    }
}
