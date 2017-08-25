package com.android.jiaqiao.ViewPagerFragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.View.SquareImageView;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

/**
 * Created by jiaqiao on 2017/8/25/0025.
 */

public class ViewPagerFragmentMusicPlayAlbumImage extends Fragment {

    private SquareImageView music_play_album_image;
    private Bitmap bitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_fragment_music_play_album_image, container, false);
        music_play_album_image = (SquareImageView) view.findViewById(R.id.music_play_album_image);
        bitmap = MusicUtils.getArtwork(getActivity(), PublicDate.music_play_now.getMusic_id(), PublicDate.music_play_now.getMusic_album_id(), true);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image_big);
        }
        setImageViewImage(music_play_album_image, bitmap);
        music_play_album_image.setDrawingCacheEnabled(true);
        PublicDate.music_last_album_image_bitmap = music_play_album_image.getDrawingCache();
        music_play_album_image.setDrawingCacheEnabled(false);
        return view;
    }

    // 给一个ImageView设置图片,并带有渐变
    public void setImageViewImage(ImageView image_view, Bitmap image_bitmap) {
        image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image_view.setDrawingCacheEnabled(true);

        Bitmap image_view_bitmap =null;
        if (PublicDate.music_last_album_image_bitmap != null) {
            image_view_bitmap = PublicDate.music_last_album_image_bitmap;
        }
        Drawable start_drawable = new BitmapDrawable(image_view_bitmap);//渐变前的Drawable
        Drawable end_drawable = new BitmapDrawable(image_bitmap);//渐变后的Drawable，bitmap转drawable
        TransitionDrawable mTransitionDrawable = new TransitionDrawable(new Drawable[]{
                start_drawable,
                end_drawable
        });
        mTransitionDrawable.setCrossFadeEnabled(true);
        mTransitionDrawable.startTransition(800);//渐变过程持续时间
        image_view.setImageDrawable(mTransitionDrawable);
        image_view.setDrawingCacheEnabled(false);
    }
}
