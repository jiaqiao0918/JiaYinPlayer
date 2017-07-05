package com.android.jiaqiao.jiayinplayer;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;

import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/6/22/0022.
 */

public class PublicDate {
    public static ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    public static ArrayList<MusicInfo> music_play = new ArrayList<MusicInfo>();
    public static Boolean add_sheet_over = false;
    public static Boolean delete_sheet_over = false;
    public static String files_dir = "";
    public static SheetInfo temp_sheet_info=null;
    public static String separate_str = "###";
}
