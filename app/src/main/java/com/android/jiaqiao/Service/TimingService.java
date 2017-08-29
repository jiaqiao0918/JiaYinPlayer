package com.android.jiaqiao.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.jiaqiao.Utils.ActivityContainer;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jiaqiao on 2017/8/28/0028.
 */

public class TimingService extends Service {

    public static final int TIMING_MUSIC_POSITION = 800000001;
    public static final int TIMING_DESTROY = 800000002;
    public static final int TIMING_ONLY_DESTROY = 800000003;

    private boolean is_timing = false;
    private int num = 0;
    private int timing_music_sum = 0;//已播放的歌曲数量
    private boolean is_over_music_play = false;
    private int time = 0;
    private TimingReceiver mReceiver;
    private IntentFilter mFilter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new TimingReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        registerReceiver(mReceiver, mFilter);
        is_timing = false;
        Log.i("into", "timing_running");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (PublicDate.is_timing_time) {
            time = PublicDate.all_timing_time;
            if (time > 0) {

                if (!PublicDate.is_play) {
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MusicPlayService.START_STOP_MUSIC);
                    sendBroadcast(temp_intent);
                }

                num = 0;
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (num < time) {
                            num++;
                            is_timing = true;
                        } else {
                            is_timing = false;
                            PublicDate.all_timing_time = 0;
                            timingOver();
                            Log.i("into","定时"+num);
                            this.cancel();
                        }
                    }
                }, 0, 1000);
            }
        } else {
            if (PublicDate.all_timing_music_sum > 0) {
                if (!PublicDate.is_play) {
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MusicPlayService.START_STOP_MUSIC);
                    sendBroadcast(temp_intent);
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        PublicDate.is_timing_destroy = true;
    }

    public void timingOver() {
        SharedPreferences userSettings = getSharedPreferences(MainActivity.SHARED, 0);
        Log.i("into", "is_timing_time:" + PublicDate.is_timing_time);
        if (PublicDate.is_timing_time) {
            Log.i("into", "定时结束");
            Log.i("into", "is_over_play_music01:" + userSettings.getBoolean("is_over_play_music", false));
            Log.i("into", "is_over_finish_app01:" + userSettings.getBoolean("is_over_finish_app", false));
            if (userSettings.getBoolean("is_over_play_music", false)) {
                is_over_music_play = true;
                Log.i("into", "等待播放结束...");
            } else if (userSettings.getBoolean("is_over_finish_app", false)) {
                PublicDate.is_timing_time = false;
                Log.i("into", "finish_in01");
                ActivityContainer.getInstance().finishAllActivity();
            } else {
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", TimingService.TIMING_DESTROY);
                sendBroadcast(temp_intent);
            }

        } else {
            Log.i("into", "is_over_finish_app02:" + userSettings.getBoolean("is_over_finish_app", false));
            if (userSettings.getBoolean("is_over_finish_app", false)) {
                PublicDate.is_timing_time = false;
                Log.i("into", "finish_in02");
                ActivityContainer.getInstance().finishAllActivity();//退出应用
            }
        }
    }

    class TimingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case TimingService.TIMING_MUSIC_POSITION:
                    if (PublicDate.is_timing_time) {
                        if (is_over_music_play) {
                            Log.i("into", "播放完毕");
                            SharedPreferences userSettings = getSharedPreferences(MainActivity.SHARED, 0);
                            if (userSettings.getBoolean("is_over_finish_app", false)) {
                                PublicDate.is_timing_time = false;
                                ActivityContainer.getInstance().finishAllActivity();
                            }else{
                                Intent temp_intent = new Intent();
                                temp_intent.setAction("com.android.jiaqiao");
                                temp_intent.putExtra("type", TimingService.TIMING_DESTROY);
                                sendBroadcast(temp_intent);
                            }
                        }
                    } else {
                        timing_music_sum++;
                        if (PublicDate.all_timing_music_sum == timing_music_sum && PublicDate.all_timing_music_sum > 0) {
                            PublicDate.is_timing_time = false;
                            PublicDate.all_timing_music_sum = 0;
                            timingOver();
                        }
                    }
                    break;
            }
        }
    }


}



