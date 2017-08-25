package com.android.jiaqiao.jiayinplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jiaqiao.Adapter.ViewPagerFragmentAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Utils.FastBlurUtil;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayAlbumImage;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayLrc;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayShowList;

import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/8/23/0023.
 */

public class MusicPlayActivity extends AppCompatActivity {

    public static final int UPDATE_MUSIC_PLAY_ACTIVITY = 400000000;


    private ViewPager music_play_view_pager;
    private ViewPagerFragmentAdapter view_pager_fragment_adapter;
    private View view_oval_001, view_oval_002, view_oval_003;
    private TextView music_tittle_view, music_artist_view, play_now_time, play_max_time;
    private ImageView music_album_image_big, play_last, play_next;

    private ArrayList<Fragment> view_pager_fragment_list = new ArrayList<Fragment>();
    private MusicInfo music_play_now;

    private Bitmap music_play_album_image_bitmap;
    private MusicPlayReceiver mReceiver;
    private IntentFilter mFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);
        music_play_now = PublicDate.music_play_now;

        music_play_view_pager = (ViewPager) findViewById(R.id.music_play_view_pager);
        view_oval_001 = (View) findViewById(R.id.view_oval_001);
        view_oval_002 = (View) findViewById(R.id.view_oval_002);
        view_oval_003 = (View) findViewById(R.id.view_oval_003);
        music_tittle_view = (TextView) findViewById(R.id.music_tittle_view);
        music_artist_view = (TextView) findViewById(R.id.music_artist_view);
        play_now_time = (TextView) findViewById(R.id.play_now_time);
        play_max_time = (TextView) findViewById(R.id.play_max_time);
        music_album_image_big = (ImageView) findViewById(R.id.music_album_image_big);
        play_last = (ImageView) findViewById(R.id.play_last);
        play_next = (ImageView) findViewById(R.id.play_next);

        play_max_time.setText(getMusicTime(music_play_now.getMusic_duration()));
        music_tittle_view.setText(music_play_now.getMusic_title());
        music_artist_view.setText(music_play_now.getMusic_artist());
        music_play_album_image_bitmap = MusicUtils.getArtwork(this, music_play_now.getMusic_id(), music_play_now.getMusic_album_id(), true);
        if(music_play_album_image_bitmap==null){
            music_play_album_image_bitmap=BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image);
        }
        setImageViewImage(music_album_image_big, music_play_album_image_bitmap);
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayShowList());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayAlbumImage());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayLrc());

        view_pager_fragment_adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), view_pager_fragment_list);
        music_play_view_pager.setAdapter(view_pager_fragment_adapter);
        music_play_view_pager.setCurrentItem(1); //设置当前页是第0页
        music_play_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 2) {
                    UpdateViewPagerBar();
                }
            }
        });
        UpdateViewPagerBar();

        play_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (PublicDate.music_play_list_position - 1 + PublicDate.music_play.size()) % PublicDate.music_play.size();
                PublicDate.music_play.get(PublicDate.music_play_list_position).setIs_playing(false);
                PublicDate.music_play.get(temp).setIs_playing(true);
                PublicDate.music_play_list_position = temp;
                PublicDate.music_play_now = PublicDate.music_play.get(temp);
                getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position",PublicDate.music_play_list_position).commit();
                updateAllActivity();
            }
        });
        play_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (PublicDate.music_play_list_position + 1 + PublicDate.music_play.size()) % PublicDate.music_play.size();
                PublicDate.music_play.get(PublicDate.music_play_list_position).setIs_playing(false);
                PublicDate.music_play.get(temp).setIs_playing(true);
                PublicDate.music_play_list_position = temp;
                PublicDate.music_play_now = PublicDate.music_play.get(temp);
                getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position",PublicDate.music_play_list_position).commit();
                updateAllActivity();
            }
        });

        //动态注册广播
        mReceiver = new MusicPlayReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        registerReceiver(mReceiver, mFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PublicDate.is_music_play) {
            PublicDate.is_music_play = false;
        }
        if (PublicDate.update_music_play) {
            PublicDate.update_music_play = false;
            if (PublicDate.music_play.size() > 0) {
                updateAllActivity();
            } else {
                PublicDate.music_play_list_position = 0;
                PublicDate.music_play = null;
                PublicDate.music_play_list_str = "";
                PublicDate.music_play_now = null;
                finish();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_enter_anim, R.anim.dialog_exit_anim);
        //设置Activity的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }

    public void UpdateViewPagerBar() {
        switch (music_play_view_pager.getCurrentItem()) {
            case 0:
                view_oval_001.setBackgroundResource(R.drawable.oval_ffffff);
                view_oval_002.setBackgroundResource(R.drawable.oval_9f9f9f);
                view_oval_003.setBackgroundResource(R.drawable.oval_9f9f9f);
                break;
            case 1:
                view_oval_001.setBackgroundResource(R.drawable.oval_9f9f9f);
                view_oval_002.setBackgroundResource(R.drawable.oval_ffffff);
                view_oval_003.setBackgroundResource(R.drawable.oval_9f9f9f);
                break;
            case 2:
                view_oval_001.setBackgroundResource(R.drawable.oval_9f9f9f);
                view_oval_002.setBackgroundResource(R.drawable.oval_9f9f9f);
                view_oval_003.setBackgroundResource(R.drawable.oval_ffffff);
                break;
            default:
                break;
        }
    }

    // 给一个ImageView设置高斯模糊的图片,并带有渐变
    public void setImageViewImage(ImageView image_view, Bitmap image_bitmap) {
        /*
         * 增大scaleRatio缩放比，使用一样更小的bitmap去虚化可以得到更好的模糊效果，而且有利于占用内存的减小；
		 * 增大blurRadius，可以得到更高程度的虚化，不过会导致CPU更加intensive
		 */
        int scaleRatio = PublicDate.scaleRatio;
        int blurRadius = PublicDate.blurRadius;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image_bitmap,
                image_bitmap.getWidth() / scaleRatio, image_bitmap.getHeight()
                        / scaleRatio, false);
        Bitmap blurBitmap = FastBlurUtil.doBlur(scaledBitmap, blurRadius, true);
        image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);

        image_view.setDrawingCacheEnabled(true);
        Bitmap image_view_bitmap = image_view.getDrawingCache();
        Drawable start_drawable = new BitmapDrawable(image_view_bitmap);//渐变前的Drawable
        Drawable end_drawable = new BitmapDrawable(blurBitmap);//渐变后的Drawable，bitmap转drawable
        TransitionDrawable mTransitionDrawable = new TransitionDrawable(new Drawable[]{
                start_drawable,
                end_drawable
        });
        mTransitionDrawable.setCrossFadeEnabled(true);
        mTransitionDrawable.startTransition(800);//渐变过程持续时间
        image_view.setImageDrawable(mTransitionDrawable);
        image_view.setDrawingCacheEnabled(false);
    }

    public void updateAllActivity() {
        music_play_now = PublicDate.music_play_now;
        play_max_time.setText(getMusicTime(music_play_now.getMusic_duration()));
        music_tittle_view.setText(music_play_now.getMusic_title());
        music_artist_view.setText(music_play_now.getMusic_artist());
        music_play_album_image_bitmap = MusicUtils.getArtwork(this, music_play_now.getMusic_id(), music_play_now.getMusic_album_id(), true);
        if (music_play_album_image_bitmap == null) {
            music_play_album_image_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image);
        }
        setImageViewImage(music_album_image_big, music_play_album_image_bitmap);
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayShowList());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayAlbumImage());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayLrc());
        view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
        //发送广播
        Intent temp_intent = new Intent();
        temp_intent.setAction("com.android.jiaqiao");
        temp_intent.putExtra("type", MainActivity.UPDATE_MUSIC_PLAY);
        temp_intent.putExtra("is_update_music_play", true);
        sendBroadcast(temp_intent);
    }

    public String getMusicTime(int music_duration) {
        int time = (music_duration / 1000);//单位：秒
        String string = String.format("%02d", (time / 60)) + ":" + String.format("%02d", (time % 60));
        return string;
    }

    class MusicPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY:
                    updateAllActivity();
                    break;
            }
        }

    }

}
