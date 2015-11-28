package com.ruffneck.player.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ruffneck.player.database.MusicHelper;
import com.ruffneck.player.music.Music;

public class DatabaseUtils {

    public static String findImgUrlByArtist(Context context, String artist) {

        MusicHelper mh = new MusicHelper(context);
        SQLiteDatabase database = mh.getReadableDatabase();

        String string = null;
        Cursor cursor = database.query("music", new String[]{"imgUrl"}, "artist=?", new String[]{artist}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                string = cursor.getString(0);
            }
            cursor.close();
        }

        database.close();
        mh.close();

        return string;
    }

    public static void insertMusicImgUrl(Context context, Music music) {

        if (music.getImgUrl() == null)
            return;

        MusicHelper mh = new MusicHelper(context);
        SQLiteDatabase database = mh.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("artist", music.getArtist());
        values.put("imgUrl",music.getImgUrl());

        database.insert("music", null, values);

        database.close();
        mh.close();

    }

}
