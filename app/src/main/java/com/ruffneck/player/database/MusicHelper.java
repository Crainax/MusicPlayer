package com.ruffneck.player.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MusicHelper extends SQLiteOpenHelper {

    public MusicHelper(Context context) {
        super(context, "music.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table music(_id integer primary key autoincrement," +
                "imgUrl text," +
                "displayName text," +
                "album text," +
                "duration long," +
                "size long," +
                "artist text," +
                "url text," +
                "title text," +
                "date long)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
