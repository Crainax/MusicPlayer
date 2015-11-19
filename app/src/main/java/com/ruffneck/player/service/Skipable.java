package com.ruffneck.player.service;


import com.ruffneck.player.music.Music;

/**
 * This interface contain the method that you can use to skip the current song.
 */
public interface Skipable {
    void Skip(Music music);
    void next();
    void previous();
}
