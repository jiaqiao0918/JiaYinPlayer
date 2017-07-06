package com.android.jiaqiao.jiayinplayer;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.jiaqiao.Fragment.FragmentMain;
import com.android.jiaqiao.JavaBean.MusicInfo;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private int WRITE_EXTERNAL_STORAGE_QUANXAN = 200000000;

    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> en_list = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> cn_list = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> other_list = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> music_play = new ArrayList<MusicInfo>();

    private View drawer_center_view;
    private RelativeLayout drawer_center_layout, drawer_left_layout,
            drawer_right_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer_center_layout = (RelativeLayout) findViewById(R.id.drawer_center_layout);
        PublicDate.files_dir=this.getFilesDir().getAbsolutePath().toString();
        //判断有无权限，权限名：WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //无权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //多次拒绝
            } else {
                //未拒绝，开始申请权限
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_QUANXAN);
            }
        } else {
            activityCreate();
        }
    }

    public void activityCreate() {
//        Log.i("into", Environment.getExternalStorageDirectory().getPath());
        drawerCenterLayout();
        getAllMusic(this);
        music_all = list_to_show(en_list, cn_list, other_list);
        PublicDate.music_all = music_all;
    }


    public void drawerCenterLayout() {
        drawer_center_view = getLayoutInflater().inflate(
                R.layout.center_layout, null);
        FragmentMain fragment_main = new FragmentMain();
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.replace(R.id.fragment_show, fragment_main);
        fragmentTransaction.commit();
        drawer_center_layout.addView(drawer_center_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("into", "软件结束运行!!");
    }

    //从SD卡中读取音频文件，无序
    public void getAllMusic(Context context) {
        int big_time = 30 * 1000;//过滤小于30秒
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };
        String where = "mime_type in ('audio/x-wav','application/ogg','audio/mp4','audio/flac','audio/x-ms-wma','audio/x-monkeys-audio','audio/aac','audio/ac3','audio/mpeg')and duration>" + big_time + " and is_music > 0 ";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, null, MediaStore.Audio.Media.TITLE);
        if (cursor == null)
            return;
        if (cursor.moveToLast()) {
            while (cursor.moveToPrevious()) {
                String music_id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));//歌曲ID
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));//path
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));//歌名
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));//歌手
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));//专辑
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                int date_time = Integer.parseInt(getDateFromSeconds(cursor.getString(cursor
                        .getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))));//添加日期

                if (artist == "<unknown>" || artist.equals("<unknown>")) {
                    artist = "";
                }
                if (album == "<unknown>" || album.equals("<unknown>")) {
                    album = "";
                }

                if (isEnglish(title.toCharArray()[0] + "")) {
                    en_list.add(new MusicInfo(music_id, url, title, artist, album, duration, title, date_time));//英文开头，拼音就是英文
                } else {
                    String text_pinyin = getPinYin(title);
                    char pinyin_char = text_pinyin.toLowerCase().trim().toCharArray()[0];
                    if (isEnglish(pinyin_char + "")) {
                        cn_list.add(new MusicInfo(music_id, url, title, artist, album, duration, text_pinyin, date_time));
                    } else {
                        other_list.add(new MusicInfo(music_id, url, title, artist, album, duration, text_pinyin, date_time));
                    }

                }
            }
        }
        cursor.close();
    }

    //排序，混合排序
    public ArrayList<MusicInfo> list_to_show(ArrayList<MusicInfo> list_list, ArrayList<MusicInfo> cn_list_list, ArrayList<MusicInfo> other_list_list) {

        char[] en_char = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        int[] en_num = new int[en_char.length];
        int[] cn_num = new int[en_char.length];
        int cn_start_num = 0;
        int cn_add_num = 0;
        ///混合排序，先显示以标点符号或者其他语言->英文->拼音首字母与英文对应的中文
        for (int i = 0; i < en_char.length; i++) {
            en_num[i] = -1;
            cn_num[i] = -1;
        }

        listSort(list_list);
        listSort(cn_list_list);
        listSort(other_list_list);
        for (int i = 0; i < list_list.size(); i++) {
            char temp = list_list.get(i).getMusic_title().toString().toLowerCase().trim().toCharArray()[0];
            for (int j = 0; j < en_num.length; j++) {
                if (temp == en_char[j]) {
                    en_num[j] = i;
                }
            }
        }

        for (int i = 0; i < cn_list_list.size(); i++) {
            char temp = cn_list_list.get(i).getMusic_pinyin().toString().toLowerCase().trim().toCharArray()[0];
            for (int j = 0; j < cn_num.length; j++) {
                if (temp == en_char[j]) {
                    cn_num[j] = i;
                }
            }
        }
        if (list_list != null && list_list.size() > 0) {
            for (int i = 0; i < en_char.length; i++) {
                if (cn_num[i] > -1) {
                    if (en_num[i] > -1) {
                        if (en_num[i] == -1) {
                            cn_start_num = 0;
                        } else {
                            cn_start_num = en_num[i];
                        }
                    } else {
                        cn_start_num = -1;
                    }
                    // System.out.println(cn_start_num + " "+cn_add_num+" " +
                    // cn_num[i] + " " );
                    for (int j = cn_add_num; j <= cn_num[i]; j++) {
                        list_list.add(cn_start_num + j + 1, cn_list_list.get(j));
                        // System.out.println(cn_start_num+" "+j);
                    }
                    // System.out.println("--------------");
                    if (cn_num[i] > -1) {
                        cn_add_num = cn_num[i] + 1;
                    }
                }
            }

        } else {
            list_list.addAll(cn_list_list);
        }
        if (other_list != null & other_list.size() > 0) {
            list_list.addAll(0, other_list_list);
        }
        return list_list;
    }


    //判断是不是英文
    public boolean isEnglish(String charaString) {
        return charaString.matches("^[a-zA-Z]*");
    }

    //获取对应的拼音
    public String getPinYin(String inputString) {
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

    //自定义的排序
    public static void listSort(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {

            public int compare(MusicInfo o1, MusicInfo o2) {
//                if(type==0){
                String name1 = o1.getMusic_pinyin();
                String name2 = o2.getMusic_pinyin();
//                }

                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);

            }
        });
    }

    //回值操作
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_QUANXAN) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意授权
                activityCreate();
            } else {
                Toast.makeText(MainActivity.this, "权限被拒绝了！！", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
}
