package com.android.jiaqiao.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (PublicDate.is_timing_time) {
            time = PublicDate.all_timing_time;
            if (time > 0) {

                if(!PublicDate.is_play) {
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
                            PublicDate.is_timing_time = false;
                            PublicDate.all_timing_time = 0;

                            if(PublicDate.is_timing_over_finish) {
                                android.os.Process.killProcess(android.os.Process.myPid());//关闭此应用
                            }

//                            Intent temp_intent02 = new Intent();
//                            temp_intent02.setAction("com.android.jiaqiao");
//                            temp_intent02.putExtra("type", TimingService.TIMING_DESTROY);
//                            sendBroadcast(temp_intent02);
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

    class TimingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case TimingService.TIMING_MUSIC_POSITION:
                    timing_music_sum++;
                    if (PublicDate.all_timing_music_sum == timing_music_sum && PublicDate.all_timing_music_sum > 0) {
                        PublicDate.is_timing_time = false;
                        PublicDate.all_timing_music_sum = 0;

                        if(PublicDate.is_timing_over_finish) {
                            android.os.Process.killProcess(android.os.Process.myPid());//关闭此应用
                        }

//                        Intent temp_intent02 = new Intent();
//                        temp_intent02.setAction("com.android.jiaqiao");
//                        temp_intent02.putExtra("type", TimingService.TIMING_DESTROY);
//                        sendBroadcast(temp_intent02);
                    }
                    break;
            }
        }
    }



}



