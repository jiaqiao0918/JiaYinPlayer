package com.android.jiaqiao.ViewPagerFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.jiaqiao.Service.MusicPlayService;
import com.android.jiaqiao.View.LrcView;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.File;

/**
 * Created by jiaqiao on 2017/8/25/0025.
 */


public class ViewPagerFragmentMusicPlayLrc extends Fragment {


    private LrcView music_play_lrc;

    private MusicPlayLrcReceiver mReceiver;
    private IntentFilter mFilter;

    private int play_time=0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_fragment_music_play_lrc, container, false);
        music_play_lrc = (LrcView) view.findViewById(R.id.music_play_lrc);

        String lrc_path01 = PublicDate.music_play_now.getMusic_path().replace(".mp3", ".lrc").replace(".MP3", ".lrc");
        String lrc_path02 = PublicDate.music_play_now.getMusic_path().replace(".mp3", ".LRC").replace(".MP3", ".LRC");
        if (new File(lrc_path01).exists()) {
            music_play_lrc.setLrcPath(lrc_path01);
            music_play_lrc.changeCurrent(0);
        } else if (new File(lrc_path02).exists()) {
            music_play_lrc.setLrcPath(lrc_path02);
            music_play_lrc.changeCurrent(0);
        }

        //动态注册广播
        mReceiver = new MusicPlayLrcReceiver();
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

    class MusicPlayLrcReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MusicPlayService.GET_MUSIC_PLAY_TIME:
                    int time = intent.getIntExtra("play_time", 0);
                    if (time > 0) {
                        play_time = time;
                        if (play_time > PublicDate.music_play_now.getMusic_duration()) {
                            play_time = PublicDate.music_play_now.getMusic_duration();
                        }
                        if(music_play_lrc!=null){
                            music_play_lrc.changeCurrent(play_time);
                        }
                    }
                    break;
            }
        }
    }
}
