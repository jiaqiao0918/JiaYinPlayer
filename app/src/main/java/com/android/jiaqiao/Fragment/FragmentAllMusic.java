package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.jiaqiao.Adapter.RecyclerViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/6/22/0022.
 */

public class FragmentAllMusic extends Fragment {
    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();

    private RecyclerViewAdapter adapter;
    private RecyclerView show_all_music_list;

    private int last_click_position = 0;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_music_layout, null);
        ImageButton back_last_fragment = (ImageButton) view.findViewById(R.id.back_last_fragment);
        TextView show_all_list_size=(TextView) view.findViewById(R.id.show_all_list_size);

        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        music_all = PublicDate.music_all;
        show_all_list_size.setText((music_all.size())+"首歌");
        if (music_all != null && music_all.size() > 0) {
            show_all_music_list = (RecyclerView) view.findViewById(R.id.show_all_music);
            show_all_music_list.setNestedScrollingEnabled(false);
            // 创建默认的线性LayoutManager
            show_all_music_list.setLayoutManager(new LinearLayoutManager(getActivity()));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            show_all_music_list.setHasFixedSize(true);
            // 创建并设置Adapter
            adapter = new RecyclerViewAdapter(music_all);
            adapter.setOnItemClickListener(new RecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件
                    music_all.get(last_click_position).setIs_playing(false);
                    music_all.get(position).setIs_playing(true);
                    adapter.notifyItemChanged(last_click_position);//刷新单个数据
                    adapter.notifyItemChanged(position);
                    last_click_position = position;
                }
            });
            adapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    //item长按事件
                    music_all.remove(position);
//                  adapter.notifyItemRemoved(position);//不会刷新RecycleView的高度
                    adapter.notifyDataSetChanged();
                    show_all_music_list.startLayoutAnimation();//重新开始LayoutAnimation

                }
            });
            show_all_music_list.setAdapter(adapter);





        } else {
            TextView no_music=(TextView) view.findViewById(R.id.no_music);
            no_music.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(music_all!=null&&music_all.size()>0) {
            music_all.get(last_click_position).setIs_playing(false);
            adapter.notifyItemChanged(last_click_position);
        }
    }
}
