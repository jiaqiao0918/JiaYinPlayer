package com.android.jiaqiao.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Adapter.WheelviewAdapter;
import com.android.jiaqiao.Service.TimingService;
import com.android.jiaqiao.View.WheelView.WheelView;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/8/29/0029.
 */

public class TimingUserNumActivity extends Activity {

    private final String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";


    private ArrayList<String> min_list = new ArrayList<>();
    private ArrayList<String> hour_list = new ArrayList<>();
    private ArrayList<String> num_list = new ArrayList<>();

    private WheelView timing_min, timing_hour, timing_num;
    private WheelviewAdapter timing_min_adapter, timing_hour_adapter, timing_num_adapter;
    private LinearLayout timing_time_layout, timing_num_layout;
    private TextView timing_tittle;

    private boolean is_time02 = false;
    private boolean is_over_play_music = false;
    private boolean is_over_finish_app = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timing_user_num);
        this.overridePendingTransition(R.anim.dialog_enter_anim, 0);//设置ActivityToDialog的进入动画
        // 获取对话框的Window对象
        Window mWindow = this.getWindow();
        mWindow.setGravity(Gravity.CENTER);//设置Dialog在底部
        //activity to diaolog充满整个屏幕的宽度
        Display display = mWindow.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        SharedPreferences userSettings = getSharedPreferences(MainActivity.SHARED, 0);
        is_over_play_music = userSettings.getBoolean("is_over_play_music", false);
        is_over_finish_app = userSettings.getBoolean("is_over_finish_app", false);
        is_time02 = getIntent().getBooleanExtra("is_time02", false);

        timing_tittle = (TextView) findViewById(R.id.timing_tittle);
        timing_time_layout = (LinearLayout) findViewById(R.id.timing_time_layout);
        timing_num_layout = (LinearLayout) findViewById(R.id.timing_num_layout);
        if (is_time02) {
            timing_tittle.setText("定时");
            timing_time_layout.setVisibility(View.VISIBLE);
            timing_num_layout.setVisibility(View.GONE);
            for (int i = 0; i < 60; i++) {
                min_list.add("" + i);
            }
            for (int i = 0; i <= 5; i++) {
                hour_list.add("" + i);
            }
            timing_min = (WheelView) findViewById(R.id.timing_min_wheel_view);
            timing_hour = (WheelView) findViewById(R.id.timing_hour_wheel_view);
            timing_min_adapter = new WheelviewAdapter(this, min_list);
            timing_min.setVisibleItems(7);
            timing_min.setViewAdapter(timing_min_adapter);
            timing_min.setCurrentItem(1);

            timing_hour_adapter = new WheelviewAdapter(this, hour_list);
            timing_hour.setVisibleItems(7);
            timing_hour.setViewAdapter(timing_hour_adapter);
            timing_hour.setCurrentItem(0);
        } else {
            timing_tittle.setText("定曲");
            timing_time_layout.setVisibility(View.GONE);
            timing_num_layout.setVisibility(View.VISIBLE);
            for (int i = 1; i <= 20; i++) {
                num_list.add("" + i);
            }
            timing_num = (WheelView) findViewById(R.id.timing_music_num_wheel_view);
            timing_num_adapter = new WheelviewAdapter(this, num_list);
            timing_num.setVisibleItems(7);
            timing_num.setViewAdapter(timing_num_adapter);
            timing_num.setCurrentItem(0);
        }

    }


    public void over_button(View view) {
        int hour_num = timing_hour.getCurrentItem();
        int min_num = timing_min.getCurrentItem();
        if (min_num > 0||hour_num>0) {
        Intent timing_intent = new Intent(TimingUserNumActivity.this, TimingService.class);
        if (is_time02) {
                if (hour_num >= 2) {
                    intentOtherActivity();
                } else {

                    if (!PublicDate.is_timing_destroy) {
                        stopService(timing_intent);
                    }
                    PublicDate.is_timing_time = true;
                    PublicDate.all_timing_time = (hour_num * 60 + min_num) * 60;
                    startService(timing_intent);

                    String timing_time_str = "";
                    if (hour_num > 0) {
                        timing_time_str += hour_num + "小时";
                    }
                    timing_time_str += min_num + "分钟";
                    if (is_over_finish_app) {
                        Toast.makeText(TimingUserNumActivity.this, "定时 " + timing_time_str + " 分钟后退出应用", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TimingUserNumActivity.this, "定时 " + timing_time_str + " 分钟后停止播放", Toast.LENGTH_SHORT).show();
                    }
                    finish();

            }
        } else {
            int music_num = timing_num.getCurrentItem() + 1;
            if (music_num >= 15) {
                intentOtherActivity();
            } else {

                if (!PublicDate.is_timing_destroy) {
                    stopService(timing_intent);
                }
                PublicDate.is_timing_time = false;
                PublicDate.all_timing_music_sum = music_num;
                startService(timing_intent);

                if (is_over_finish_app) {
                    Toast.makeText(TimingUserNumActivity.this, "定曲 " + music_num + " 首后退出应用", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TimingUserNumActivity.this, "定曲 " + music_num + " 首后停止播放", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        }}
    }

    public void intentOtherActivity() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        Intent iStatus = this.registerReceiver(null, iFilter);
        boolean isConnected = iStatus.getIntExtra("state", 0) == 1;
        if (isConnected) {
            int timing_time_num = 0;
            if (is_time02) {
                int hour_num = timing_hour.getCurrentItem();
                int min_num = timing_min.getCurrentItem();
                timing_time_num = (hour_num * 60 + min_num) * 60;
            } else {
                int music_num = timing_num.getCurrentItem() + 1;
                timing_time_num = music_num;
            }
            if (timing_time_num > 0) {
                startActivity(new Intent(TimingUserNumActivity.this, TimingTimeNumTooLongActivity.class).putExtra("is_time03", is_time02).putExtra("timing_time_num", timing_time_num));
            }
        } else {
            Log.i("into", "耳机未插入");
        }
        finish();
    }


    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }


}
