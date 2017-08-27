package com.android.jiaqiao.jiayinplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Adapter.ViewPagerFragmentAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.MusicPlayService;
import com.android.jiaqiao.Utils.FastBlurUtil;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayAlbumImage;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayLrc;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayShowList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static com.android.jiaqiao.Utils.MusicPlayUtil.addTextToFile;
import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/8/23/0023.
 */

public class MusicPlayActivity extends AppCompatActivity {

    public static final int UPDATE_MUSIC_PLAY_ACTIVITY = 400000000;
    public static final int UPDATE_MUSIC_PLAY_ACTIVITY_OTHER = 400000001;


    private ViewPager music_play_view_pager;
    private ViewPagerFragmentAdapter view_pager_fragment_adapter;
    private View view_oval_001, view_oval_002, view_oval_003;
    private TextView music_tittle_view, music_artist_view, play_now_time, play_max_time;
    private ImageView back_activity, music_album_image_big, music_play_mode, play_last, play_next, music_play_love, music_play_is_not_play;
    private SeekBar music_play_seek_bar;

    private ArrayList<Fragment> view_pager_fragment_list = new ArrayList<Fragment>();
    private MusicInfo music_play_now;
    private boolean is_love_music = false;
    private int play_time = 0;
    private boolean is_update_start_stop = false;
    private boolean is_playing = false;
    private int seek_bar_touch_time = 0;


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
        music_play_mode = (ImageView) findViewById(R.id.music_play_mode);
        back_activity = (ImageView) findViewById(R.id.back_activity);
        music_album_image_big = (ImageView) findViewById(R.id.music_album_image_big);
        play_last = (ImageView) findViewById(R.id.play_last);
        play_next = (ImageView) findViewById(R.id.play_next);
        music_play_love = (ImageView) findViewById(R.id.music_play_love);
        music_play_is_not_play = (ImageView) findViewById(R.id.music_play_is_not_play);
        music_play_seek_bar = (SeekBar) findViewById(R.id.music_play_seek_bar);


        is_love_music = selectIsLoveMusic();
        updateLoveUi();
        play_max_time.setText(getMusicTime(music_play_now.getMusic_duration()));
        music_tittle_view.setText(music_play_now.getMusic_title());
        music_artist_view.setText(music_play_now.getMusic_artist());
        music_play_album_image_bitmap = MusicUtils.getArtwork(this, music_play_now.getMusic_id(), music_play_now.getMusic_album_id(), true);
        if (music_play_album_image_bitmap != null) {
            setImageViewImage(music_album_image_big, music_play_album_image_bitmap);
        }
        play_time = 0;
        seek_bar_touch_time = 0;
        is_playing = false;
        music_play_seek_bar.setMax(PublicDate.music_play_now.getMusic_duration());
        music_play_seek_bar.setProgress(0);
        play_now_time.setText(getMusicTime(play_time));
        updateStartStopUi();
        updatePlayMode();
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayShowList());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayAlbumImage());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayLrc());

        view_pager_fragment_adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), view_pager_fragment_list);
        music_play_view_pager.setAdapter(view_pager_fragment_adapter);
        music_play_view_pager.setCurrentItem(1); //设置当前页是第1页
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

        music_play_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_ORDER) {
                    PublicDate.play_mode = MusicPlayService.PLAY_MODE_RANDOM;
                } else if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_RANDOM) {
                    PublicDate.play_mode = MusicPlayService.PLAY_MODE_SINGLE;
                } else if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_SINGLE) {
                    PublicDate.play_mode = MusicPlayService.PLAY_MODE_ORDER;
                }
                //发送广播
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.UPDATE_PLAY_MODE);
                sendBroadcast(temp_intent);

                getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("play_mode", PublicDate.play_mode).commit();
                updatePlayMode();
            }
        });

        back_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        music_play_is_not_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送广播
                if (is_playing) {
                    is_playing = false;
                } else {
                    is_playing = true;
                }
                updateStartStopUi();
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.START_STOP_MUSIC);
                sendBroadcast(temp_intent);
            }
        });
        play_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = 0;
                if (PublicDate.play_mode != MusicPlayService.PLAY_MODE_RANDOM) {
                    temp = (PublicDate.music_play_list_position - 1 + PublicDate.music_play.size()) % PublicDate.music_play.size();
                } else {
                    temp = PublicDate.play_randoms.get(1);
                }
                PublicDate.music_play.get(PublicDate.music_play_list_position).setIs_playing(false);
                PublicDate.music_play.get(temp).setIs_playing(true);
                PublicDate.music_play_list_position = temp;
                PublicDate.music_play_now = PublicDate.music_play.get(temp);
                getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();

                updateActivity();
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.PLAY_LAST_MUSIC);
                sendBroadcast(temp_intent);
            }
        });
        play_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = 0;
                if (PublicDate.play_mode != MusicPlayService.PLAY_MODE_RANDOM) {
                    temp = (PublicDate.music_play_list_position + 1 + PublicDate.music_play.size()) % PublicDate.music_play.size();
                } else {
                    temp = PublicDate.play_randoms.get(3);
                }
                PublicDate.music_play.get(PublicDate.music_play_list_position).setIs_playing(false);
                PublicDate.music_play.get(temp).setIs_playing(true);
                PublicDate.music_play_list_position = temp;
                PublicDate.music_play_now = PublicDate.music_play.get(temp);
                getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();

                updateActivity();
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.PLAY_NEXT_MUSIC);
                sendBroadcast(temp_intent);

            }
        });
        music_play_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = getPath("love");
                if (is_love_music) {
                    String music_id = music_play_now.getMusic_id() + "";
                    deleteSelectStr(path, music_id);
                    is_love_music = false;
                } else {
                    int num = 0;
                    String add_context = music_play_now.getMusic_id() + separate_str + music_play_now.getMusic_title();
                    if (!isHavaContextFromPath(path, add_context)) {
                        addTextToFile(path, add_context);
                        num++;
                    }
                    if (num > 0) {
                        Toast.makeText(MusicPlayActivity.this, num + "首歌曲收藏成功！！", Toast.LENGTH_SHORT).show();
                    }
                    is_love_music = true;
                }
                //发送广播
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MainActivity.ALL_MUSIC_UPDATE);
                temp_intent.putExtra("is_update", true);
                sendBroadcast(temp_intent);
                updateLoveUi();
            }
        });
        music_play_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seek_bar_touch_time = seekBar.getProgress();
                play_now_time.setText(getMusicTime(seek_bar_touch_time));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.SEEK_TO);
                temp_intent.putExtra("seek_to_time", seek_bar_touch_time);
                sendBroadcast(temp_intent);
                seek_bar_touch_time = 0;
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

    public void deleteSelectStr(String path, String select_str) {
        /*
         *
		 * 删除一行思路：将整个txt的内容保存到list中，再清空txt的内容，然后将list中的数据保存到txt中，保存时进行筛选过滤需要删除的某行
		 * */
        ArrayList<String> temp_list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.indexOf(PublicDate.separate_str) > -1) {
                    temp_list.add(line);
                }
            }
            br.close();
            fr.close();

            clearTxtAll(path);// 清空txt内容

            for (int i = 0; i < temp_list.size(); i++) {
                String temp_str = temp_list.get(i).toString();
                if (temp_str.indexOf(select_str + PublicDate.separate_str) <= -1) {
                    addTextToFile(path, temp_str);
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clearTxtAll(String path) {
        // 清空txt文件
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件,false表示覆盖的方式写入
            writer = new FileWriter(path, false);
            writer.write("");
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_enter_anim, R.anim.dialog_exit_anim);
        //设置Activity的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }

    public boolean isHavaContextFromPath(String path_name, String context) {
        try {
            FileReader fr = new FileReader(path_name);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (context.trim().equals(line.trim())) {
                    return true;
                }
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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
        Bitmap image_view_bitmap =null;
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
        is_love_music = selectIsLoveMusic();
        play_time = 0;
        seek_bar_touch_time = 0;
        is_playing = false;
        music_play_seek_bar.setMax(PublicDate.music_play_now.getMusic_duration());
        music_play_seek_bar.setProgress(0);
        play_now_time.setText(getMusicTime(play_time));
        updateLoveUi();
        updatePlayMode();
        play_max_time.setText(getMusicTime(music_play_now.getMusic_duration()));
        music_tittle_view.setText(music_play_now.getMusic_title());
        music_artist_view.setText(music_play_now.getMusic_artist());
        music_play_album_image_bitmap = MusicUtils.getArtwork(this, music_play_now.getMusic_id(), music_play_now.getMusic_album_id(), true);
        if (music_play_album_image_bitmap != null) {
            setImageViewImage(music_album_image_big, music_play_album_image_bitmap);
        } else {
            music_album_image_big.setImageResource(R.color.touming);
        }
        play_time = 0;
        view_pager_fragment_list.clear();
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

    public void updateActivity() {
        music_play_now = PublicDate.music_play_now;
        is_love_music = selectIsLoveMusic();
        play_time = 0;
        seek_bar_touch_time = 0;
        is_playing = false;
        music_play_seek_bar.setMax(PublicDate.music_play_now.getMusic_duration());
        music_play_seek_bar.setProgress(0);
        play_now_time.setText(getMusicTime(play_time));

        is_love_music = selectIsLoveMusic();
        updateLoveUi();
        updatePlayMode();
        play_max_time.setText(getMusicTime(music_play_now.getMusic_duration()));
        music_tittle_view.setText(music_play_now.getMusic_title());
        music_artist_view.setText(music_play_now.getMusic_artist());

        music_play_album_image_bitmap = MusicUtils.getArtwork(this, music_play_now.getMusic_id(), music_play_now.getMusic_album_id(), true);
        if (music_play_album_image_bitmap != null) {
            setImageViewImage(music_album_image_big, music_play_album_image_bitmap);
        } else {
            music_album_image_big.setImageResource(R.color.touming);
        }
        play_time = 0;
        view_pager_fragment_list.clear();
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayShowList());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayAlbumImage());
        view_pager_fragment_list.add(new ViewPagerFragmentMusicPlayLrc());
        view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
    }

    public String getMusicTime(int music_duration) {
        int time = (music_duration / 1000);//单位：秒
        String string = String.format("%02d", (time / 60)) + ":" + String.format("%02d", (time % 60));
        return string;
    }

    public String getPath(String sheet_id) {
        File file = new File(getFilesDir() + "/music_sheet_list");
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

    public boolean selectIsLoveMusic() {
        try {
            FileReader fr = new FileReader(getPath("love"));
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.indexOf(music_play_now.getMusic_id() + "") > -1 && line.indexOf(music_play_now.getMusic_title()) > -1) {
                    return true;
                }
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public void updateLoveUi() {
        if (is_love_music) {
            music_play_love.setImageResource(R.drawable.music_play_is_love);
        } else {
            music_play_love.setImageResource(R.drawable.music_play_not_love);
        }
    }

    public void updateStartStopUi() {
        if (is_playing) {
            music_play_is_not_play.setImageResource(R.drawable.music_play_is);
        } else {
            music_play_is_not_play.setImageResource(R.drawable.music_play_not);
            is_update_start_stop = false;
        }
    }

    public void updatePlayMode() {
        switch (PublicDate.play_mode) {
            case MusicPlayService.PLAY_MODE_ORDER:
                music_play_mode.setImageResource(R.drawable.music_play_mode_order);
                break;
            case MusicPlayService.PLAY_MODE_RANDOM:
                music_play_mode.setImageResource(R.drawable.music_play_mode_ramdom);
                break;
            case MusicPlayService.PLAY_MODE_SINGLE:
                music_play_mode.setImageResource(R.drawable.music_play_mode_single);
                break;
        }
    }

    class MusicPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY:
                    updateAllActivity();
                    break;
                case MainActivity.ALL_MUSIC_UPDATE:
                    updateActivity();
                    break;
                case MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY_OTHER:
                    updateActivity();
                    break;
                case MusicPlayService.GET_MUSIC_PLAY_TIME:
                    int time = intent.getIntExtra("play_time", 0);
                    if (time > 0) {
                        play_time = time;
                        if (play_time > music_play_seek_bar.getMax()) {
                            play_time = music_play_seek_bar.getMax();
                        }
                        music_play_seek_bar.setProgress(play_time);
                        play_now_time.setText(getMusicTime(play_time));
                        if (!is_update_start_stop) {
                            updateStartStopUi();
                            is_update_start_stop = true;
                        }
                    }
                    break;
                case MusicPlayService.IS_PLAY:
                    is_playing = intent.getBooleanExtra("is_playing", false);
                    updateStartStopUi();
                    break;
            }
        }

    }

}
