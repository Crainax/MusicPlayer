package com.ruffneck.player.service;

import com.ruffneck.player.music.Music;

public interface Playable {
    void play();
    void pause();
    void continuePlay();
    void seekTo(int process);
    boolean isPlaying();
    boolean isInit();
    Music getMusic();

}
