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
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.jiaqiao.Fragment.FragmentMain;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.SelectMusicService;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static final int WRITE_EXTERNAL_STORAGE_QUANXAN = 200000000;
    public static final int ALL_MUSIC_UPDATE = 200000001;

    private ArrayList<MusicInfo> music_play = new ArrayList<MusicInfo>();
    private View drawer_center_view;
    private RelativeLayout drawer_center_layout, drawer_left_layout,
            drawer_right_layout;

    private double start_sd_size = 0;
    private double restart_sd_size = 0;

    private MainActivityReceiver mReceiver;
    private IntentFilter mFilter;

    private Intent select_music_intent ;

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
        /*
        music_all.clear();
        getAllMusic(this);
        music_all = listToList(all_list);
        PublicDate.music_all = music_all;
        */
        //启动service
        select_music_intent = new Intent(MainActivity.this, SelectMusicService.class);
        startService(select_music_intent);
        start_sd_size = (double) getAvailableSize();

        //动态注册广播
        mReceiver = new MainActivityReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao.SelectMusicService");
        this.registerReceiver(mReceiver, mFilter);
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
        if(!PublicDate.is_service_select_music_destroy){
            stopService(select_music_intent);
        }
    }

    class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    if(!PublicDate.is_service_select_music_destroy){
                        stopService(select_music_intent);
                    }
                    break;
            }
        }

    }

}
