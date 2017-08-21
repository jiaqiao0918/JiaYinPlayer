package com.android.jiaqiao.jiayinplayer;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.jiaqiao.Fragment.FragmentMain;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.SelectMusicService;
import com.android.jiaqiao.Utils.DataInfoCache;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final int WRITE_EXTERNAL_STORAGE_QUANXAN = 200000000;
    public static final int ALL_MUSIC_UPDATE = 200000001;

    public static final int UPDATE_SHEET=300000001;


    private ArrayList<MusicInfo> music_play = new ArrayList<MusicInfo>();


    private View drawer_center_view;
    private RelativeLayout drawer_center_layout, drawer_left_layout,
            drawer_right_layout;
    private LinearLayout show_shape_view;

    private double start_sd_size = 0;
    private double restart_sd_size = 0;

    private MainActivityReceiver mReceiver;
    private IntentFilter mFilter;

    private String sd_path = Environment.getExternalStorageDirectory().getPath();


    private Intent select_music_intent;

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
        drawerCenterLayout();

        ArrayList<MusicInfo> music_all_temp = DataInfoCache.loadListCache(this, "music_all");
        listSortPinYin(music_all_temp);
        PublicDate.public_music_all = music_all_temp;
        PublicDate.list_folder_all = DataInfoCache.loadListCache(this, "list_folder_all");

        //启动service
        select_music_intent = new Intent(MainActivity.this, SelectMusicService.class);
        startService(select_music_intent);
        start_sd_size = (double) getAvailableSize();

        //动态注册广播
        mReceiver = new MainActivityReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao.SelectMusicService");
        this.registerReceiver(mReceiver, mFilter);

        //Test
//        startActivity(new Intent(MainActivity.this, MusicEditNeedListActivity.class));
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        //测算控件高度
        show_shape_view = (LinearLayout) drawer_center_view.findViewById(R.id.show_shape_view);
        ViewTreeObserver vto = show_shape_view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                show_shape_view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                PublicDate.public_drawer_center_bottom_view_height=show_shape_view.getHeight();
            }
        });


//        int view_w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//        int view_h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
//        show_shape_view.measure(view_w, view_h);
//        int view_height =show_shape_view.getMeasuredHeight();
//        PublicDate.public_drawer_center_bottom_view_height = view_height;
//        Log.i("into","public_drawer_center_bottom_view_height:"+view_height);

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
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);
            }
        });
    }


}
