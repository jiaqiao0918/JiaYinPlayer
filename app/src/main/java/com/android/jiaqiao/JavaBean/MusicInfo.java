package com.android.jiaqiao.JavaBean;

import java.io.Serializable;

/**
 * Created by jiaqiao on 2017/5/26/0026.
 */

public class MusicInfo implements Serializable {
    private String music_id;//歌曲ID
    private String music_path;//歌曲物理路径
    private String music_title;//歌曲名
    private String music_artist;//歌曲歌手名
    private String music_album;//歌曲专辑名
    private int music_duration;//歌曲总时间（单位：毫秒）
    private String music_pinyin;//歌曲名的拼音（用于排序）
    private boolean is_playing;//歌曲是否在播放
    private int add_date_time;
    private boolean is_selected;

    public MusicInfo(String music_id, String music_path, String music_title, String music_artist, String music_album, int music_duration, String music_pinyin, int add_date_time) {
        this.music_id = music_id;
        this.music_path = music_path;
        this.music_title = music_title;
        this.music_artist = music_artist;
        this.music_album = music_album;
        this.music_duration = music_duration;
        this.music_pinyin = music_pinyin;
        this.is_playing = false;
        this.add_date_time = add_date_time;
        this.is_selected = false;
    }

    public int getAdd_date_time() {
        return add_date_time;
    }

    public void setIs_selected(boolean is_selected) {
        this.is_selected = is_selected;
    }

    public boolean is_selected() {
        return is_selected;
    }

    public void setAdd_date_time(int add_date_time) {
        this.add_date_time = add_date_time;
    }

    public void setMusic_id(String music_id) {
        this.music_id = music_id;
    }

    public void setMusic_path(String music_path) {
        this.music_path = music_path;
    }

    public void setMusic_title(String music_title) {
        this.music_title = music_title;
    }

    public void setMusic_artist(String music_artist) {
        this.music_artist = music_artist;
    }

    public void setMusic_album(String music_album) {
        this.music_album = music_album;
    }

    public void setMusic_duration(int music_duration) {
        this.music_duration = music_duration;
    }

    public void setMusic_pinyin(String music_pinyin) {
        this.music_pinyin = music_pinyin;
    }

    public void setIs_playing(boolean is_playing) {
        this.is_playing = is_playing;
    }

    public String getMusic_id() {
        return music_id;
    }

    public String getMusic_path() {
        return music_path;
    }

    public String getMusic_title() {
        return music_title;
    }

    public String getMusic_artist() {
        return music_artist;
    }

    public String getMusic_album() {
        return music_album;
    }

    public int getMusic_duration() {
        return music_duration;
    }

    public String getMusic_pinyin() {
        return music_pinyin;
    }

    public boolean is_playing() {
        return is_playing;
    }
}
