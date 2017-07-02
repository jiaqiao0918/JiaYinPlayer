package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.jiaqiao.Adapter.FolderListViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Utils.FragmentTransactionExtended;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class FragmentFolderForMusic extends Fragment {

    private Context mContext;
    private String sd_path = Environment.getExternalStorageDirectory().getPath();

    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    private ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_for_music_layout, null);
        ImageButton back_last_fragment = (ImageButton) view.findViewById(R.id.back_last_fragment);
        ListView show_all_folder_of_list = (ListView) view.findViewById(R.id.show_all_folder_of_list);
        music_all = PublicDate.music_all;
        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        list_folder_all = listToFolder(music_all);
        FolderListViewAdapter adapter = new FolderListViewAdapter(mContext, list_folder_all);
        show_all_folder_of_list.setAdapter(adapter);

        show_all_folder_of_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentFolderForMusicItem fragment_date_for_music_item = new FragmentFolderForMusicItem();
                fragment_date_for_music_item.setContext(mContext);
                fragment_date_for_music_item.setValues(list_folder_all.get(position).get("folder_name").toString(),(ArrayList<MusicInfo>)list_folder_all.get(position).get("folder_name_list"));
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        mContext, fragmentTransaction, new FragmentMain(),
                        fragment_date_for_music_item, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });

        return view;
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
            map_temp.put("folder_name_list", folder_list_temp);
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
