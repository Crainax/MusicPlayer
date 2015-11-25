package com.ruffneck.player.music.exception;

public class NoMoreMusicException extends Exception{

    public NoMoreMusicException() {
    }

    public NoMoreMusicException(String detailMessage) {
        super(detailMessage);
    }

    public NoMoreMusicException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoMoreMusicException(Throwable throwable) {
        super(throwable);
    }
}
