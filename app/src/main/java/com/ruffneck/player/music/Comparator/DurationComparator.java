package com.ruffneck.player.music.Comparator;

import com.ruffneck.player.music.Music;


/**
 * Sort the Music list by Title.
 */
public class DurationComparator implements MusicComparator {
    @Override
    public int compare(Music lhs, Music rhs) {
        long ld = lhs.getDuration();
        long rd = rhs.getDuration();
        return (int) (ld - rd);
    }
}
