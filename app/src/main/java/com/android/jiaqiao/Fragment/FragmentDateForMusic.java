package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.android.jiaqiao.Adapter.MyExpandableListAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jiaqiao on 2017/6/24/0024.
 */

public class FragmentDateForMusic extends Fragment {

    private MyExpandableListAdapter adapter;

    private int now_parent_playing_position = 0;
    private int now_child_playing_position = 0;
    private int last_parent_playing_position = 0;
    private int last_child_playing_position = 0;
    private View last_click_view = null;


    private Context mContext;
    private ArrayList<ArrayList<MusicInfo>> list_child_date_time = new ArrayList<>();
    private ArrayList<String> list_parent_date_time = new ArrayList<>();

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_for_music_layout, null);
        list_child_date_time = listDateTimeToList(PublicDate.music_all);
        final ExpandableListView show_date_for_music = (ExpandableListView) view.findViewById(R.id.show_date_for_music);
        adapter = new MyExpandableListAdapter(list_parent_date_time, list_child_date_time, mContext);
        show_date_for_music.setAdapter(adapter);
        show_date_for_music.setGroupIndicator(null);//去掉向下的箭头
        for (int i = 0; i < adapter.getGroupCount(); i++)//展开所有项
        {
            show_date_for_music.expandGroup(i);
        }
        show_date_for_music.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //返回true，屏蔽组的单击事件
                return true;
            }
        });
        show_date_for_music.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//v就是ExpandableListView中单击的ChildView

                updataChildView(show_date_for_music,groupPosition,childPosition,v);

                return true;
            }
        });


        return view;
    }

    public ArrayList<ArrayList<MusicInfo>> listDateTimeToList(ArrayList<MusicInfo> list) {
        ArrayList<ArrayList<MusicInfo>> list_date_time_temp = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
        int now_date_time = Integer.parseInt(df.format(new Date()));
        ArrayList<MusicInfo> date_time_futrue = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> date_time_0 = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> date_time_1 = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> date_time_2 = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> date_time_3 = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> date_time_7 = new ArrayList<MusicInfo>();
        ArrayList<MusicInfo> date_time_more = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int date_time_temp = list.get(i).getAdd_date_time();
            int temp = now_date_time - date_time_temp;
            if (temp == 0) {
                date_time_0.add(list.get(i));
            } else if (temp == 1) {
                date_time_1.add(list.get(i));
            } else if (temp == 2) {
                date_time_2.add(list.get(i));
            } else if (temp == 3) {
                date_time_3.add(list.get(i));
            } else if (temp > 3 && temp <= 7) {
                date_time_7.add(list.get(i));
            } else if (temp > 7) {
                date_time_more.add(list.get(i));
            } else if (temp < 0) {
                date_time_futrue.add(list.get(i));
            }
        }
        /*listSortDateTime(date_time_futrue);
        listSortDateTime(date_time_0);
        listSortDateTime(date_time_1);
        listSortDateTime(date_time_2);
        listSortDateTime(date_time_3);
        listSortDateTime(date_time_7);
        listSortDateTime(date_time_more);*/
        addToList(list_parent_date_time, list_date_time_temp, "未来", date_time_futrue);
        addToList(list_parent_date_time, list_date_time_temp, "今天", date_time_0);
        addToList(list_parent_date_time, list_date_time_temp, "昨天", date_time_1);
        addToList(list_parent_date_time, list_date_time_temp, "前天", date_time_2);
        addToList(list_parent_date_time, list_date_time_temp, "3天前", date_time_3);
        addToList(list_parent_date_time, list_date_time_temp, "一周前", date_time_7);
        addToList(list_parent_date_time, list_date_time_temp, "很久以前", date_time_more);
        return list_date_time_temp;
    }

    public void addToList(ArrayList<String> list_parent, ArrayList<ArrayList<MusicInfo>> list_child, String string, ArrayList<MusicInfo> add_list) {
        if (add_list != null && add_list.size() > 0) {
            list_parent.add(string);
            list_child.add(add_list);
        }
    }

    public void updataChildView(ExpandableListView show_date_for_music, int groupPosition, int childPosition, View v) {
        now_parent_playing_position = groupPosition;
        now_child_playing_position = childPosition;


        list_child_date_time.get(groupPosition).get(childPosition).setIs_playing(true);
        list_child_date_time.get(last_parent_playing_position).get(last_child_playing_position).setIs_playing(false);
        adapter.updataView(show_date_for_music, groupPosition, childPosition, v);
        adapter.updataView(show_date_for_music, last_parent_playing_position, last_child_playing_position, last_click_view);
        last_parent_playing_position = groupPosition;
        last_child_playing_position = childPosition;
        last_click_view=v;

    }

    public static void listSortDateTime(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getAdd_date_time()+"";
                String name2 = o2.getAdd_date_time()+"";
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);

            }
        });
    }

}
