package com.ruffneck.player.utils;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class RuntimeUtils {

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> serviceInfos = manager.getRunningServices(100);
//        System.out.println("serviceClass = " + serviceClass.getName());
        for (ActivityManager.RunningServiceInfo serviceInfo : serviceInfos) {
//            System.out.println("serviceInfo = " + serviceInfo.service.getClassName());
            if (serviceInfo.service.getClassName().equals(serviceClass.getName()))
                return true;
        }
        return false;
    }
}
