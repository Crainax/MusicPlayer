package com.ruffneck.player.music.exception;

public class NoMoreNextSongException extends NoMoreMusicException {
    public NoMoreNextSongException() {
    }

    public NoMoreNextSongException(String detailMessage) {
        super(detailMessage);
    }

    public NoMoreNextSongException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoMoreNextSongException(Throwable throwable) {
        super(throwable);
    }
}
