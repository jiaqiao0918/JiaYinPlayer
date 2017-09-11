package com.android.jiaqiao.jiayinplayer;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Activity.MusicEditItemLongActivity;
import com.android.jiaqiao.Activity.TimingActivity;
import com.android.jiaqiao.Adapter.LeftRightRecyclerViewAdapter;
import com.android.jiaqiao.Adapter.ViewPagerFragmentAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.MusicPlayService;
import com.android.jiaqiao.Service.SelectMusicService;
import com.android.jiaqiao.Service.TimingService;
import com.android.jiaqiao.Service.UpdateServiec;
import com.android.jiaqiao.UiFragment.FragmentMain;
import com.android.jiaqiao.Utils.ActivityContainer;
import com.android.jiaqiao.Utils.DataInfoCache;
import com.android.jiaqiao.Utils.FastBlurUtil;
import com.android.jiaqiao.Utils.MusicPlayUtil;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.Utils.SharedUtile;
import com.android.jiaqiao.ViewPagerFragment.ViewPagerFragmentMusicPlayItem;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    public static final int WRITE_EXTERNAL_STORAGE_QUANXAN = 200;
    public static final int ALL_MUSIC_UPDATE = 2000001;
    public static final int START_ACTIVITY_TO_OTHER = 2000002;

    public static final int UPDATE_VIEW_PAGER_SHEET = 3000001;
    public static final int UPDATE_MUSIC_PLAY = 3000002;
    public static final int SERVICE_UPDATE_MUSIC_PLAY = 3000003;
    public static final int VIEW_PAGER_UPDATE_LIST = 3000004;
    public static final int LOVE_MUSIC_UPDATE = 3000005;
    public static final int AUTO_PLAY_NEXT = 3000006;
    public static final int UPDATE_FRAGMENT_SHEET = 3000007;
    public static final int UPDATE_FRAGMENT_MAIN_SET = 3000008;
    public static final int UPDATE_FRAGMENT_MUSIC_ALL_SET = 3000009;
    public static final int UPDATE_FRAGMENT_MUSIC_SHEET_SET = 30000010;

    public static final String SHARED = "setting";

    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> music_play_list_temp = new ArrayList<MusicInfo>();
    private ArrayList<Fragment> view_pager_fragment_list = new ArrayList<Fragment>();

    private View drawer_center_view, drawer_left_view, drawer_right_view;
    private RelativeLayout drawer_center_layout, drawer_left_layout,
            drawer_right_layout;
    private LinearLayout show_view_pager_layout, activity_left_bottom, activity_right_bottom;
    private ViewPager view_pager_fragment;
    private ViewPagerFragmentAdapter view_pager_fragment_adapter;
    private ImageView music_is_playing;
    private View view_pager_seek_bar;
    private ImageView left_timing_time, right_timing_time, left_timing_music_position, right_timing_music_position, left_setting, right_setting, left_break_finish, right_break_finish;
    private RecyclerView left_list, right_list;
    private LeftRightRecyclerViewAdapter left_right_adapter;
    private TextView left_music_tittle, right_music_tittle, left_music_artist, right_music_artist;
    private ImageView left_album_image, right_album_image;
    private DrawerLayout activity_drawer_layout;

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
    private ArrayList<MusicInfo> music_now_play_list = new ArrayList<MusicInfo>();
    private int last_click_position = 0;
    private int list_scroll_position = 0;

    private MainActivityReceiver mReceiver;
    private IntentFilter mFilter;

    private FragmentMain fragment_main;

    private String sd_path = Environment.getExternalStorageDirectory().getPath();


    private Intent select_music_intent;
    private Intent music_play_intent;
    private Intent timing_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer_center_layout = (RelativeLayout) findViewById(R.id.drawer_center_layout);
        drawer_left_layout = (RelativeLayout) findViewById(R.id.drawer_left_layout);
        drawer_right_layout = (RelativeLayout) findViewById(R.id.drawer_right_layout);
        activity_drawer_layout = (DrawerLayout) findViewById(R.id.activity_drawer_layout);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用代码设置状态栏透明，使用xml，在部分rom上会失效
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        ActivityContainer.getInstance().addActivity(this);

        PublicDate.path_files_dir = getFilesDir();//获取软件在date文件夹下的路径
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        phone_width = dm.widthPixels;

        ArrayList<MusicInfo> music_all_temp = DataInfoCache.loadListCache(this, "music_all");
        listSortPinYin(music_all_temp);
        PublicDate.public_music_all = music_all_temp;
        music_all = music_all_temp;
        PublicDate.list_folder_all = DataInfoCache.loadListCache(this, "list_folder_all");

//        SharedPreferences userSettings = getSharedPreferences(MainActivity.SHARED, 0);
//        PublicDate.music_play_list_str = userSettings.getString("music_play_list_str", "");
        PublicDate.music_play_list_str = SharedUtile.getSharedString(this, "music_play_list_str", "");

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

//            PublicDate.music_play_list_position = userSettings.getInt("music_play_list_position", 0);
            PublicDate.music_play_list_position = SharedUtile.getSharedInt(this, "music_play_list_position", 0);
            now_show_position = PublicDate.music_play_list_position;
            PublicDate.music_play_now = PublicDate.music_play.get(now_show_position);
            music_play_list_temp = PublicDate.music_play;
        }


//        PublicDate.play_mode = userSettings.getInt("play_mode", MusicPlayService.PLAY_MODE_ORDER);
        PublicDate.play_mode = SharedUtile.getSharedInt(this, "play_mode", MusicPlayService.PLAY_MODE_ORDER);

        if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_RANDOM) {
            is_random = true;
        }

        //启动service
        select_music_intent = new Intent(MainActivity.this, SelectMusicService.class);
        startService(select_music_intent);
        music_play_intent = new Intent(MainActivity.this, MusicPlayService.class);
        startService(music_play_intent);
        timing_intent = new Intent(MainActivity.this, TimingService.class);

//        PublicDate.is_timing_time = true;
//        PublicDate.all_timing_time = 15;
//        PublicDate.all_timing_music_sum = 3;
//        startService(timing_intent);

        start_sd_size = (double) getAvailableSize();//获取手机内置存储卡的剩余空间，用于判断文件是否变化

        //动态注册广播
        mReceiver = new MainActivityReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        this.registerReceiver(mReceiver, mFilter);

        drawerLeftRightLayout();
        drawerCenterLayout();

        //Test
//        startActivity(new Intent(this,SetActivity.class));


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
        fragment_main = new FragmentMain();
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
                if (PublicDate.music_play.size() > 0) {
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

    public void drawerLeftRightLayout() {
        drawer_left_view = getLayoutInflater().inflate(
                R.layout.left_right_layout, null);
        drawer_right_view = getLayoutInflater().inflate(
                R.layout.left_right_layout, null);

        activity_left_bottom = (LinearLayout) drawer_left_view.findViewById(R.id.activity_left_right_bottom);
        activity_right_bottom = (LinearLayout) drawer_right_view.findViewById(R.id.activity_left_right_bottom);
        left_timing_time = (ImageView) drawer_left_view.findViewById(R.id.timing_time);
        right_timing_time = (ImageView) drawer_right_view.findViewById(R.id.timing_time);
        left_timing_music_position = (ImageView) drawer_left_view.findViewById(R.id.timing_music_position);
        right_timing_music_position = (ImageView) drawer_right_view.findViewById(R.id.timing_music_position);
        left_setting = (ImageView) drawer_left_view.findViewById(R.id.setting);
        right_setting = (ImageView) drawer_right_view.findViewById(R.id.setting);
        left_break_finish = (ImageView) drawer_left_view.findViewById(R.id.break_finish);
        right_break_finish = (ImageView) drawer_right_view.findViewById(R.id.break_finish);
        left_list = (RecyclerView) drawer_left_view.findViewById(R.id.left_right_list);
        right_list = (RecyclerView) drawer_right_view.findViewById(R.id.left_right_list);
        left_music_tittle = (TextView) drawer_left_view.findViewById(R.id.left_right_music_tittle);
        right_music_tittle = (TextView) drawer_right_view.findViewById(R.id.left_right_music_tittle);
        left_music_artist = (TextView) drawer_left_view.findViewById(R.id.left_right_music_artist);
        right_music_artist = (TextView) drawer_right_view.findViewById(R.id.left_right_music_artist);
        left_album_image = (ImageView) drawer_left_view.findViewById(R.id.left_right_album_image);
        right_album_image = (ImageView) drawer_right_view.findViewById(R.id.left_right_album_image);

        activity_left_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        activity_right_bottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        left_timing_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_drawer_layout.isDrawerOpen(Gravity.LEFT)，判断左边侧滑栏是否打开
                if (activity_drawer_layout.isDrawerOpen(Gravity.LEFT) || activity_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                    activity_drawer_layout.closeDrawers();//关闭所有的侧滑栏
                }
                startActivity(new Intent(MainActivity.this, TimingActivity.class).putExtra("is_time", true));
            }
        });
        right_timing_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_drawer_layout.isDrawerOpen(Gravity.LEFT)，判断左边侧滑栏是否打开
                if (activity_drawer_layout.isDrawerOpen(Gravity.LEFT) || activity_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                    activity_drawer_layout.closeDrawers();//关闭所有的侧滑栏
                }
                startActivity(new Intent(MainActivity.this, TimingActivity.class).putExtra("is_time", true));
            }
        });
        left_timing_music_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_drawer_layout.isDrawerOpen(Gravity.LEFT)，判断左边侧滑栏是否打开
                if (activity_drawer_layout.isDrawerOpen(Gravity.LEFT) || activity_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                    activity_drawer_layout.closeDrawers();//关闭所有的侧滑栏
                }
                startActivity(new Intent(MainActivity.this, TimingActivity.class).putExtra("is_time", false));
            }
        });
        right_timing_music_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //activity_drawer_layout.isDrawerOpen(Gravity.LEFT)，判断左边侧滑栏是否打开
                if (activity_drawer_layout.isDrawerOpen(Gravity.LEFT) || activity_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                    activity_drawer_layout.closeDrawers();//关闭所有的侧滑栏
                }
                startActivity(new Intent(MainActivity.this, TimingActivity.class).putExtra("is_time", false));
            }
        });
        left_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity_drawer_layout.isDrawerOpen(Gravity.LEFT) || activity_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                    activity_drawer_layout.closeDrawers();//关闭所有的侧滑栏
                }
                startActivity(new Intent(MainActivity.this, SetActivity.class));
            }
        });
        right_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity_drawer_layout.isDrawerOpen(Gravity.LEFT) || activity_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                    activity_drawer_layout.closeDrawers();//关闭所有的侧滑栏
                }
                startActivity(new Intent(MainActivity.this, SetActivity.class));
            }
        });
        left_break_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        right_break_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        updateLeftRightList();
        drawer_left_layout.addView(drawer_left_view);
        drawer_right_layout.addView(drawer_right_view);

    }

    public void updateLeftRightList() {
        if (PublicDate.music_play.size() > 0) {
            music_now_play_list = PublicDate.music_play;
            for (int i = 0; i < music_now_play_list.size(); i++) {
                music_now_play_list.get(i).setIs_playing(false);
            }
            music_now_play_list.get(PublicDate.music_play_list_position).setIs_playing(true);


            // 创建默认的线性LayoutManager
            left_list.setLayoutManager(new LinearLayoutManager(this));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            left_list.setHasFixedSize(true);
            // 创建并设置Adapter
            left_right_adapter = new LeftRightRecyclerViewAdapter(music_now_play_list);
            left_right_adapter.setOnItemClickListener(new LeftRightRecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件


                    music_now_play_list.get(last_click_position).setIs_playing(false);
                    music_now_play_list.get(position).setIs_playing(true);
                    left_right_adapter.notifyItemChanged(last_click_position);//刷新单个数据
                    left_right_adapter.notifyItemChanged(position);


                    PublicDate.music_play_now = music_now_play_list.get(position);
                    PublicDate.music_play_list_position = position;

//                    getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                    SharedUtile.putSharedInt(MainActivity.this, "music_play_list_position", PublicDate.music_play_list_position);

                    //发送广播
                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY);
                    temp_intent02.putExtra("is_update_music_play", true);
                    sendBroadcast(temp_intent02);
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MusicPlayService.PLAY_MUSIC);
                    sendBroadcast(temp_intent);

                    Intent temp_intent03 = new Intent();
                    temp_intent03.setAction("com.android.jiaqiao");
                    temp_intent03.putExtra("type", UpdateServiec.TO_UPDATE_UI);
                    sendBroadcast(temp_intent03);

                    now_show_position = PublicDate.music_play_list_position;
                    updateIsPlayUi();
                    updateViewPagerFragment();
                    view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                    view_pager_fragment.setCurrentItem(2, false); //设置当前页是第0页，false为不需要过渡动画，默认为true
                    updateLeftRightListItem();
                    last_click_position = position;
                }
            });
            left_right_adapter.setOnItemLongClickListener(new LeftRightRecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {

                    //item长按事件
                    ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                    music_edit_select.add(position);
                    PublicDate.public_music_edit_temp = music_now_play_list;
                    PublicDate.public_music_edit_temp_select = music_edit_select;
                    PublicDate.is_music_play = true;
                    startActivity(new Intent(MainActivity.this, MusicEditItemLongActivity.class).putExtra("is_all_music_01", false).putExtra("is_music_play_list01", true));
                }
            });
            left_list.setAdapter(left_right_adapter);

            // 创建默认的线性LayoutManager
            right_list.setLayoutManager(new LinearLayoutManager(this));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            right_list.setHasFixedSize(true);
            right_list.setAdapter(left_right_adapter);

            updateLeftRightListItem();
        }
    }

    public void updateLeftRightAllList() {
        music_now_play_list = PublicDate.music_play;
        if (PublicDate.music_play.size() > 0) {
            music_now_play_list = PublicDate.music_play;
            for (int i = 0; i < music_now_play_list.size(); i++) {
                music_now_play_list.get(i).setIs_playing(false);
            }
            music_now_play_list.get(PublicDate.music_play_list_position).setIs_playing(true);


            // 创建默认的线性LayoutManager
            left_list.setLayoutManager(new LinearLayoutManager(this));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            left_list.setHasFixedSize(true);
            // 创建并设置Adapter
            left_right_adapter = new LeftRightRecyclerViewAdapter(music_now_play_list);
            left_right_adapter.setOnItemClickListener(new LeftRightRecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件

                    last_click_position = PublicDate.music_play_list_position;
                    music_now_play_list.get(last_click_position).setIs_playing(false);
                    music_now_play_list.get(position).setIs_playing(true);
                    left_right_adapter.notifyItemChanged(last_click_position);//刷新单个数据
                    left_right_adapter.notifyItemChanged(position);


                    PublicDate.music_play_now = music_now_play_list.get(position);
                    PublicDate.music_play_list_position = position;

//                    getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                    SharedUtile.putSharedInt(MainActivity.this, "music_play_list_position", PublicDate.music_play_list_position);


                    //发送广播
                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", MusicPlayActivity.UPDATE_MUSIC_PLAY_ACTIVITY);
                    temp_intent02.putExtra("is_update_music_play", true);
                    sendBroadcast(temp_intent02);
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MusicPlayService.PLAY_MUSIC);
                    sendBroadcast(temp_intent);

                    Intent temp_intent03 = new Intent();
                    temp_intent03.setAction("com.android.jiaqiao");
                    temp_intent03.putExtra("type", UpdateServiec.TO_UPDATE_UI);
                    sendBroadcast(temp_intent03);

                    now_show_position = PublicDate.music_play_list_position;
                    updateIsPlayUi();
                    updateViewPagerFragment();
                    view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                    view_pager_fragment.setCurrentItem(2, false); //设置当前页是第0页，false为不需要过渡动画，默认为true
//                    updateLeftRightListItem();

                    last_click_position = position;
                }
            });
            left_right_adapter.setOnItemLongClickListener(new LeftRightRecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {

                    //item长按事件
                    ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                    music_edit_select.add(position);
                    PublicDate.public_music_edit_temp = music_now_play_list;
                    PublicDate.public_music_edit_temp_select = music_edit_select;
                    PublicDate.is_music_play = true;
                    startActivity(new Intent(MainActivity.this, MusicEditItemLongActivity.class).putExtra("is_all_music_01", false).putExtra("is_music_play_list01", true).putExtra("is_main_play", true));
                }
            });
            left_list.setAdapter(left_right_adapter);

            // 创建默认的线性LayoutManager
            right_list.setLayoutManager(new LinearLayoutManager(this));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            right_list.setHasFixedSize(true);
            right_list.setAdapter(left_right_adapter);

            left_music_tittle.setText(PublicDate.music_play_now.getMusic_title());
            right_music_tittle.setText(PublicDate.music_play_now.getMusic_title());
            left_music_artist.setText(PublicDate.music_play_now.getMusic_artist());
            right_music_artist.setText(PublicDate.music_play_now.getMusic_artist());

            updateLeftRightAlbumImage();

        }
    }


    public void updateLeftRightListItem() {
        if (PublicDate.music_play.size() > 0) {
            if (music_now_play_list.size() <= 0) {
                music_now_play_list = PublicDate.music_play;
            }
            if (music_now_play_list.size() > 0) {
                music_now_play_list.get(last_click_position).setIs_playing(false);
                left_right_adapter.notifyItemChanged(last_click_position);//刷新单个数据
                last_click_position = PublicDate.music_play_list_position;
                if (last_click_position > -1) {
                    music_now_play_list.get(last_click_position).setIs_playing(true);
                    left_right_adapter.notifyItemChanged(last_click_position);//刷新单个数据
                }
                if (last_click_position > 0) {
                    list_scroll_position = last_click_position - 1;
                }

                if (PublicDate.music_play.size() > 11) {
                    ((LinearLayoutManager) left_list.getLayoutManager()).scrollToPositionWithOffset(list_scroll_position, 0);
                    ((LinearLayoutManager) left_list.getLayoutManager()).setStackFromEnd(true);
                    ((LinearLayoutManager) right_list.getLayoutManager()).scrollToPositionWithOffset(list_scroll_position, 0);
                    ((LinearLayoutManager) right_list.getLayoutManager()).setStackFromEnd(true);
                }
                left_music_tittle.setText(PublicDate.music_play_now.getMusic_title());
                right_music_tittle.setText(PublicDate.music_play_now.getMusic_title());
                left_music_artist.setText(PublicDate.music_play_now.getMusic_artist());
                right_music_artist.setText(PublicDate.music_play_now.getMusic_artist());

                updateLeftRightAlbumImage();

            } else {
                left_music_tittle.setText("");
                right_music_tittle.setText("");
                left_music_artist.setText("");
                right_music_artist.setText("");
            }
        }
    }

    public void updateLeftRightAlbumImage() {
        Bitmap bitmap = MusicUtils.getArtwork(MainActivity.this, PublicDate.music_play_now.getMusic_id(), PublicDate.music_play_now.getMusic_album_id(), true);
        if (bitmap == null) {
            left_album_image.setImageResource(R.color.back_ground);
            right_album_image.setImageResource(R.color.back_ground);
        } else {
            setImageViewImage(left_album_image, bitmap);
            setImageViewImage(right_album_image, bitmap);
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

//                            getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                            SharedUtile.putSharedInt(MainActivity.this, "music_play_list_position", PublicDate.music_play_list_position);


                            is_need_update = false;
                            play_time = 0;
                            updateViewPagerSeekBar(play_time);
                            updateLeftRightListItem();

                            Intent temp_intent = new Intent();
                            temp_intent.setAction("com.android.jiaqiao");
                            temp_intent.putExtra("type", MusicPlayService.PLAY_MUSIC);
                            sendBroadcast(temp_intent);

                            Intent temp_intent05 = new Intent();
                            temp_intent05.setAction("com.android.jiaqiao");
                            temp_intent05.putExtra("type", UpdateServiec.TO_UPDATE_UI);
                            sendBroadcast(temp_intent05);

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
        if (show_view_pager_layout != null) {
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //拦截返回键操作，返回桌面，而不是退出应用
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            //activity_drawer_layout.isDrawerOpen(Gravity.LEFT)，判断左边侧滑栏是否打开
            if (activity_drawer_layout.isDrawerOpen(Gravity.LEFT) || activity_drawer_layout.isDrawerOpen(Gravity.RIGHT)) {
                activity_drawer_layout.closeDrawers();//关闭所有的侧滑栏
                return true;
            } else if (getFragmentManager().getBackStackEntryCount() > 0) {
                //判断栈中是否有还未返回的fragment，栈中有多个activity的情况不用考虑
                getFragmentManager().popBackStack();
            } else {
                Intent to_home = new Intent(Intent.ACTION_MAIN);
                to_home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                to_home.addCategory(Intent.CATEGORY_HOME);
                startActivity(to_home);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        Bitmap image_view_bitmap = null;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!PublicDate.is_service_select_music_destroy) {
            stopService(select_music_intent);
        }
        if (!PublicDate.is_music_play_destroy) {
            stopService(music_play_intent);
        }
        if (!PublicDate.is_timing_destroy) {
            stopService(timing_intent);
        }
        unregisterReceiver(mReceiver);
        ActivityContainer.getInstance().removeActivity(this);
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
            num = (now_show_position + 2 + PublicDate.music_play.size()) % PublicDate.music_play.size();
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
        MusicInfo music_info_temp = PublicDate.music_play.get(num);
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

        if (music_play_list_temp.size() <= 0) {
            music_play_list_temp = PublicDate.music_play;
        }
        if (music_play_list_temp.size() > 0) {
            view_pager_fragment_list.remove(view_pager_fragment_list.size() - 1);
            randoms.remove(randoms.size() - 1);
            int num = -1;
            if (!is_random) {
                num = (now_show_position - 2 + PublicDate.music_play.size()) % PublicDate.music_play.size();
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
            MusicInfo music_info_temp = PublicDate.music_play.get(num);
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
        if (min != max) {
            Random random = new Random();
            return random.nextInt(max) % (max - min + 1) + min;
        }
        return min;
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
                        updateLeftRightAllList();
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

//                            updateLeftRightAllList();
                            updateLeftRightAllList();
                        } else {
                            show_view_pager_layout.setVisibility(View.INVISIBLE);
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
                case MainActivity.AUTO_PLAY_NEXT:

                    if (intent.getBooleanExtra("auto_update_mode", false)) {
                        viewPagerRight();
                    } else {
                        viewPagerLeft();
                    }
                    updateViewPagerFragment();
                    view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                    view_pager_fragment.setCurrentItem(2, false); //设置当前页是第2页，false为不需要过渡动画，默认为true
                    updateLeftRightListItem();
                    play_time = 0;
                    updateViewPagerSeekBar(play_time);
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

//                        getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                        SharedUtile.putSharedInt(MainActivity.this, "music_play_list_position", PublicDate.music_play_list_position);


                        updateLeftRightListItem();
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
                case TimingService.TIMING_DESTROY:
                    if (!PublicDate.is_timing_destroy) {
                        stopService(timing_intent);
                        if (PublicDate.is_play) {
                            Intent temp_intent = new Intent();
                            temp_intent.setAction("com.android.jiaqiao");
                            temp_intent.putExtra("type", MusicPlayService.START_STOP_MUSIC);
                            sendBroadcast(temp_intent);
                        }
                    }
                    break;
                case TimingService.TIMING_ONLY_DESTROY:
                    if (PublicDate.is_play) {
                        Intent temp_intent = new Intent();
                        temp_intent.setAction("com.android.jiaqiao");
                        temp_intent.putExtra("type", MusicPlayService.START_STOP_MUSIC);
                        sendBroadcast(temp_intent);
                    }
                    break;
                case MainActivity.UPDATE_VIEW_PAGER_SHEET:
                    updateLeftRightAllList();
                    now_show_position = PublicDate.music_play_list_position;
                    updateIsPlayUi();
                    updateViewPagerFragment();
                    view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                    view_pager_fragment.setCurrentItem(2, false); //设置当前页是第0页，false为不需要过渡动画，默认为true
                    break;


                //Test
                case UpdateServiec.UPDATE_UI:

                    music_play_list_temp = PublicDate.music_play;
                    if (music_play_list_temp.size() > 0) {
                        now_show_position = PublicDate.music_play_list_position;
                        updateIsPlayUi();
                        updateViewPagerFragment();
                        view_pager_fragment_adapter.UpdateList(view_pager_fragment_list);
                        view_pager_fragment.setCurrentItem(2, false); //设置当前页是第0页，false为不需要过渡动画，默认为true

                        updateLeftRightAllList();

                    } else {
                        show_view_pager_layout.setVisibility(View.INVISIBLE);
                    }

                    break;

            }
        }

    }

}
