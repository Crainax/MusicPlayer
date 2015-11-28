package com.ruffneck.player.task;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.ruffneck.player.music.Music;
import com.ruffneck.player.utils.DatabaseUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;


/**
 * A asyncTask is used to crawl the image URL from the YinYueTai and save the url in to the database in
 * order to recycle to use.
 */
public class LoadImageTask extends AsyncTask<Music, Music, Void> {

    private final Context mContext;

    private UpdateCallBack updateCallBack;
    private UpdateFailCallBack updateFailCallBack;

    public interface UpdateCallBack {
        void onUpdate(Music music);
    }

    public interface UpdateFailCallBack {
        void onUpdateFail(Music music);
    }
    public LoadImageTask(Context context) {
        mContext = context;
    }

    public void setUpdateCallBack(UpdateCallBack updateCallBack) {
        this.updateCallBack = updateCallBack;
    }

    public void setUpdateFailCallBack(UpdateFailCallBack updateFailCallBack) {
        this.updateFailCallBack = updateFailCallBack;
    }

    @Override
    protected Void doInBackground(Music... musics) {

        for (int i = 0; i < musics.length; i++) {

            if (musics[i].getImgUrl() == null) {

                String artist = musics[i].getArtist().split("、")[0];

                String imgUrl = DatabaseUtils.findImgUrlByArtist(mContext, artist);

                if (imgUrl == null) {

                    Document document = null;
                    try {
                        document = Jsoup.connect("http://so.yinyuetai.com/mv?keyword=" +
                                artist.replaceAll(" ", "+"))
                                .userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.93 Safari/537.36")
                                .get();
                        String img = document.select("img.photo").attr("src");
                        musics[i].setImgUrl(img);
                        DatabaseUtils.insertMusicImgUrl(mContext, musics[i]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    musics[i].setImgUrl(imgUrl);
                }
            }
//            System.out.println("musics = " + musics[i].getImgUrl());
            publishProgress(musics[i]);
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Music... position) {
        super.onProgressUpdate(position);

        if (!TextUtils.isEmpty(position[0].getImgUrl()))
            updateCallBack.onUpdate(position[0]);
        else
            updateFailCallBack.onUpdateFail(position[0]);

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }
}
