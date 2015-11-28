package com.ruffneck.player.music.Comparator;

import com.ruffneck.player.music.Music;

public class ArtistComparator implements MusicComparator {
    @Override
    public int compare(Music lhs, Music rhs) {

        String l = lhs.getArtist();
        String r = rhs.getArtist();

        return l.toUpperCase().compareTo(r.toUpperCase());
    }
}
