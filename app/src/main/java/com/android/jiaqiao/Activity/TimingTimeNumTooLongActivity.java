package com.android.jiaqiao.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Service.TimingService;
import com.android.jiaqiao.Utils.SharedUtile;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

/**
 * Created by jiaqiao on 2017/8/29/0029.
 */

public class TimingTimeNumTooLongActivity extends Activity {

    private boolean is_time03 = false;
    private int timing_time_num = 0;
    private boolean is_over_play_music = false;
    private boolean is_over_finish_app = false;

    private TextView too_long_tittle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timing_too_long);
        this.overridePendingTransition(R.anim.dialog_enter_anim, 0);//设置ActivityToDialog的进入动画
        // 获取对话框的Window对象
        Window mWindow = this.getWindow();
        mWindow.setGravity(Gravity.CENTER);//设置Dialog在底部
        //activity to diaolog充满整个屏幕的宽度
        Display display = mWindow.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

//        SharedPreferences userSettings = getSharedPreferences(MainActivity.SHARED, 0);
//        is_over_play_music = userSettings.getBoolean("is_over_play_music", false);
//        is_over_finish_app = userSettings.getBoolean("is_over_finish_app", false);

        is_over_play_music = SharedUtile.getSharedBoolean(this, "is_over_play_music", false);
        is_over_finish_app = SharedUtile.getSharedBoolean(this, "is_over_finish_app", false);

        is_time03 = getIntent().getBooleanExtra("is_time03", false);
        timing_time_num = getIntent().getIntExtra("timing_time_num", 0);
        too_long_tittle = (TextView) findViewById(R.id.too_long_tittle);

        if (is_time03) {
            if (timing_time_num >= 0) {
                String timing_time_str = "";
                int hour_num = timing_time_num / 60 / 60;
                int min_num = timing_time_num / 60 % 60;
                if (hour_num > 0) {
                    timing_time_str += hour_num + "小时";
                }
                if (min_num > 0) {
                    timing_time_str += min_num + "分钟";
                }
                too_long_tittle.setText("已定时" + timing_time_str);
            } else {
                finish();
            }
        } else {
            if (timing_time_num >= 0) {
                too_long_tittle.setText("已定曲" + timing_time_num + "首");
            } else {
                finish();
            }
        }

    }

    public void canael_button(View view) {
        finish();
    }

    public void over_button(View view) {
        Intent timing_intent = new Intent(TimingTimeNumTooLongActivity.this, TimingService.class);
        if (is_time03) {
            if (!PublicDate.is_timing_destroy) {
                stopService(timing_intent);
            }
            PublicDate.is_timing_time = true;
            PublicDate.all_timing_time = timing_time_num;
            startService(timing_intent);

            String timing_time_str = "";
            int hour_num = timing_time_num / 60 / 60;
            int min_num = timing_time_num / 60 % 60;
            if (hour_num > 0) {
                timing_time_str += hour_num + "小时";
            }
            if (min_num > 0) {
                timing_time_str += min_num + "分钟";
            }
            if (is_over_finish_app) {
                Toast.makeText(TimingTimeNumTooLongActivity.this, "定时 " + timing_time_str + " 分钟后退出应用", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TimingTimeNumTooLongActivity.this, "定时 " + timing_time_str + " 分钟后停止播放", Toast.LENGTH_SHORT).show();
            }
            finish();
        }else{
            if (!PublicDate.is_timing_destroy) {
                stopService(timing_intent);
            }
            PublicDate.is_timing_time = false;
            PublicDate.all_timing_music_sum = timing_time_num;
            startService(timing_intent);

            if (is_over_finish_app) {
                Toast.makeText(TimingTimeNumTooLongActivity.this, "定曲 " + timing_time_num + " 首后退出应用", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TimingTimeNumTooLongActivity.this, "定曲 " + timing_time_num + " 首后停止播放", Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }
}