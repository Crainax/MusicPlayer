package com.ruffneck.player.service;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class TimeOffService extends Service {

    Timer timer = new Timer();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final int time = intent.getIntExtra("time",0);

        if(time == 0){
            Toast.makeText(TimeOffService.this, "时间有误!", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(time);
                    android.os.Process.killProcess(android.os.Process.myPid());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },0);

        return super.onStartCommand(intent, flags, startId);
    }


}
