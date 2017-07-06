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
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;


/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class FragmentFolderForMusicItem extends Fragment {
    private String folder_name_str = "";
    private ArrayList<MusicInfo> folder_list = new ArrayList<MusicInfo>();

    private int last_click_position = 0;

    private RecyclerViewAdapter adapter;
    private RecyclerView show_folder_list;

    public void setValues(String folder_name_str, ArrayList<MusicInfo> folder_list) {
        this.folder_name_str = folder_name_str;
        this.folder_list = folder_list;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_for_music_item_layout, null);
        TextView folder_name = (TextView) view.findViewById(R.id.folder_name);
        ImageButton back_last_fragment = (ImageButton) view.findViewById(R.id.back_last_fragment);
        show_folder_list = (RecyclerView) view.findViewById(R.id.show_folder_list);
        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        if (folder_name_str.length() > 0) {
            folder_name.setText(folder_name_str);
        }
        if (folder_list != null && folder_list.size() > 0) {
            show_folder_list.setNestedScrollingEnabled(false);
            // 创建默认的线性LayoutManager
            show_folder_list.setLayoutManager(new LinearLayoutManager(getActivity()));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            show_folder_list.setHasFixedSize(true);
            // 创建并设置Adapter
            adapter = new RecyclerViewAdapter(folder_list);
            adapter.setOnItemClickListener(new RecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件
                    folder_list.get(last_click_position).setIs_playing(false);
                    folder_list.get(position).setIs_playing(true);
                    adapter.notifyItemChanged(last_click_position);//刷新单个数据
                    adapter.notifyItemChanged(position);
                    last_click_position = position;
                }
            });
            adapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    //item长按事件
                    folder_list.remove(position);
//                  adapter.notifyItemRemoved(position);//不会刷新RecycleView的高度
                    adapter.notifyDataSetChanged();
                    show_folder_list.startLayoutAnimation();//重新开始LayoutAnimation

                }
            });
            show_folder_list.setAdapter(adapter);

        }else{
            TextView no_music=(TextView) view.findViewById(R.id.no_music);
            no_music.setVisibility(View.GONE);
        }
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (folder_list != null && folder_list.size() > 0) {
            folder_list.get(last_click_position).setIs_playing(false);
            adapter.notifyItemChanged(last_click_position);//刷新单个数据
        }
    }
}
