package com.ruffneck.player.service;


import com.ruffneck.player.exception.NoMoreNextSongException;
import com.ruffneck.player.exception.NoMorePreviousSongException;
import com.ruffneck.player.music.Music;
import com.ruffneck.player.music.queue.MusicQueue;

/**
 * This interface contain the method that you can use to skip the current song.
 */
public interface Skipable {
    void Skip(Music music);
    void next() throws NoMoreNextSongException;
    void previous() throws NoMorePreviousSongException;
    void setQueue(MusicQueue musicQueue);
}
