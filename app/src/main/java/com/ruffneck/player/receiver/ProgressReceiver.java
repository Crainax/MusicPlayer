package com.ruffneck.player.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ruffneck.player.service.PlayerService;


/**
 * A receiver can handler the specific broadcast by implements several methods.
 */
public abstract class ProgressReceiver extends BroadcastReceiver {

    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case PlayerService.ACTION_UPDATE_POSITION:
                onUpdatePosition();
                break;
            case PlayerService.ACTION_UPDATE_DURATION:
                onUpdateDuration();
                break;
        }
    }

    public abstract void onUpdatePosition();

    public abstract void onUpdateDuration();
}