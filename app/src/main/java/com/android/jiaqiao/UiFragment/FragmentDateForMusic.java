package com.android.jiaqiao.UiFragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.android.jiaqiao.Adapter.MyExpandableListAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.MusicPlayService;
import com.android.jiaqiao.Utils.MusicPlayUtil;
import com.android.jiaqiao.jiayinplayer.MainActivity;
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
    private ExpandableListView show_date_for_music;

    private int last_parent_playing_position = 0;
    private int last_child_playing_position = 0;

    private FragmentDateForMusicReceiver mReceiver;
    private IntentFilter mFilter;


    private ArrayList<ArrayList<MusicInfo>> list_child_date_time = new ArrayList<>();
    private ArrayList<String> list_parent_date_time = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_for_music_layout, null);
        show_date_for_music = (ExpandableListView) view.findViewById(R.id.show_date_for_music);
        ImageView back_last_fragment = (ImageView) view.findViewById(R.id.back_last_fragment);
        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        update_date();
        //动态注册广播
        mReceiver = new FragmentDateForMusicReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        getActivity().registerReceiver(mReceiver, mFilter);


        return view;
    }

    public void update_date() {
        list_child_date_time = listDateTimeToList(PublicDate.public_music_all);
        for (int i = 0; i < list_child_date_time.size(); i++) {
            ArrayList<MusicInfo> music_date_time_temp = list_child_date_time.get(i);
            int num = MusicPlayUtil.selectMusicPosition(music_date_time_temp, PublicDate.music_play_now);
            if (num > -1) {
                list_child_date_time.get(i).get(num).setIs_playing(true);
                last_parent_playing_position = i;
                last_child_playing_position = num;
                break;
            }
        }
        adapter = new MyExpandableListAdapter(list_parent_date_time, list_child_date_time, getActivity());
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
                PublicDate.music_play_now = list_child_date_time.get(groupPosition).get(childPosition);
                if (PublicDate.music_play_list_str == null || PublicDate.music_play_list_str.equals("")) {
                    PublicDate.music_play_list_str = list_child_date_time.get(groupPosition).toString();
                    PublicDate.music_play = list_child_date_time.get(groupPosition);
                    MusicPlayUtil.saveMusicPlayList();
                    getActivity().getSharedPreferences(MainActivity.SHARED, 0).edit().putString("music_play_list_str", PublicDate.music_play_list_str).commit();
                } else {
                    if (!PublicDate.music_play_list_str.equals(list_child_date_time.get(groupPosition).toString())) {
                        PublicDate.music_play_list_str = list_child_date_time.get(groupPosition).toString();
                        PublicDate.music_play = list_child_date_time.get(groupPosition);
                        MusicPlayUtil.saveMusicPlayList();
                        getActivity().getSharedPreferences(MainActivity.SHARED, 0).edit().putString("music_play_list_str", PublicDate.music_play_list_str).commit();
                    }
                }
                PublicDate.music_play_list_position = childPosition;
                getActivity().getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                //发送广播
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MainActivity.UPDATE_MUSIC_PLAY);
                temp_intent.putExtra("is_update_music_play", true);
                getActivity().sendBroadcast(temp_intent);
                Intent temp_intent002 = new Intent();
                temp_intent002.setAction("com.android.jiaqiao");
                temp_intent002.putExtra("type", MusicPlayService.PLAY_MUSIC);
                getActivity().sendBroadcast(temp_intent002);
                updateChildView(show_date_for_music, groupPosition, childPosition);
                return true;
            }
        });

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

    public void updateChildView(ExpandableListView show_date_for_music, int groupPosition, int childPosition) {
        list_child_date_time.get(last_parent_playing_position).get(last_child_playing_position).setIs_playing(false);
        list_child_date_time.get(groupPosition).get(childPosition).setIs_playing(true);
        adapter.updataView(show_date_for_music, last_parent_playing_position, last_child_playing_position);
        adapter.updataView(show_date_for_music, groupPosition, childPosition);
        last_parent_playing_position = groupPosition;
        last_child_playing_position = childPosition;
    }

    public void listSortDateTime(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getAdd_date_time() + "";
                String name2 = o2.getAdd_date_time() + "";
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);

            }
        });
    }

    public View getView(int groupPosition, int childPosition) {
        int group_position_sum = 0;
        for (int i = 0; i < groupPosition; i++) {
            group_position_sum += adapter.getChildrenCount(i);
        }
        group_position_sum = group_position_sum + groupPosition + childPosition + 1 - show_date_for_music.getFirstVisiblePosition();
        return show_date_for_music.getChildAt(group_position_sum);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (list_child_date_time != null && list_child_date_time.size() > 0) {
            list_child_date_time.get(last_parent_playing_position).get(last_child_playing_position).setIs_playing(false);
            adapter.updataView(show_date_for_music, last_parent_playing_position, last_child_playing_position);
        }
        getActivity().unregisterReceiver(mReceiver);
    }

    class FragmentDateForMusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    boolean is_update = intent.getBooleanExtra("is_update", false);
                    if (is_update) {
                        update_date();
                    }
                    break;
                case MainActivity.VIEW_PAGER_UPDATE_LIST:
                    for (int i = 0; i < list_child_date_time.size(); i++) {
                        ArrayList<MusicInfo> music_date_time_temp = list_child_date_time.get(i);
                        int num = MusicPlayUtil.selectMusicPosition(music_date_time_temp, PublicDate.music_play_now);
                        if (num > -1) {
                            list_child_date_time.get(i).get(num).setIs_playing(true);
                            updateChildView(show_date_for_music, i, num);
                            break;
                        }
                    }

                    break;
            }
        }

    }
}
