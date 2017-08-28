package com.android.jiaqiao.jiayinplayer;

import android.graphics.Bitmap;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.Service.MusicPlayService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jiaqiao on 2017/6/22/0022.
 */

public class PublicDate {
    public static ArrayList<MusicInfo> public_music_all = new ArrayList<MusicInfo>();
    public static ArrayList<MusicInfo> music_play = new ArrayList<MusicInfo>();
    public static ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();


    public static Boolean add_sheet_over = false;
    public static Boolean delete_sheet_over = false;
    public static String files_dir = "";
    public static SheetInfo temp_sheet_info = null;
    public static String separate_str = "###";
    public static boolean is_select_music_over = false;
    public static boolean update_music_sheet = false;
    public static int play_mode = MusicPlayService.PLAY_MODE_ORDER;
    public static ArrayList<Integer> play_randoms = new ArrayList<Integer>();


    public static boolean is_music_play_destroy = false;
    public static boolean is_service_select_music_destroy = false;
    public static boolean is_timing_destroy = false;
    public static boolean is_play = false;
    public static boolean is_notification_running = false;
    public static boolean is_timing_time = false;

    /*专辑图片虚化
         * 增大scaleRatio缩放比，使用一样更小的bitmap去虚化可以得到更好的模糊效果，而且有利于占用内存的减小；
		 * 增大blurRadius，可以得到更高程度的虚化，不过会导致CPU更加intensive
		 */
    public static int scaleRatio = 20;
    public static int blurRadius = 5;

    public static String music_edit_temp_sheet_id = "";
    public static ArrayList<MusicInfo> public_music_edit_temp = new ArrayList<MusicInfo>();
    public static ArrayList<Integer> public_music_edit_temp_select = new ArrayList<Integer>();

    public static int public_drawer_center_bottom_view_height = 0;

    public static MusicInfo music_play_now;
    public static String music_play_list_str = null;
    public static int music_play_list_position = 0;

    public static File path_files_dir = null;
    public static boolean is_music_play = false;
    public static boolean update_music_play = false;

    public static Bitmap music_last_album_image_bitmap = null;

    //定时，定曲
    public static int all_timing_time = 100;//单位：秒，计时时间
    public static int all_timing_music_sum=0;
    public static boolean is_timing_over_finish=false;
}
