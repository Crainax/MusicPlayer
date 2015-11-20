package com.ruffneck.player.exception;

public class NoMorePreviousSongException extends NoMoreMusicException{
    public NoMorePreviousSongException() {
    }

    public NoMorePreviousSongException(String detailMessage) {
        super(detailMessage);
    }

    public NoMorePreviousSongException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoMorePreviousSongException(Throwable throwable) {
        super(throwable);
    }
}
