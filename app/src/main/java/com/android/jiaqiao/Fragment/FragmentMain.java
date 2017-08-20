package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.jiaqiao.Activity.AddMusicSheetNameActivity;
import com.android.jiaqiao.Activity.AllMusciAddToSheetActivity;
import com.android.jiaqiao.Activity.DeleteMusicSheetActivity;
import com.android.jiaqiao.Adapter.MusicSheetAdapter;
import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.Utils.FragmentTransactionExtended;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class FragmentMain extends Fragment {
    private ArrayList<SheetInfo> music_sheet_info_list = new ArrayList<SheetInfo>();

    private MusicSheetAdapter music_sheet_adapter;
    private String path = "";
    private ListView music_all_sheet_list;
    private ScrollView scroll_view;
    private TextView music_all_count;

    private FragmentMainReceiver mReceiver;
    private IntentFilter mFilter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_layout, null);

        //动态注册广播
        mReceiver = new FragmentMainReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao.SelectMusicService");
        getActivity().registerReceiver(mReceiver, mFilter);


        path = PublicDate.files_dir + "/music_sheet/music_sheet_name.txt";
        scroll_view = (ScrollView) view.findViewById(R.id.scroll_view);
        music_all_sheet_list = (ListView) view.findViewById(R.id.show_music_list);
        LinearLayout fragment_to_all_music = (LinearLayout) view.findViewById(R.id.fragment_to_all_music);
        LinearLayout fragment_to_love_music = (LinearLayout) view.findViewById(R.id.fragment_to_love_music);
        LinearLayout fragment_to_date_for_music = (LinearLayout) view.findViewById(R.id.fragment_to_date_for_music);
        LinearLayout fragment_to_folder_for_music = (LinearLayout) view.findViewById(R.id.fragment_to_folder_for_music);
        music_all_count = (TextView) view.findViewById(R.id.music_all_count);
        ImageView add_music_sheet_name = (ImageView) view.findViewById(R.id.add_music_sheet_name);
//        loading_gif = (ImageView) view.findViewById(loading_gif);
//        if (!PublicDate.is_select_music_over) {
//            //使用第三方Glide库，加载GIF图片，后期还可以加载专辑图片，google推荐（R.drawable.loading 资源文件，loading_gif ImageView对象）
//            Glide.with(getActivity()).load(R.drawable.loading).into(loading_gif);
//
//        }
        music_all_count.setText((PublicDate.music_all.size()) + "首");
        music_sheet_info_list = getMusicSheetToArrayList(path);

        music_sheet_adapter = new MusicSheetAdapter(getActivity(), music_sheet_info_list);
        music_all_sheet_list.setAdapter(music_sheet_adapter);

        setListViewHeightBasedOnChildren(music_all_sheet_list);
        //设置scrollview初始化后滑动到顶部，必须在ListView填充数据之后，否则无法实现预期效果
//        scroll_view.smoothScrollTo(0,0);//滑动到顶部，两种方法都可行
        scroll_view.fullScroll(ScrollView.FOCUS_UP);//滑动到顶部

        music_all_sheet_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentMusicSheet fragment_music_sheet = new FragmentMusicSheet();
                fragment_music_sheet.setShow_sheet_info(music_sheet_info_list.get(position));
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        getActivity(), fragmentTransaction, new FragmentMain(),
                        fragment_music_sheet, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });
        music_all_sheet_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getActivity(), DeleteMusicSheetActivity.class).putExtra("sheet_id", music_sheet_info_list.get(position).getSheet_id().toString()));
                return true;
            }
        });

        fragment_to_all_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentAllMusic fragment_all_music = new FragmentAllMusic();
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        getActivity(), fragmentTransaction, new FragmentMain(),
                        fragment_all_music, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });
        fragment_to_love_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentLoveMusic fragment_love_music = new FragmentLoveMusic();
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        getActivity(), fragmentTransaction, new FragmentMain(),
                        fragment_love_music, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });
        fragment_to_date_for_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDateForMusic fragment_date_for_music = new FragmentDateForMusic();
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        getActivity(), fragmentTransaction, new FragmentMain(),
                        fragment_date_for_music, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });
        fragment_to_folder_for_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentFolderForMusic fragment_folder_for_music = new FragmentFolderForMusic();
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        getActivity(), fragmentTransaction, new FragmentMain(),
                        fragment_folder_for_music, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });
        add_music_sheet_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddMusicSheetNameActivity.class));
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (PublicDate.add_sheet_over || PublicDate.delete_sheet_over) {
            music_sheet_info_list = getMusicSheetToArrayList(path);
            music_all_sheet_list.setAdapter(new MusicSheetAdapter(getActivity(), music_sheet_info_list));
            setListViewHeightBasedOnChildren(music_all_sheet_list);
            //设置scrollview初始化后滑动到顶部，必须在ListView填充数据之后，否则无法实现预期效果
        }
        if (PublicDate.add_sheet_over) {
            //设置scrollview初始化后滑动到顶部，必须在ListView填充数据之后，否则无法实现预期效果
            //scroll_view.smoothScrollTo(0,0);//滑动到顶部
            scroll_view.fullScroll(ScrollView.FOCUS_DOWN);//滑动到底部
            PublicDate.add_sheet_over = false;
            startActivity(new Intent(getActivity(), AllMusciAddToSheetActivity.class));
        } else if (PublicDate.delete_sheet_over) {
            PublicDate.delete_sheet_over = false;
            scroll_view.smoothScrollTo(0, 0);
        }
    }

    public ArrayList<SheetInfo> getMusicSheetToArrayList(String path_name) {
        ArrayList<SheetInfo> list_temp = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path_name);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (temp.length() > 0 && temp.indexOf(PublicDate.separate_str) > -1) {
                    String[] name = temp.split(PublicDate.separate_str);
                    if (name.length >= 2) {
                        list_temp.add(new SheetInfo(name[0], name[1]));
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
        return list_temp;
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
        getActivity().unregisterReceiver(mReceiver);
    }

    class FragmentMainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    boolean is_update = intent.getBooleanExtra("is_update", false);
                    if (is_update) {
                        music_all_count.setText((PublicDate.music_all.size()) + "首");
                    }
                    break;
            }
        }

    }
}
