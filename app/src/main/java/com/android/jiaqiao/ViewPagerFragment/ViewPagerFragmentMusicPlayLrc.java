package com.android.jiaqiao.ViewPagerFragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.jiaqiao.View.LrcView;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.File;

/**
 * Created by jiaqiao on 2017/8/25/0025.
 */


public class ViewPagerFragmentMusicPlayLrc extends Fragment {


    private LrcView music_play_lrc;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_fragment_music_play_lrc,container,false);
        music_play_lrc = (LrcView) view.findViewById(R.id.music_play_lrc);

        String lrc_path01 = PublicDate.music_play_now.getMusic_path().replace(".mp3",".lrc").replace(".MP3",".lrc");
        String lrc_path02 = PublicDate.music_play_now.getMusic_path().replace(".mp3",".LRC").replace(".MP3",".LRC");
        if(new File(lrc_path01).exists()){
            music_play_lrc.setLrcPath(lrc_path01);
        }else if(new File(lrc_path02).exists()){
            music_play_lrc.setLrcPath(lrc_path02);
        }

        return view;
    }


}
