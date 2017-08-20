package com.android.jiaqiao.jiayinplayer;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jiaqiao on 2017/6/22/0022.
 */

public class PublicDate {
    public static ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    public static ArrayList<MusicInfo> music_play = new ArrayList<MusicInfo>();
    public static ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();


    public static Boolean add_sheet_over = false;
    public static Boolean delete_sheet_over = false;
    public static String files_dir = "";
    public static SheetInfo temp_sheet_info = null;
    public static String separate_str = "###";
    public static boolean is_select_music_over = false;


    public static boolean is_service_select_music_destroy = false;

    /*专辑图片虚化
         * 增大scaleRatio缩放比，使用一样更小的bitmap去虚化可以得到更好的模糊效果，而且有利于占用内存的减小；
		 * 增大blurRadius，可以得到更高程度的虚化，不过会导致CPU更加intensive
		 */
    public static int scaleRatio = 20;
    public static int blurRadius = 5;

    public static String music_edit_temp_sheet_id = "";
    public static ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
    public static ArrayList<Integer> music_edit_temp_select = new ArrayList<Integer>();
}
