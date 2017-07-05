package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.android.jiaqiao.Adapter.MusicListViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;


/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class FragmentFolderForMusicItem extends Fragment {
    private Context mContext;
    private String folder_name_str = "";
    private ArrayList<MusicInfo> folder_list = new ArrayList<MusicInfo>();

    private int now_playing_position = 0;
    private int last_click_position = 0;

    private MusicListViewAdapter adapter;
    private ListView show_folder_list;

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

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
        show_folder_list = (ListView) view.findViewById(R.id.show_folder_list);
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
            adapter = new MusicListViewAdapter(mContext, folder_list);
            show_folder_list.setAdapter(adapter);
            show_folder_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    updateListView(position);
                }
            });
        }


        return view;
    }

    public void updateListView(int position) {
        now_playing_position = position;

        folder_list.get(last_click_position).setIs_playing(false);
        folder_list.get(position).setIs_playing(true);
//                    adapter.notifyDataSetChanged();

        adapter.updataView(last_click_position, show_folder_list);
        adapter.updataView(position, show_folder_list);
        last_click_position = position;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (folder_list != null && folder_list.size() > 0) {
            folder_list.get(last_click_position).setIs_playing(false);
            adapter.updataView(last_click_position, show_folder_list);
        }
    }
}
