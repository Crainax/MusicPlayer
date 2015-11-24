package com.ruffneck.player.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.ruffneck.player.music.Comparator.DurationComparator;
import com.ruffneck.player.music.Comparator.MusicComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicLoader {

    public static final int MODE_LIST = 1;

    public static final int MODE_SINGLE_LOOP = 2;

    public static final int MODE_RANDOM = 3;

    private static Context mContext;

    private Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private List<Music> musicList = new ArrayList<>();

    private MusicComparator comparator = new DurationComparator();
    /**
     * The instance.
     */
    private static MusicLoader instance ;

    /**
     * Gain the Single Instance.
     * @param context the context.
     * @return the single instance of MusicLoader.
     */
    public static MusicLoader getInstance(Context context){
        if(instance == null){
            instance = new MusicLoader(context);
            mContext = context;
        }
        return instance;
    }

    private MusicLoader(Context context) {
        Cursor cursor = context.getContentResolver().query(mediaUri, null, null, null, null);
        if (cursor != null) {
            Music music;
            while (cursor.moveToNext()) {

                // Get the Column Index from each datas.
                int displayNameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
                int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int urlCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int dateCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

                String displayName = cursor.getString(displayNameCol);
                String album = cursor.getString(albumCol);
                int id = cursor.getInt(idCol);
                long duration = cursor.getLong(durationCol);
                long size = cursor.getLong(sizeCol);
                String artist = cursor.getString(artistCol);
                String url = cursor.getString(urlCol);
                String title = cursor.getString(titleCol);
                long date = cursor.getLong(dateCol);

                music = new Music();
                music.setDisplayName(displayName);
                music.setAlbum(album);
                music.setId(id);
                music.setDuration(duration);
                music.setSize(size);
                music.setArtist(artist);
                music.setUrl(url);
                music.setTitle(title);
                music.setDate(date);

                musicList.add(music);

            }

            cursor.close();
        }
        Collections.sort(musicList,comparator);
    }

    public List<Music> getMusicList(){
        return musicList;
    }

    /**
     * Resort the musicList by invoking this method.
     * @param musicComparator A comparator implements this interface.
     */
    public void setComparator(MusicComparator musicComparator){
        this.comparator = musicComparator;
        Collections.sort(musicList,musicComparator);
    }

    public Music findMusicById(int id){
        for (Music music : musicList) {
            if(music.getId() == id){
                return music;
            }
        }
        return null;
    }

}
