package jiayinplayer.example.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

import jiayinplayer.example.jiaqiao.Adapter.MusicListViewAdapter;
import jiayinplayer.example.jiaqiao.JavaBean.MusicInfo;
import jiayinplayer.example.jiaqiao.jiayinplayer.PublicDate;
import jiayinplayer.example.jiaqiao.jiayinplayer.R;

/**
 * Created by jiaqiao on 2017/6/22/0022.
 */

public class FragmentAllMusic extends Fragment {
    private Context mContext;
    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();

    private MusicListViewAdapter adapter;
    private ListView show_all_music_list;

    private int now_playing_position = 0;
    private int last_click_position = 0;


    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_music_layout, null);
        ImageButton back_last_fragment = (ImageButton) view.findViewById(R.id.back_last_fragment);


        show_all_music_list = (ListView) view.findViewById(R.id.show_all_music_list);

        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        music_all = PublicDate.music_all;



        if (music_all != null && music_all.size() > 0) {
            adapter = new MusicListViewAdapter(mContext, music_all);
            show_all_music_list.setAdapter(adapter);
            show_all_music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    updateListView(position);
                }
            });
        } else {
            Log.i("into", "无数据！！");
        }
        return view;
    }

    public void updateListView(int position) {
        now_playing_position = position;
        music_all.get(position).setIs_playing(true);
        music_all.get(last_click_position).setIs_playing(false);

//                    adapter.notifyDataSetChanged();
        adapter.updataView(position, show_all_music_list);
        adapter.updataView(last_click_position, show_all_music_list);
        last_click_position = position;
    }
}
