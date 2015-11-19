package com.ruffneck.player.music.Comparator;

import com.ruffneck.player.music.Music;


/**
 * Sort the Music list by Title.
 */
public class NameComparator implements MusicComparator {
    @Override
    public int compare(Music lhs, Music rhs) {
        String l = lhs.getTitle();
        String r = rhs.getTitle();
        return l.compareTo(r);
    }
}
