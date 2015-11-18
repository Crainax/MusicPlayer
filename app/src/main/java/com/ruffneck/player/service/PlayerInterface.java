package com.ruffneck.player.service;

public interface PlayerInterface {
    void play();
    void pause();
    void continuePlay();
    void seekTo(int process);
    boolean isPlaying();
    boolean isInit();
}
