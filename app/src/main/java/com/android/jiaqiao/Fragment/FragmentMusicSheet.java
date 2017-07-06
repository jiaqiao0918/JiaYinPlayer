package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Activity.DeleteMusicSheetActivity;
import com.android.jiaqiao.Adapter.RecyclerViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static com.android.jiaqiao.jiayinplayer.PublicDate.music_all;
import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/7/3/0003.
 */

public class FragmentMusicSheet extends Fragment {
    private SheetInfo show_sheet_info = null;

    private TextView show_sheet_name, show_sheet_list_size;

    private String path = "";
    private ArrayList<MusicInfo> music_sheet_list = new ArrayList<>();

    private RecyclerView show_music_sheet_recycler_view;
    private RecyclerViewAdapter adapter;

    private int last_click_position = 0;

    public void setShow_sheet_info(SheetInfo show_sheet_info) {
        this.show_sheet_info = show_sheet_info;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_music_sheet, null);
        show_sheet_name = (TextView) view.findViewById(R.id.show_all_name);
        show_sheet_list_size = (TextView) view.findViewById(R.id.show_all_list_size);
        show_music_sheet_recycler_view = (RecyclerView) view.findViewById(R.id.show_all_music);

        ImageButton back_last_fragment = (ImageButton) view.findViewById(R.id.back_last_fragment);
        LinearLayout music_sheet_delete = (LinearLayout) view.findViewById(R.id.music_sheet_delete);
        LinearLayout music_sheet_modify = (LinearLayout) view.findViewById(R.id.music_sheet_modify);
        LinearLayout music_sheet_add = (LinearLayout) view.findViewById(R.id.music_sheet_add);

        show_sheet_name.setText(show_sheet_info.getSheet_name().toString());
        path = getPath(show_sheet_info.getSheet_id().toString());
        getMusicSheetToArrayList(path);

        show_sheet_list_size.setText(music_sheet_list.size() + "首歌");
        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        if (music_sheet_list != null && music_sheet_list.size() > 0) {
            show_music_sheet_recycler_view.setNestedScrollingEnabled(false);
            // 创建默认的线性LayoutManager
            show_music_sheet_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            show_music_sheet_recycler_view.setHasFixedSize(true);
            // 创建并设置Adapter
            adapter = new RecyclerViewAdapter(music_sheet_list);
            adapter.setOnItemClickListener(new RecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件
                    music_sheet_list.get(last_click_position).setIs_playing(false);
                    music_sheet_list.get(position).setIs_playing(true);
                    adapter.notifyItemChanged(last_click_position);//刷新单个数据
                    adapter.notifyItemChanged(position);
                    last_click_position = position;
                }
            });
            adapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    //item长按事件
                    music_sheet_list.remove(position);
//                  adapter.notifyItemRemoved(position);//不会刷新RecyclerView的高度
                    adapter.notifyDataSetChanged();
                    show_music_sheet_recycler_view.startLayoutAnimation();//重新开始LayoutAnimation

                }
            });
            show_music_sheet_recycler_view.setAdapter(adapter);


        } else {
            TextView no_music = (TextView) view.findViewById(R.id.no_music);
            no_music.setVisibility(View.VISIBLE);
        }

        music_sheet_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DeleteMusicSheetActivity.class).putExtra("sheet_id", show_sheet_info.getSheet_id().toString()));
            }
        });
        music_sheet_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "编辑按钮", Toast.LENGTH_SHORT).show();
            }
        });
        music_sheet_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "添加按钮", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public void setRecyclerView(final RecyclerView listView) {
        /*
        * 代码设置ListView的高度，使ListView的高度固定，可以在ScrollView中完全显示，而不是显示ListView中一个Item
        *
        * */
        // 获取ListView对应的Adapter
        RecyclerView.Adapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getItemCount(); i < len; i++) {
            totalHeight += listView.getChildAt(i).getHeight();
        }
        Log.i("into", totalHeight + "");

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getHeight() * (listAdapter.getItemCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);

    }


    @Override
    public void onResume() {
        super.onResume();
        if (PublicDate.delete_sheet_over) {
            getFragmentManager().popBackStack();
        }
    }

    public MusicInfo getFromListGetMusicInfo(String music_id, String music_title) {
        ArrayList<MusicInfo> list_temp = music_all;

        for (int i = 0; i < list_temp.size(); i++) {
            if (list_temp.get(i).getMusic_id().equals(music_id) && list_temp.get(i).getMusic_title().equals(music_title)) {
                return list_temp.get(i);
            }
        }


        return null;
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
                        MusicInfo music_info_temp = getFromListGetMusicInfo(music_temp[0], music_temp[1]);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (music_sheet_list != null && music_sheet_list.size() > 0) {
            music_sheet_list.get(last_click_position).setIs_playing(false);
            adapter.notifyItemChanged(last_click_position);//刷新单个数据
        }
    }
}
