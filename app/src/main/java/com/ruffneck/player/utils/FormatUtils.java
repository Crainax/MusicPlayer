package com.ruffneck.player.utils;

/**
 * A tool Utils used to format the data.
 */
public class FormatUtils {


    /**
     * A method is used to format the millis time to a string time like "mm:ss".
     * @param millis the time to format.
     * @return the formatted String.
     */
    public static String formatTime(long millis){

        long second = millis / 1000;

        long posSecond = second%60;
        long posMin = second/60;

        return (posMin >= 10 ? posMin : "0" + posMin) + ":" +(posSecond >= 10 ? posSecond : "0" + posSecond);
    }

}
