package com.ruffneck.player.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;


/**
 * This service is used to listen the telephony state in order to pause or continue to play.
 */
public class TelephonyService extends Service {

    private TelephonyManager tm;

    private CallListener listener = new CallListener();
    private OutcallReceiver receiver;


    //The first time start it ,it will switch to the idle case.
    private boolean isFirst = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        receiver = new OutcallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(receiver,filter);
    }

    private class CallListener extends PhoneStateListener{

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Intent intent = null;
            switch (state){
                case TelephonyManager.CALL_STATE_IDLE:
                    if(isFirst){
                        isFirst = false;
                        return;
                    }
                    intent = new Intent(PlayerService.ACTION_NOTIFY_PLAY);
                    sendBroadcast(intent);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    intent = new Intent(PlayerService.ACTION_NOTIFY_PAUSE);
                    sendBroadcast(intent);
                    break;
            }

            super.onCallStateChanged(state, incomingNumber);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tm.listen(listener,PhoneStateListener.LISTEN_NONE);
        unregisterReceiver(receiver);
    }

    private class OutcallReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            intent = new Intent(PlayerService.ACTION_NOTIFY_PAUSE);
            sendBroadcast(intent);
        }
    }
}
