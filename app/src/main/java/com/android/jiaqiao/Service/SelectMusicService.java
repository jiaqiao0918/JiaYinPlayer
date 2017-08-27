package com.android.jiaqiao.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Utils.DataInfoCache;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.File;
import java.text.Collator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by jiaqiao on 2017/8/2/0002.
 */

public class SelectMusicService extends Service {

    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> all_list = new ArrayList<MusicInfo>();
    private boolean is_update_this = false;

    private String sd_path = Environment.getExternalStorageDirectory().getPath();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PublicDate.is_select_music_over = false;
        PublicDate.is_service_select_music_destroy = false;
    }

    public String getNowTime() {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String time = format.format(date);
        return time;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                ArrayList<MusicInfo> list_temp = new ArrayList<MusicInfo>();
                list_temp.addAll(PublicDate.public_music_all);
                music_all.clear();
                getAllMusic();
                listSortId(all_list);
                listSortId(list_temp);
                music_all = all_list;
                if (list_temp.size() != music_all.size()) {
                    is_update_this = true;
                } else {
                    int num = 0;
                    boolean is_is = true;
                    for (int i = 0; i < music_all.size(); i++) {
                        if (music_all.get(i).getMusic_id() == list_temp.get(i).getMusic_id() && music_all.get(i).getMusic_title().trim().equals(list_temp.get(i).getMusic_title().trim())) {
                            is_is = true;
                            num++;
                        } else {
                            is_is = false;
                            break;
                        }
                    }
                    if (!is_is) {
                        is_update_this = true;
                    } else {
                        is_update_this = false;
                    }
                }
                listSortPinYin(music_all);
                PublicDate.public_music_all.clear();
                PublicDate.public_music_all.addAll(music_all);
                PublicDate.list_folder_all.clear();
                PublicDate.list_folder_all.addAll(listToFolder(music_all));
                DataInfoCache.saveListCache(getApplicationContext(), music_all, "music_all");
                DataInfoCache.saveListCache(getApplicationContext(), PublicDate.list_folder_all, "list_folder_all");

                if (is_update_this) {
                    //发送广播
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MainActivity.ALL_MUSIC_UPDATE);
                    temp_intent.putExtra("is_update", true);
                    sendBroadcast(temp_intent);

                }

                PublicDate.is_select_music_over = true;
                PublicDate.is_service_select_music_destroy = false;
                this.interrupt();//中断线程
            }
        };
        thread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PublicDate.is_select_music_over = true;
        PublicDate.is_service_select_music_destroy = true;
    }

    //从SD卡中读取音频文件，无序
    public void getAllMusic() {
        all_list.clear();
        int big_time = 30 * 1000;//过滤小于30秒
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };
        String where = "mime_type in ('audio/x-wav','application/ogg','audio/mp4','audio/flac','audio/x-ms-wma','audio/x-monkeys-audio','audio/aac','audio/ac3','audio/mpeg')and duration>" + big_time + " and is_music > 0 ";
        ContentResolver resolver = this.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, null, MediaStore.Audio.Media.TITLE);
        if (cursor == null)
            return;
        if (cursor.moveToLast()) {
            while (cursor.moveToPrevious()) {
                Long music_id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));//歌曲ID
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//path
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));//歌名
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));//歌手
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));//专辑
                long album_id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));//专辑ID
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));//总时长（单位：毫秒）
                int date_time = Integer.parseInt(getDateFromSeconds(cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))));//添加日期(格式：20170801)
                int add_time = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)));//单位：毫秒

                if (artist == "<unknown>" || artist.equals("<unknown>")) {
                    artist = "";
                }
                if (album == "<unknown>" || album.equals("<unknown>")) {
                    album = "";
                }
                if (url.toLowerCase().indexOf("record") == -1 && url.toLowerCase().indexOf("phonerecord") == -1) {
                    if (new File(url).exists()) {
                        all_list.add(new MusicInfo(music_id, url, title, artist, album, album_id, duration, getMusic_pinyin(title), date_time, add_time));//英文开头，拼音就是英文
                    }
                }
            }
        }
        cursor.close();
    }

    //获取对应的拼音
    public String getMusic_pinyin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        String output = "";
        if (inputString != null && inputString.length() > 0 && !"null".equals(inputString)) {
            char[] input = inputString.trim().toCharArray();
            try {
                for (int i = 0; i < input.length; i++) {

                    if (java.lang.Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                        output += temp[0];
                    } else
                        output += java.lang.Character.toString(input[i]);
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                e.printStackTrace();
            }
        } else {
            return "*";
        }
        return output;
    }

    // 自定义的排序
    public void listSortId(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                int i1 = (int) o1.getMusic_id();
                int i2 = (int) o2.getMusic_id();
                if (i1 < i2) {
                    return 1;
                }
                if (i1 > i2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }

    // 自定义的排序
    public static void listSortPinYin(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getMusic_pinyin();
                String name2 = o2.getMusic_pinyin();
                Collator instance = Collator.getInstance(Locale.ENGLISH);
                if (name1.trim().equals(name2.trim())) {
                    return 0;
                }
                return instance.compare(name1, name2);
            }
        });
    }

    public String getDateFromSeconds(String seconds) {
        if (seconds == null)
            return " ";
        else {
            Date date = new Date();
            try {
                date.setTime(Long.parseLong(seconds) * 1000);
            } catch (NumberFormatException nfe) {

            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            return sdf.format(date);
        }
    }

    public ArrayList<HashMap<String, Object>> listToFolder(ArrayList<MusicInfo> list_temp) {
        listSortFolder(list_temp);
        ArrayList<HashMap<String, Object>> list_folder_all_temp = new ArrayList<>();
        ArrayList<HashMap<String, Object>> list_folder = new ArrayList<>();
        for (int i = 0; i < list_temp.size(); i++) {
            String temp = list_temp.get(i).getMusic_path();
            String temp002 = "";

            temp = getStringFolder(temp);
            if (i == 0) {
                HashMap<String, Object> folder_map = new HashMap<>();
                folder_map.put("folder_name", temp);
                folder_map.put("start_position", 0);
                list_folder.add(folder_map);
            } else {
                temp002 = getStringFolder(list_temp.get(i - 1).getMusic_path());
                if (!temp.equals(temp002)) {
                    HashMap<String, Object> folder_map = new HashMap<>();
                    folder_map.put("folder_name", temp);
                    folder_map.put("start_position", i);
                    list_folder.add(folder_map);
                }
            }
        }

        for (int i = 0; i < list_folder.size(); i++) {
            int start_position = (int) list_folder.get(i).get("start_position");
            int end_position = -1;
            if (i == list_folder.size() - 1) {
                end_position = list_temp.size();
            } else {
                end_position = (int) list_folder.get(i + 1).get("start_position");
            }
            HashMap<String, Object> map_temp = new HashMap<>();
            ArrayList<MusicInfo> folder_list_temp = new ArrayList<>();
            for (int j = start_position; j < end_position; j++) {
                folder_list_temp.add((MusicInfo) list_temp.get(j));
            }
            map_temp.put("folder_name", getInfoString(list_folder.get(i).get("folder_name").toString()));
            listSortPinYin(folder_list_temp);
            map_temp.put("folder_name_list", folder_list_temp);
            map_temp.put("folder_name_path", list_folder.get(i).get("folder_name").toString());
            list_folder_all_temp.add(map_temp);
        }

        return list_folder_all_temp;
    }


    public String getInfoString(String string) {
        if (string.equals(sd_path)) {
            return "根目录";
        } else if (string.toLowerCase().indexOf("12530") > -1) {
            return "咪咕音乐";
        } else if (string.toLowerCase().indexOf("music") > -1 && string.toLowerCase().indexOf("baidu") > -1) {
            return "百度音乐";
        } else if (string.toLowerCase().indexOf("kgmusic") > -1) {
            return "酷狗音乐";
        } else if (string.toLowerCase().indexOf("kuwomusic") > -1) {
            return "酷我音乐";
        } else if (string.toLowerCase().indexOf("cloudmusic") > -1) {
            return "网易云音乐";
        } else if (string.toLowerCase().indexOf("qqmusic") > -1) {
            return "QQ音乐";
        } else if (string.toLowerCase().indexOf("xiami") > -1) {
            return "虾米音乐";
        } else {
            return string.substring(string.lastIndexOf("/") + "/".length());
        }

    }

    public void listSortFolder(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getMusic_path() + "";
                String name2 = o2.getMusic_path() + "";
                Collator instance = Collator.getInstance(Locale.CHINA);
                if (name1.trim().equals(name2.trim())) {
                    return 0;
                }
                return instance.compare(name1, name2);

            }
        });
    }

    public String getStringFolder(String str) {
        String string = str;
        if (str.indexOf("/") > -1) {
            string = str.substring(0, str.lastIndexOf("/"));
        }
        return string;
    }
}
