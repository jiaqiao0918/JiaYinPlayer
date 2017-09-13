package com.android.jiaqiao.Others;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.android.jiaqiao.Service.MusicPlayService;
import com.android.jiaqiao.Service.UpdateService;
import com.android.jiaqiao.Utils.SharedUtile;
import com.android.jiaqiao.jiayinplayer.PublicDate;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jiaqiao on 2017/9/13/0013.
 */

public class MediaButtonReceiver extends BroadcastReceiver {


    private Timer timer = null;
    private MTask myTimer = null;
    private Context mContext;

    public MediaButtonReceiver() {
        timer = new Timer(true);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        String action = intent.getAction();
        // 获得KeyEvent对象
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (Intent.ACTION_MEDIA_BUTTON.equals(action) && event.getAction() == KeyEvent.ACTION_UP) {
            //event.getAction()==KeyEvent.ACTION_UP  判断耳机按键的状态，up是才响应


            // 获得按键码
            int keycode = event.getKeyCode();
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    //播放下一首
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    //播放上一首
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    //中间按钮,暂停or播放
                    //可以通过发送一个新的广播通知正在播放的视频页面,暂停或者播放视频


                    if (PublicDate.clickCount == 0) {//单击
                        PublicDate.clickCount++;
                        myTimer = new MTask();
                        timer.schedule(myTimer, 1000);
                    } else if (PublicDate.clickCount == 1) {//双击
                        PublicDate.clickCount++;
                    } else if (PublicDate.clickCount == 2) {//三连击
                        PublicDate.clickCount++;
                    }

                    break;
            }
        }
        abortBroadcast();
    }

    /**
     * 定时器，用于延迟1秒，判断是否会发生双击和三连击
     */
    class MTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (PublicDate.clickCount == 1) {//单击
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MusicPlayService.START_STOP_MUSIC);
                    mContext.sendBroadcast(temp_intent);
                } else if (PublicDate.clickCount == 2) {//双击
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


//                getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                    SharedUtile.putSharedInt(mContext,"music_play_list_position", PublicDate.music_play_list_position);

//                updateActivity();

                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", UpdateService.TO_UPDATE_UI);
                    mContext.sendBroadcast(temp_intent);


                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", MusicPlayService.PLAY_MUSIC);
                    mContext.sendBroadcast(temp_intent02);


                } else if (PublicDate.clickCount == 3) {//三击

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


//                getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                    SharedUtile.putSharedInt(mContext,"music_play_list_position", PublicDate.music_play_list_position);

//                updateActivity();

                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", UpdateService.TO_UPDATE_UI);
                    mContext.sendBroadcast(temp_intent);


                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", MusicPlayService.PLAY_MUSIC);
                    mContext.sendBroadcast(temp_intent02);


                    PublicDate.clickCount = 0;
                    timer.cancel();
                }
                PublicDate.clickCount = 0;
            } catch (Exception e) {
            }
        }
    }
}