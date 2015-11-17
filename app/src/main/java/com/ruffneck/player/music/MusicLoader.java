package com.ruffneck.player.music;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MusicLoader {

    private static Context mContext;

    private Uri mediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private List<Music> musicList = new ArrayList<>();

    /**
     * The instance.
     */
    private static MusicLoader instance ;

    /**
     * Gain the Single Instance.
     * @param context the context.
     * @return
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

                String displayName = cursor.getString(displayNameCol);
                String album = cursor.getString(albumCol);
                int id = cursor.getInt(idCol);
                long duration = cursor.getLong(durationCol);
                long size = cursor.getLong(sizeCol);
                String artist = cursor.getString(artistCol);
                String url = cursor.getString(urlCol);
                String title = cursor.getString(titleCol);

                music = new Music();
                music.setDisplayName(displayName);
                music.setAlbum(album);
                music.setId(id);
                music.setDuration(duration);
                music.setSize(size);
                music.setArtist(artist);
                music.setUrl(url);
                music.setTitle(title);

                musicList.add(music);

            }

            cursor.close();
        }
    }

    public List<Music> getMusicList(){
        return musicList;
    }

}
