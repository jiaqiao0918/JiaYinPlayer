package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.jiaqiao.Adapter.MusicListViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/7/3/0003.
 */

public class FragmentMusicSheet extends Fragment {
    private Context mContext;
    private SheetInfo show_sheet_info = null;

    private TextView show_sheet_name, show_sheet_list_size;

    private String path = "";
    private ArrayList<MusicInfo> music_sheet_list = new ArrayList<>();

    private ListView show_music_sheet_list;
    private MusicListViewAdapter adapter;

    private int now_playing_position = 0;
    private int last_click_position = 0;

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setShow_sheet_info(SheetInfo show_sheet_info) {
        this.show_sheet_info = show_sheet_info;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_sheet, null);
        show_sheet_name = (TextView) view.findViewById(R.id.show_sheet_name);
        show_sheet_list_size = (TextView) view.findViewById(R.id.show_sheet_list_size);
        show_music_sheet_list = (ListView) view.findViewById(R.id.show_music_sheet_list);
        show_sheet_name.setText(show_sheet_info.getSheet_name().toString());
        path = getPath(show_sheet_info.getSheet_id().toString());
        getMusicSheetToArrayList(path);
        adapter = new MusicListViewAdapter(mContext, music_sheet_list);
        show_music_sheet_list.setAdapter(adapter);
        setListViewHeightBasedOnChildren(show_music_sheet_list);
        show_music_sheet_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                updateListView(position);
            }
        });

        return view;
    }

    public void getMusicSheetToArrayList(String path_name) {
        try {
            FileReader fr = new FileReader(path_name);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (temp.length() > 0 && temp.indexOf(separate_str) > -1) {
                    String[] music_temp = temp.split(PublicDate.separate_str);
                    if (music_temp.length >= 2) {
                        MusicInfo music_info_temp = getFromSheetGetMusicInfo(music_temp[0], music_temp[1]);
                        if (music_info_temp != null) {
                            music_sheet_list.add(music_info_temp);
                        }
                    }
                }
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MusicInfo getFromSheetGetMusicInfo(String music_id, String music_title) {
        MusicInfo music_info_temp = null;
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
        String where = "mime_type in ('audio/x-wav','application/ogg','audio/mp4','audio/flac','audio/x-ms-wma','audio/x-monkeys-audio','audio/aac','audio/ac3','audio/mpeg')and duration>" + big_time + " and is_music > 0 and _id in('" + music_id + "') and title in('" + music_title + "')";
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, null, MediaStore.Audio.Media.TITLE);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));//歌曲ID
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
            String text_pinyin = "";
            if (isEnglish(title.toCharArray()[0] + "")) {
                text_pinyin = title;
            } else {
                text_pinyin = getPinYin(title);
            }
            music_info_temp = new MusicInfo(music_id, url, title, artist, album, duration, text_pinyin, date_time);
            return music_info_temp;
        } else {
            return null;
        }
    }

    public String getPath(String sheet_id) {
        File file = new File(getActivity().getFilesDir() + "/music_sheet_list");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file.getPath() + "/" + sheet_id + ".txt");
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file2.getPath().toString();
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

    public void setListViewHeightBasedOnChildren(ListView listView) {
        /*
        * 代码设置ListView的高度，使ListView的高度固定，可以在ScrollView中完全显示，而不是显示ListView中一个Item
        *
        * */
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
//        listView.setAdapter(listAdapter);

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    public void updateListView(int position) {
        now_playing_position = position;

        music_sheet_list.get(last_click_position).setIs_playing(false);
        music_sheet_list.get(position).setIs_playing(true);
//                    adapter.notifyDataSetChanged();

        adapter.updataView(last_click_position, show_music_sheet_list);
        adapter.updataView(position, show_music_sheet_list);
        last_click_position = position;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        music_sheet_list.get(last_click_position).setIs_playing(false);
        adapter.updataView(last_click_position, show_music_sheet_list);
    }
}
