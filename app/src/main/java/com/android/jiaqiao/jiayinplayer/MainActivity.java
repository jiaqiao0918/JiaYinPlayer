package com.android.jiaqiao.jiayinplayer;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.jiaqiao.Adapter.ViewPagerFragmentAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.MusicPlayService;
import com.android.jiaqiao.Service.SelectMusicService;
import com.android.jiaqiao.UiFragment.FragmentMain;
import com.android.jiaqiao.Utils.DataInfoCache;
import com.android.jiaqiao.Utils.MusicPlayUtil;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayItem;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final int WRITE_EXTERNAL_STORAGE_QUANXAN = 200000000;
    public static final int ALL_MUSIC_UPDATE = 200000001;
    public static final int START_ACTIVITY_TO_OTHER = 200000002;

    public static final int UPDATE_SHEET = 300000001;
    public static final int UPDATE_MUSIC_PLAY = 300000002;
    public static final int SERVICE_UPDATE_MUSIC_PLAY = 300000003;
    public static final int VIEW_PAGER_UPDATE_LIST = 300000004;

    public static final String SHARED = "setting";

    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> music_play_list_temp = new ArrayList<MusicInfo>();
    private ArrayList<Fragment> view_pager_fragment_list = new ArrayList<Fragment>();

    private View drawer_center_view;
    private RelativeLayout drawer_center_layout, drawer_left_layout,
            drawer_right_layout;
    private LinearLayout show_view_pager_layout;
    private ViewPager view_pager_fragment;
    private ViewPagerFragmentAdapter view_pager_fragment_adapter;
    private ImageView music_is_playing;
    private View view_pager_seek_bar;

    private int now_show_position = 0;
    private int last_position = 0;
    private int state_1_position = -1;
    private boolean is_need_update = false;
    private ArrayList<Integer> randoms = new ArrayList<Integer>();
    private boolean is_random = false;//用于辨别是否是随机播放
    private boolean is_playing = false;
    private double start_sd_size = 0;
    private double restart_sd_size = 0;
    private int play_time = 0;
    private int phone_width = 0;
    private boolean is_need_update_view_pager = false;

    private MainActivityReceiver mReceiver;
    private IntentFilter mFilter;

    private String sd_path = Environment.getExternalStorageDirectory().getPath();


    private Intent select_music_intent;
    private Intent music_play_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer_center_layout = (RelativeLayout) findViewById(R.id.drawer_center_layout);
        PublicDate.files_dir = this.getFilesDir().getAbsolutePath().toString();
        //判断有无权限，权限名：WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //无权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //多次拒绝
            } else {
                //未拒绝，开始申请权限
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_QUANXAN);
            }
        } else {
            activityCreate();
        }
    }

    public void activityCreate() {
        PublicDate.path_files_dir = getFilesDir();//获取软件在date文件夹下的路径
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        phone_width = dm.widthPixels;

        ArrayList<MusicInfo> music_all_temp = DataInfoCache.loadListCache(this, "music_all");
        listSortPinYin(music_all_temp);
        PublicDate.public_music_all = music_all_temp;
        music_all = music_all_temp;
        PublicDate.list_folder_all = DataInfoCache.loadListCache(this, "list_folder_all");
        SharedPreferences userSettings = getSharedPreferences(MainActivity.SHARED, 0);
        PublicDate.music_play_list_str = userSettings.getString("music_play_list_str", "");
        PublicDate.music_play = MusicPlayUtil.getMusicPlayList();
        if (PublicDate.music_play.size() <= 0) {
            if (music_all.size() > 0) {
                PublicDate.music_play = music_all;
                PublicDate.music_play_list_position = 0;
                now_show_position = PublicDate.music_play_list_position;
                PublicDate.music_play_now = PublicDate.music_play.get(now_show_position);
            }
        }
        if (PublicDate.music_play.size() > 0) {
            PublicDate.music_play_list_position = userSettings.getInt("music_play_list_position", 0);
            now_show_position = PublicDate.music_play_list_position;
            PublicDate.music_play_now = PublicDate.music_play.get(now_show_position);
            music_play_list_temp = PublicDate.music_play;
        }
        PublicDate.play_mode = userSettings.getInt("play_mode", MusicPlayService.PLAY_MODE_ORDER);
        if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_RANDOM) {
            is_random = true;
        }

        //启动service
        select_music_intent = new Intent(MainActivity.this, SelectMusicService.class);
        startService(select_music_intent);
        music_play_intent = new Intent(MainActivity.this, MusicPlayService.class);
        startService(music_play_intent);

        start_sd_size = (double) getAvailableSize();

        //动态注册广播
        mReceiver = new MainActivityReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        this.registerReceiver(mReceiver, mFilter);

        drawerCenterLayout();

        //Test
//        startActivity(new Intent(MainActivity.this, MusicPlayActivity.class));
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restart_sd_size = (double) getAvailableSize();
        if (start_sd_size + 1 < restart_sd_size || start_sd_size - 1 > restart_sd_size) {
            startService(select_music_intent);
        }
        start_sd_size = (double) getAvailableSize();
    }

    private long getAvailableSize() {
        String sdcard = Environment.getExternalStorageState();
        String state = Environment.MEDIA_MOUNTED;
        File file = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(file.getPath());
        if (sdcard.equals(state)) {
            long blockSize = statFs.getBlockSize();
            long blockavailable = statFs.getAvailableBlocks();
            long blockavailableTotal = blockSize * blockavailable / 1000 / 1000;
            return blockavailableTotal;
        } else {
            return -1;
        }
    }


    public void drawerCenterLayout() {
        drawer_center_view = getLayoutInflater().inflate(
                R.layout.center_layout, null);
        FragmentMain fragment_main = new FragmentMain();
        FragmentTransaction fragmentTransaction = getFragmentManager()
                .beginTransaction();
        fragmentTransaction.replace(R.id.fragment_show, fragment_main);
        fragmentTransaction.commit();
        drawer_center_layout.addView(drawer_center_view);
        show_view_pager_layout = (LinearLayout) drawer_center_view.findViewById(R.id.show_shape_view);
        view_pager_fragment = (ViewPager) drawer_center_view.findViewById(R.id.view_pager_fragment);
        music_is_playing = (ImageView) drawer_center_view.findViewById(R.id.music_is_playing);
        view_pager_seek_bar = (View) drawer_center_view.findViewById(R.id.view_pager_seek_bar);
        play_time = 0;
        updateViewPagerSeekBar(0);
        music_is_playing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_playing) {
                    is_playing = false;
                } else {
                    is_playing = true;
                }
                updateIsPlayUi();
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.START_STOP_MUSIC);
                sendBroadcast(temp_intent);
            }
        });
        updateIsPlayUi();
        if (music_all.size() > 0) {
            setViewPager();
            is_need_update_view_pager = false;
        } else {
            is_need_update_view_pager = true;
        }
    }

    public void setViewPager() {
        //ViewPager+Fragment
        updateViewPagerFragment();
        updateViewPagerSeekBar(0);
        view_pager_fragment_adapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), view_pager_fragment_list);
        view_pager_fragment.setAdapter(view_pager_fragment_adapter);
        view_pager_fragment.setCurrentItem(2); //设置当前页是第0页
        view_pager_fragment.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            /* state == 1的时辰默示正在滑动，
             * state == 2的时辰默示滑动完毕了，
             * state == 0的时辰默示什么都没做。
             * 当页面开始滑动的时候，三种状态的变化顺序为（1，2，0），演示如下：
             */
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == 2) {
                    is_need_update = true;
                } else if (state == 0) {
                    if (is_need_update) {
                        if (state_1_position != view_pager_fragment.getCurrentItem()) {
                            if (view_pager_fragment.getCurrentItem() > state_1_position) {
                                now_show_position = randoms.get(3);
                                viewPagerRight();
                            } else if (view_pager_fragment.getCurrentItem() < state_1_position) {
                                now_show_position = randoms.get(1);
                                viewPagerLeft();
                            }
                            PublicDate.music_play_list_position = now_show_position;
                            PublicDate.music_play_now = PublicDate.music_play.get(now_show_position);
                            view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                            view_pager_fragment.setCurrentItem(2, false); //设置当前页是第2页，false为不需要过渡动画，默认为true
                            getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                            is_need_update = false;
                            play_time = 0;
                            updateViewPagerSeekBar(play_time);
                            Intent temp_intent02 = new Intent();
                            temp_intent02.setAction("com.android.jiaqiao");
                            temp_intent02.putExtra("type", MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY_OTHER);
                            temp_intent02.putExtra("is_update_music_play", true);
                            sendBroadcast(temp_intent02);

                            Intent temp_intent = new Intent();
                            temp_intent.setAction("com.android.jiaqiao");
                            temp_intent.putExtra("type", MusicPlayService.PLAY_MUSIC);
                            sendBroadcast(temp_intent);

                            Intent temp_intent03 = new Intent();
                            temp_intent03.setAction("com.android.jiaqiao");
                            temp_intent03.putExtra("type", MainActivity.VIEW_PAGER_UPDATE_LIST);
                            sendBroadcast(temp_intent03);
                        }

                    }
                } else if (state == 1) {
                    state_1_position = view_pager_fragment.getCurrentItem();
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //测算控件高度
        ViewTreeObserver vto = show_view_pager_layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                show_view_pager_layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                PublicDate.public_drawer_center_bottom_view_height = show_view_pager_layout.getHeight();
            }
        });
    }

    //回值操作
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_QUANXAN) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意授权
                activityCreate();
            } else {
                Toast.makeText(MainActivity.this, "权限被拒绝了！！", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!PublicDate.is_service_select_music_destroy) {
            stopService(select_music_intent);
        }
        if (!PublicDate.is_music_play_destroy) {
            stopService(music_play_intent);
        }
        unregisterReceiver(mReceiver);
    }

    class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    if (!PublicDate.is_service_select_music_destroy) {
                        stopService(select_music_intent);
                    }
                    if (PublicDate.music_play.size() <= 0) {
                        if (music_all.size() > 0) {
                            PublicDate.music_play = music_all;
                            PublicDate.music_play_list_position = 0;
                            now_show_position = PublicDate.music_play_list_position;
                            PublicDate.music_play_now = PublicDate.music_play.get(now_show_position);
                        }
                    }
                    if (PublicDate.music_play.size() > 0) {
                        view_pager_fragment.setVisibility(View.VISIBLE);
                        if (!is_need_update_view_pager) {
                            now_show_position = PublicDate.music_play_list_position;
                            updateIsPlayUi();
                            updateViewPagerFragment();
                            view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                            view_pager_fragment.setCurrentItem(2, false); //设置当前页是第0页，false为不需要过渡动画，默认为true
                        } else {
                            setViewPager();
                        }
                    } else {
                        show_view_pager_layout.setVisibility(View.INVISIBLE);
                    }
                    break;
                case MainActivity.UPDATE_MUSIC_PLAY:
                    if (intent.getBooleanExtra("is_update_music_play", false)) {
                        music_play_list_temp = PublicDate.music_play;
                        if (music_play_list_temp.size() > 0) {
                            now_show_position = PublicDate.music_play_list_position;
                            updateIsPlayUi();
                            updateViewPagerFragment();
                            view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                            view_pager_fragment.setCurrentItem(2, false); //设置当前页是第0页，false为不需要过渡动画，默认为true
                        } else {
                            show_view_pager_layout.setVisibility(View.GONE);
                        }
                    }
                    break;
                case MainActivity.START_ACTIVITY_TO_OTHER:
                    if (intent.getBooleanExtra("is_to", false)) {
                        if (PublicDate.music_play.size() > 0) {
                            startActivity(new Intent(MainActivity.this, MusicPlayActivity.class));
                            overridePendingTransition(R.anim.dialog_enter_anim, R.anim.dialog_exit_anim);
                        }
                    }
                    break;
                case MainActivity.SERVICE_UPDATE_MUSIC_PLAY:
                    if (intent.getBooleanExtra("service_is_update", false)) {
                        if (intent.getBooleanExtra("update_mode", false)) {
                            now_show_position = randoms.get(3);
                            viewPagerRight();
                        } else {
                            now_show_position = randoms.get(1);
                            viewPagerLeft();
                        }
                        PublicDate.music_play_list_position = now_show_position;
                        PublicDate.music_play_now = PublicDate.music_play.get(now_show_position);
                        view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                        view_pager_fragment.setCurrentItem(2, false); //设置当前页是第2页，false为不需要过渡动画，默认为true
                        getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                        Intent temp_intent02 = new Intent();
                        temp_intent02.setAction("com.android.jiaqiao");
                        temp_intent02.putExtra("type", MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY_OTHER);
                        temp_intent02.putExtra("is_update_music_play", true);
                        sendBroadcast(temp_intent02);
                        play_time = 0;
                        updateViewPagerSeekBar(play_time);
                    }
                    break;
                case MusicPlayService.GET_MUSIC_PLAY_TIME:
                    int time = intent.getIntExtra("play_time", 0);
                    if (time > 0) {
                        play_time = time;
                        if (play_time > PublicDate.music_play_now.getMusic_duration()) {
                            play_time = PublicDate.music_play_now.getMusic_duration();
                        }
                        updateViewPagerSeekBar(play_time);
                    }
                    break;
                case MusicPlayService.IS_PLAY:
                    is_playing = intent.getBooleanExtra("is_playing", false);
                    updateIsPlayUi();
                    break;
                case MusicPlayService.UPDATE_PLAY_MODE:
                    if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_RANDOM) {
                        is_random = true;
                    } else {
                        is_random = false;
                    }
                    updateViewPagerFragment();
                    view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                    view_pager_fragment.setCurrentItem(2, false); //设置当前页是第2页，false为不需要过渡动画，默认为true

                    break;
            }
        }

    }

    // 自定义的排序
    public void listSortTitle(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getMusic_title();
                String name2 = o2.getMusic_title();
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);
            }
        });
    }

    // 自定义的排序
    public static void listSortPinYin(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getMusic_pinyin();
                String name2 = o2.getMusic_pinyin();
                Collator instance = Collator.getInstance(Locale.ENGLISH);
                return instance.compare(name1, name2);
            }
        });
    }

    public void viewPagerRight() {
        view_pager_fragment_list.remove(0);
        randoms.remove(0);
        int num = -1;
        if (!is_random) {
            num = (now_show_position + 2 + music_play_list_temp.size()) % music_play_list_temp.size();
        } else {
            while (true) {
                boolean is_have_num = true;
                num = getRandomForMinMax(0, music_play_list_temp.size() - 1);
                for (int j = 0; j < randoms.size(); j++) {
                    if (randoms.get(j) == num) {
                        is_have_num = true;
                        break;
                    } else {
                        is_have_num = false;
                    }
                }
                if (!is_have_num) {
                    break;
                }
            }
        }
        MusicInfo music_info_temp = music_play_list_temp.get(num);
        ViewPagerFragmentMusicPlayItem fragment_temp = new ViewPagerFragmentMusicPlayItem();
        Bitmap bitmap = MusicUtils.getArtwork(this, music_info_temp.getMusic_id(), music_info_temp.getMusic_album_id(), true);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image);
        }
        fragment_temp.setValue(music_info_temp.getMusic_title(), music_info_temp.getMusic_artist(), bitmap);
        view_pager_fragment_list.add(view_pager_fragment_list.size(), fragment_temp);
        randoms.add(randoms.size(), num);
        PublicDate.play_randoms = randoms;
    }

    public void viewPagerLeft() {

        view_pager_fragment_list.remove(view_pager_fragment_list.size() - 1);
        randoms.remove(randoms.size() - 1);
        int num = -1;
        if (!is_random) {
            num = (now_show_position - 2 + music_play_list_temp.size()) % music_play_list_temp.size();
        } else {
            while (true) {
                boolean is_have_num = true;
                num = getRandomForMinMax(0, music_play_list_temp.size() - 1);
                for (int j = 0; j < randoms.size(); j++) {
                    if (randoms.get(j) == num) {
                        is_have_num = true;
                        break;
                    } else {
                        is_have_num = false;
                    }
                }
                if (!is_have_num) {
                    break;
                }
            }
        }
        MusicInfo music_info_temp = music_play_list_temp.get(num);
        ViewPagerFragmentMusicPlayItem fragment_temp = new ViewPagerFragmentMusicPlayItem();
        Bitmap bitmap = MusicUtils.getArtwork(this, music_info_temp.getMusic_id(), music_info_temp.getMusic_album_id(), true);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image);
        }
        fragment_temp.setValue(music_info_temp.getMusic_title(), music_info_temp.getMusic_artist(), bitmap);
        view_pager_fragment_list.add(0, fragment_temp);
        randoms.add(0, num);
        PublicDate.play_randoms = randoms;
    }

    public ArrayList<Fragment> getFragemntFromList01(ArrayList<MusicInfo> music_list, int position) {
        randoms.clear();
        ArrayList<Fragment> temp = new ArrayList<Fragment>();
        for (int i = 0; i < 5; i++) {
            int now_num = (i - 2 + music_list.size() + position) % music_list.size();
            randoms.add(now_num);
            MusicInfo music_info_temp = music_list.get(now_num);
            ViewPagerFragmentMusicPlayItem fragment_temp = new ViewPagerFragmentMusicPlayItem();
            Bitmap bitmap = MusicUtils.getArtwork(this, music_info_temp.getMusic_id(), music_info_temp.getMusic_album_id(), true);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image);
            }
            fragment_temp.setValue(music_info_temp.getMusic_title(), music_info_temp.getMusic_artist(), bitmap);
            temp.add(fragment_temp);
        }
        PublicDate.play_randoms = randoms;
        return temp;
    }

    public ArrayList<Fragment> getFragemntFromList02(ArrayList<MusicInfo> music_list, int position) {
        randoms.clear();
        ArrayList<Fragment> temp = new ArrayList<Fragment>();
        int[] random_num_temp = new int[5];
        for (int i = 0; i < random_num_temp.length; i++) {
            random_num_temp[i] = -1;
        }
        random_num_temp[2] = position;
        for (int i = 0; i < 5; i++) {
            int now_num = -1;
            if (i != 2) {
                while (true) {
                    boolean is_have_num = true;
                    now_num = getRandomForMinMax(0, music_list.size() - 1);
                    for (int j = 0; j < random_num_temp.length; j++) {
                        if (random_num_temp[i] == now_num) {
                            is_have_num = true;
                            break;
                        } else {
                            is_have_num = false;
                        }
                    }
                    if (!is_have_num) {
                        break;
                    }
                }
            } else {
                now_num = position;
            }
            MusicInfo music_info_temp = music_list.get(now_num);
            ViewPagerFragmentMusicPlayItem fragment_temp = new ViewPagerFragmentMusicPlayItem();
            Bitmap bitmap = MusicUtils.getArtwork(this, music_info_temp.getMusic_id(), music_info_temp.getMusic_album_id(), true);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image);
            }
            fragment_temp.setValue(music_info_temp.getMusic_title(), music_info_temp.getMusic_artist(), bitmap);
            temp.add(fragment_temp);
            randoms.add(now_num);
        }
        PublicDate.play_randoms = randoms;
        return temp;
    }

    public int getRandomForMinMax(int min, int max) {
        Random random = new Random();
        return random.nextInt(max) % (max - min + 1) + min;
    }

    public void updateViewPagerFragment() {
        if (!is_random) {
            view_pager_fragment_list = getFragemntFromList01(PublicDate.music_play, PublicDate.music_play_list_position);
        } else {
            view_pager_fragment_list = getFragemntFromList02(PublicDate.music_play, PublicDate.music_play_list_position);
        }

    }

    public void updateIsPlayUi() {
        if (is_playing) {
            music_is_playing.setImageResource(R.drawable.is_playing);
        } else {
            music_is_playing.setImageResource(R.drawable.not_playing);
        }
    }

    public void updateViewPagerSeekBar(int play_time) {
        if (play_time > 0) {
            int max = PublicDate.music_play_now.getMusic_duration();
            float s = 0.0f;
            view_pager_seek_bar.setVisibility(View.VISIBLE);
            play_time = max - play_time;
            s = ((float) play_time / max) * phone_width * 1.0f;
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view_pager_seek_bar.getLayoutParams();
            lp.setMargins(0, 0, (int) s, 0);
            view_pager_seek_bar.setLayoutParams(lp);
        } else {
            view_pager_seek_bar.setVisibility(View.INVISIBLE);
        }
    }

}
