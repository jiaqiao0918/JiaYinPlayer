package com.android.jiaqiao.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Service.TimingService;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jiaqiao on 2017/8/29/0029.
 */

public class TimingActivity extends Activity {

    private ArrayList<HashMap<String, String>> timing_item = new ArrayList<>();

    private ListView timing_list;
    private SimpleAdapter timing_adapter;
    private ImageView play_music_view, finish_app_view;
    private LinearLayout timing_over_play_music, timing_over_finish_app;
    private TextView timing_tittle;

    private boolean is_time = false;
    private boolean is_over_play_music = false;
    private boolean is_over_finish_app = false;


    private String[] timing_time = {"10分钟", "15分钟", "20分钟", "30分钟", "45分钟", "60分钟", "自定义"};
    private int[] timing_time_num = {10, 15, 20, 30, 45, 60};

    private String[] timing_music = {"1首", "2首", "3首", "5首", "10首", "15首", "自定义"};
    private int[] timing_music_num = {1, 2, 3, 5, 10, 15};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timing);
        this.overridePendingTransition(R.anim.dialog_enter_anim, 0);//设置ActivityToDialog的进入动画
        // 获取对话框的Window对象
        Window mWindow = this.getWindow();
        mWindow.setGravity(Gravity.CENTER);//设置Dialog在底部
        //activity to diaolog充满整个屏幕的宽度
        Display display = mWindow.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        timing_tittle = (TextView) findViewById(R.id.timing_tittle);
        timing_list = (ListView) findViewById(R.id.show_timing_item);
        play_music_view = (ImageView) findViewById(R.id.play_music_view);
        finish_app_view = (ImageView) findViewById(R.id.finish_app_view);
        timing_over_play_music = (LinearLayout) findViewById(R.id.timing_over_play_music);
        timing_over_finish_app = (LinearLayout) findViewById(R.id.timing_over_finish_app);

        is_time = getIntent().getBooleanExtra("is_time", false);
        SharedPreferences userSettings = getSharedPreferences(MainActivity.SHARED, 0);
        is_over_play_music = userSettings.getBoolean("is_over_play_music", false);
        is_over_finish_app = userSettings.getBoolean("is_over_finish_app", false);
        updateCheakBox();
        intoTimingListDate();
        if (is_time) {
            timing_tittle.setText("定时播放");
            timing_over_play_music.setVisibility(View.VISIBLE);
        } else {
            timing_tittle.setText("定曲播放");
            timing_over_play_music.setVisibility(View.GONE);
        }
        timing_adapter = new SimpleAdapter(this, timing_item, R.layout.activity_timing_list_item, new String[]{"time"}, new int[]{R.id.show_timing_item});
        timing_list.setAdapter(timing_adapter);
        timing_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == timing_item.size() - 1) {
                    startActivity(new Intent(TimingActivity.this, TimingUserNumActivity.class).putExtra("is_time02",is_time));
                    finish();
                } else {
                    Intent timing_intent = new Intent(TimingActivity.this, TimingService.class);
                    if (is_time) {

                        if (!PublicDate.is_timing_destroy) {
                            stopService(timing_intent);
                        }
                        PublicDate.is_timing_time = true;
                        PublicDate.all_timing_time = timing_time_num[position] * 60;
                        startService(timing_intent);

                        String timing_time_str = "";
                        if(timing_time_num[position]==60){
                            timing_time_str = "一小时";
                        }else{
                            timing_time_str = timing_time_num[position]+"";
                        }
                        if (is_over_finish_app) {
                            Toast.makeText(TimingActivity.this, "定时 " + timing_time_str + " 分钟后退出应用", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TimingActivity.this, "定时 " + timing_time_str + " 分钟后停止播放", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        if (!PublicDate.is_timing_destroy) {
                            stopService(timing_intent);
                        }
                        PublicDate.is_timing_time = false;
                        PublicDate.all_timing_music_sum = timing_music_num[position];
                        startService(timing_intent);
                        if (is_over_finish_app) {
                            Toast.makeText(TimingActivity.this, "定曲 " + timing_music_num[position] + " 首后退出应用", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TimingActivity.this, "定曲 " + timing_music_num[position] + " 首后停止播放", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                finish();
            }
        });
        timing_over_play_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_over_play_music) {
                    is_over_play_music = false;
                } else {
                    is_over_play_music = true;
                }
                updateCheakBox();
                getSharedPreferences(MainActivity.SHARED, 0).edit().putBoolean("is_over_play_music", is_over_play_music).commit();

            }
        });
        timing_over_finish_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_over_finish_app) {
                    is_over_finish_app = false;
                } else {
                    is_over_finish_app = true;
                }
                updateCheakBox();
                getSharedPreferences(MainActivity.SHARED, 0).edit().putBoolean("is_over_finish_app", is_over_finish_app).commit();

            }
        });


    }

    public void intoTimingListDate() {
        timing_item.clear();
        if (is_time) {
            for (int i = 0; i < timing_time.length; i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("time", timing_time[i]);
                timing_item.add(map);
            }
        } else {
            for (int i = 0; i < timing_music.length; i++) {
                HashMap<String, String> map = new HashMap<>();
                map.put("time", timing_music[i]);
                timing_item.add(map);
            }
        }
    }

    public void updateCheakBox() {
        if (is_over_play_music) {
            play_music_view.setImageResource(R.drawable.timing_is_check);
        } else {
            play_music_view.setImageResource(R.drawable.timing_not_check);
        }
        if (is_over_finish_app) {
            finish_app_view.setImageResource(R.drawable.timing_is_check);
        } else {
            finish_app_view.setImageResource(R.drawable.timing_not_check);
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }
}
