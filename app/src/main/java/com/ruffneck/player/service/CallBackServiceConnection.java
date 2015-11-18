package com.ruffneck.player.service;

import android.content.ServiceConnection;

public interface CallBackServiceConnection extends ServiceConnection {
    /**
     * Remember invoked it after override the onConnection method!!!!!!!!!!
     */
    void boundCallback();
}
