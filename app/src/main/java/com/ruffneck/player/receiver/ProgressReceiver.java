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
                onUpdatePosition(intent);
                break;
            case PlayerService.ACTION_UPDATE_DURATION:
                onUpdateDuration(intent);
                break;
            case PlayerService.ACTION_SKIP_SONG:
                onSkipSong(intent);
                break;
            case PlayerService.ACTION_PLAY:
                onPlay(intent);
                break;
            case PlayerService.ACTION_PAUSE:
                onPause(intent);
                break;
            case PlayerService.ACTION_CONTINUE_PLAY:
                onContinuePlay(intent);
                break;
        }
    }

    public abstract void onContinuePlay(Intent intent);

    public abstract void onPause(Intent intent);

    public abstract void onPlay(Intent intent);

    public abstract void onSkipSong(Intent intent);

    public abstract void onUpdatePosition(Intent intent);

    public abstract void onUpdateDuration(Intent intent);
}
