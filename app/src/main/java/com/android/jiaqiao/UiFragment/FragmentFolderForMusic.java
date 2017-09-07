package com.android.jiaqiao.UiFragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.jiaqiao.Adapter.FolderListViewAdapter;
import com.android.jiaqiao.Utils.FragmentTransactionExtended;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class FragmentFolderForMusic extends Fragment {



    private ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();

    private FolderListViewAdapter adapter;
    private ListView show_all_folder_of_list;

    private FragmentFolderForMusicReceiver mReceiver;
    private IntentFilter mFilter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_for_music_layout, null);
        ImageView back_last_fragment = (ImageView) view.findViewById(R.id.back_last_fragment);
        show_all_folder_of_list = (ListView) view.findViewById(R.id.show_all_folder_of_list);
        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        list_folder_all = PublicDate.list_folder_all;
        adapter = new FolderListViewAdapter(getActivity(), list_folder_all);
        show_all_folder_of_list.setAdapter(adapter);

        show_all_folder_of_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentFolderForMusicItem fragment_date_for_music_item = new FragmentFolderForMusicItem();

                fragment_date_for_music_item.setValues(list_folder_all.get(position).get("folder_name").toString(), position,list_folder_all.get(position).get("folder_name_path").toString());
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        getActivity(), fragmentTransaction, new FragmentMain(),
                        fragment_date_for_music_item, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });

        //动态注册广播
        mReceiver = new FragmentFolderForMusicReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        getActivity().registerReceiver(mReceiver, mFilter);


        return view;
    }





    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    class FragmentFolderForMusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    boolean is_update = intent.getBooleanExtra("is_update", false);
                    if (is_update) {
                        list_folder_all = PublicDate.list_folder_all;
                        PublicDate.list_folder_all = list_folder_all;
                        adapter = new FolderListViewAdapter(getActivity(), list_folder_all);
                        show_all_folder_of_list.setAdapter(adapter);
                    }
                    break;
            }
        }

    }
}
