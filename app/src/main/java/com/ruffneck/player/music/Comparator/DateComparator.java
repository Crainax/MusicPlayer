package com.ruffneck.player.music.Comparator;

import com.ruffneck.player.music.Music;


public class DateComparator implements MusicComparator {
    @Override
    public int compare(Music lhs, Music rhs) {
        long ldate = lhs.getDate();
        long rdate = rhs.getDate();

        return (int) (ldate - rdate);
    }
}
