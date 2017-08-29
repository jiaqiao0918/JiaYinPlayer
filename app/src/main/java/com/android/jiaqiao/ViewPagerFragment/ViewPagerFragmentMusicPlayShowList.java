package com.android.jiaqiao.ViewPagerFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.jiaqiao.Activity.MusicEditItemLongActivity;
import com.android.jiaqiao.Adapter.MusicPlayRecyclerViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.MusicPlayService;
import com.android.jiaqiao.Utils.MusicPlayUtil;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.MusicPlayActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/8/25/0025.
 */

public class ViewPagerFragmentMusicPlayShowList extends Fragment {

    private int last_click_position = 0;
    private int list_scroll_position = 0;

    private MusicPlayRecyclerViewAdapter adapter;
    private RecyclerView music_play_show_list;
    private ArrayList<MusicInfo> music_now_play_list = new ArrayList<MusicInfo>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_fragment_music_play_show, container, false);
        music_play_show_list = (RecyclerView) view.findViewById(R.id.music_play_show_list);
        music_now_play_list = PublicDate.music_play;
        if (music_now_play_list.size() > 0) {
            for (int i=0;i<music_now_play_list.size();i++){
                music_now_play_list.get(i).setIs_playing(false);
            }
            music_now_play_list.get(PublicDate.music_play_list_position).setIs_playing(true);
            // 创建默认的线性LayoutManager
            music_play_show_list.setLayoutManager(new LinearLayoutManager(getActivity()));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            music_play_show_list.setHasFixedSize(true);
            // 创建并设置Adapter
            adapter = new MusicPlayRecyclerViewAdapter(music_now_play_list);
            adapter.setOnItemClickListener(new MusicPlayRecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件
                    last_click_position = position;
                    PublicDate.music_play_now = music_now_play_list.get(position);
                    PublicDate.music_play_list_position = position;
                    getActivity().getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position",PublicDate.music_play_list_position).commit();
                    //发送广播
                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY);
                    temp_intent02.putExtra("is_update_music_play", true);
                    getActivity().sendBroadcast(temp_intent02);
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MusicPlayService.PLAY_MUSIC);
                    getActivity().sendBroadcast(temp_intent);

                }
            });
            adapter.setOnItemLongClickListener(new MusicPlayRecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    //item长按事件
                    ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                    music_edit_select.add(position);
                    PublicDate.public_music_edit_temp = music_now_play_list;
                    PublicDate.public_music_edit_temp_select = music_edit_select;
                    PublicDate.is_music_play = true;//播放列表的标识

                    startActivity(new Intent(getActivity(), MusicEditItemLongActivity.class).putExtra("is_all_music_01", false).putExtra("is_music_play_list01",true));
                }
            });
            last_click_position = MusicPlayUtil.selectMusicPosition(music_now_play_list, PublicDate.music_play_now);
            music_play_show_list.setAdapter(adapter);
            if (last_click_position > -1) {
                music_now_play_list.get(last_click_position).setIs_playing(true);
                adapter.notifyItemChanged(last_click_position);//刷新单个数据
            }
            if (last_click_position > 0) {
                list_scroll_position = last_click_position - 1;
            }
            music_play_show_list.scrollToPosition(list_scroll_position);
        }
        return view;
    }


}
