package com.ruffneck.player.music;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Music implements Parcelable {

    private String displayName;
    private String album;
    private int id;
    private long duration;
    private long size;
    private String artist;
    private String url;
    private String title;
    private long date;

    public Music() {
    }

    protected Music(Parcel in) {
        displayName = in.readString();
        album = in.readString();
        id = in.readInt();
        duration = in.readLong();
        size = in.readLong();
        artist = in.readString();
        url = in.readString();
        title = in.readString();
        date = in.readLong();
    }


    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }

        @Override
        public Music[] newArray(int size) {
            return new Music[size];
        }
    };


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {

        String regex = "(.*?) - (.*?)\\.mp3";
        Matcher matcher = Pattern.compile(regex).matcher(displayName);

        while(matcher.find()){
            String artist = matcher.group(1);
            String title = matcher.group(2);

            this.artist = artist;
            this.title = title;
        }

        this.displayName = displayName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {

        return artist;
    }

    public void setArtist(String artist) {

        if(this.artist != null )return;

        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(this.title != null )return;
        this.title = title;
    }

    @Override
    public String toString() {
        return "Music{" +
                "displayName='" + displayName + '\'' +
                ", album='" + album + '\'' +
                ", id=" + id +
                ", duration=" + duration +
                ", size=" + size +
                ", artist='" + artist + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Music music = (Music) o;

        return id == music.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(album);
        dest.writeInt(id);
        dest.writeLong(duration);
        dest.writeLong(size);
        dest.writeString(artist);
        dest.writeString(url);
        dest.writeString(title);
        dest.writeLong(date);
    }
}
