package com.android.jiaqiao.ViewPagerFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jiaqiao.jiayinplayer.MusicPlayActivity;
import com.android.jiaqiao.jiayinplayer.R;

/**
 * Created by jiaqiao on 2017/8/21/0021.
 */

public class ViewPagerFragmentMusicPlayItem extends Fragment {
    private String music_title, music_artis;
    private Bitmap music_image_bitmap;

    private TextView show_music_title, show_music_artis;
    private ImageView show_image;

    public void setValue(String music_title, String music_artis, Bitmap music_image_bitmap) {
        this.music_title = music_title;
        this.music_artis = music_artis;
        this.music_image_bitmap = music_image_bitmap;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_fragment_item, container, false);
        show_music_title = (TextView) view.findViewById(R.id.music_title);
        show_music_artis = (TextView) view.findViewById(R.id.music_artist);
        show_image = (ImageView) view.findViewById(R.id.music_album_image);
        if (show_music_title != null) {
            show_music_title.setText(music_title + "");
        }
        if (show_music_artis != null) {
            show_music_artis.setText(music_artis + "");
        }
        if (show_image != null) {
            show_image.setImageBitmap(music_image_bitmap);
        }
        if (view != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().startActivity(new Intent(getActivity(), MusicPlayActivity.class));
                    getActivity().overridePendingTransition(R.anim.dialog_enter_anim, R.anim.dialog_exit_anim);
                }
            });
        }
        return view;
    }
}
