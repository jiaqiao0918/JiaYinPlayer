package com.android.jiaqiao.jiayinplayer;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.jiaqiao.Adapter.SetFolderListViewAdapter;
import com.android.jiaqiao.Service.SelectMusicService;
import com.android.jiaqiao.Utils.SharedUtile;
import com.android.jiaqiao.View.ToggleButton.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jiaqiao on 2017/9/4/0004.
 */

public class SetActivity extends AppCompatActivity {


    private ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();

    private ImageView close_set_activity;
    private Spinner music_all_spinner, music_sheet_spinner, music_love_spinner, music_floder_spinner, music_timing_type;
    private ToggleButton toggle_button_date_time, toggle_button_floder, toggle_button_floder_time, toggle_button_floder_size, toggle_button_floder_name, toggle_button_auto_timing_time, toggle_button_headset_start_play, toggle_button_headset_stop_play, toggle_button_close_app;
    private LinearLayout toggle_button_floder_time_linear, toggle_button_floder_size_linear, floder_spinner_layout;
    private SeekBar toggle_button_floder_time_seek_bar, toggle_button_floder_size_seek_bar, toggle_button_timing_type_seek_bar;
    private TextView time_seek_bar_text, size_seek_bar_text, timing_type_min_text, timing_type_max_text;
    private ListView floder_name_list;
    private SetFolderListViewAdapter adapter;
    private ScrollView all_scroll_view;

    private TextView auto_timing_text_001, auto_timing_text_002, auto_timing_text_003, auto_timing_text_004;
    private LinearLayout toggle_button_timing_type_linear;
    private ToggleButton toggle_button_play_over_auto, toggle_button_close_screen_auto, toggle_button_in_headset_auto;

    private String sd_path = Environment.getExternalStorageDirectory().getPath();
    private String[] music_spinner_item_str = new String[]{"歌名", "添加日期(升序)", "添加日期(降序)"};
    private String[] music_timing_type_str = new String[]{"定时", "定曲"};
    private boolean show_date_time, show_floder, floder_name, headset_start_play, headset_stop_play,close_app;
    private int sheet_num = 0, music_all_num = 0, floder_time_num = 0, floder_size_num = 0, floder_num = 0, love_num = 0;
    private String floder_name_str = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 123456) {
                int offset = all_scroll_view.getHeight();
                if (offset < 0) {
                    offset = 0;
                }
                all_scroll_view.scrollTo(0, offset);
            } else if (msg.what == 0x12345678) {
                all_scroll_view.scrollTo(0, 0);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //使用代码设置状态栏透明，使用xml，在部分rom上会失效
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }


        close_set_activity=(ImageView) findViewById(R.id.close_set_activity);
        music_all_spinner = (Spinner) findViewById(R.id.music_all_spinner);
        music_sheet_spinner = (Spinner) findViewById(R.id.music_sheet_spinner);
        music_love_spinner = (Spinner) findViewById(R.id.music_love_spinner);
        music_floder_spinner = (Spinner) findViewById(R.id.music_floder_spinner);

        music_timing_type = (Spinner) findViewById(R.id.music_timing_type);
        toggle_button_floder = (ToggleButton) findViewById(R.id.toggle_button_floder);
        toggle_button_date_time = (ToggleButton) findViewById(R.id.toggle_button_date_time);
        toggle_button_floder_time = (ToggleButton) findViewById(R.id.toggle_button_floder_time);
        toggle_button_floder_size = (ToggleButton) findViewById(R.id.toggle_button_floder_size);
        toggle_button_floder_name = (ToggleButton) findViewById(R.id.toggle_button_floder_name);
        toggle_button_auto_timing_time = (ToggleButton) findViewById(R.id.toggle_button_auto_timing_time);
        toggle_button_headset_start_play = (ToggleButton) findViewById(R.id.toggle_button_headset_start_play);
        toggle_button_headset_stop_play = (ToggleButton) findViewById(R.id.toggle_button_hedaset_stop_play);
        toggle_button_close_app = (ToggleButton) findViewById(R.id.toggle_button_close_app);

        toggle_button_floder_time_linear = (LinearLayout) findViewById(R.id.toggle_button_floder_time_linear);
        toggle_button_floder_size_linear = (LinearLayout) findViewById(R.id.toggle_button_floder_size_linear);
        floder_spinner_layout = (LinearLayout) findViewById(R.id.floder_spinner_layout);
        toggle_button_floder_time_seek_bar = (SeekBar) findViewById(R.id.toggle_button_floder_time_seek_bar);
        toggle_button_floder_size_seek_bar = (SeekBar) findViewById(R.id.toggle_button_floder_size_seek_bar);
        toggle_button_timing_type_seek_bar = (SeekBar) findViewById(R.id.toggle_button_timing_type_seek_bar);
        time_seek_bar_text = (TextView) findViewById(R.id.time_seek_bar_text);
        size_seek_bar_text = (TextView) findViewById(R.id.size_seek_bar_text);
        timing_type_min_text = (TextView) findViewById(R.id.timing_type_min_text);
        timing_type_max_text = (TextView) findViewById(R.id.timing_type_max_text);
        floder_name_list = (ListView) findViewById(R.id.floder_name_list);
        all_scroll_view = (ScrollView) findViewById(R.id.all_scroll_view);

        auto_timing_text_001 = (TextView) findViewById(R.id.auto_timing_text_001);
        auto_timing_text_002 = (TextView) findViewById(R.id.auto_timing_text_002);
        auto_timing_text_003 = (TextView) findViewById(R.id.auto_timing_text_003);
        auto_timing_text_004 = (TextView) findViewById(R.id.auto_timing_text_004);
        toggle_button_timing_type_linear = (LinearLayout) findViewById(R.id.toggle_button_timing_type_linear);
        toggle_button_play_over_auto = (ToggleButton) findViewById(R.id.toggle_button_play_over_auto);
        toggle_button_close_screen_auto = (ToggleButton) findViewById(R.id.toggle_button_close_screen_auto);
        toggle_button_in_headset_auto = (ToggleButton) findViewById(R.id.toggle_button_in_headset_auto);


        show_date_time = SharedUtile.getSharedBoolean(this, "is_show_date_time", false);
        show_floder = SharedUtile.getSharedBoolean(this, "is_show_floder", false);
        music_all_num = SharedUtile.getSharedInt(this, "music_all_num", 0);
        sheet_num = SharedUtile.getSharedInt(this, "sheet_num", 1);
        love_num = SharedUtile.getSharedInt(this, "love_num", 1);
        floder_num = SharedUtile.getSharedInt(this, "floder_num", 0);
        floder_time_num = SharedUtile.getSharedInt(this, "floder_time_num", 15);
        floder_size_num = SharedUtile.getSharedInt(this, "floder_size_num", 0);
        floder_name_str = SharedUtile.getSharedString(this, "floder_name_str", "");
        floder_name = SharedUtile.getSharedBoolean(this, "floder_name", true);
        headset_start_play = SharedUtile.getSharedBoolean(this, "headset_start_play", false);
        headset_stop_play = SharedUtile.getSharedBoolean(this, "headset_stop_play", true);
        close_app = SharedUtile.getSharedBoolean(this, "close_app", true);

        list_folder_all = PublicDate.list_folder_all;
        if (list_folder_all.size() > 0) {
            upDtaeFloderNameList();
            adapter = new SetFolderListViewAdapter(this, list_folder_all);
            floder_name_list.setAdapter(adapter);
            floder_name_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (floder_name) {
                        String temp = SharedUtile.getSharedString(SetActivity.this, "floder_name_str", "");
                        if ((boolean) list_folder_all.get(position).get("is_click")) {
                            list_folder_all.get(position).put("is_click", false);
//                        String temp = SharedUtile.getSharedString(SetActivity.this, "floder_name_str", "");
                            temp = list_folder_all.get(position).get("folder_name_path").toString() + PublicDate.separate_str;

                        } else {
                            list_folder_all.get(position).put("is_click", true);

                            temp = deleteStrFromStr(temp, list_folder_all.get(position).get("folder_name_path").toString());
                        }
                        SharedUtile.putSharedString(SetActivity.this, "floder_name_str", temp);
                        adapter.updataView(position, floder_name_list);
                    }
                }
            });
            setListViewHeightBasedOnChildren(floder_name_list);
            setToggleButtonBoolean(toggle_button_floder_name, true);
        } else {
            toggle_button_floder_name.setToggle(false);
            setToggleButtonBoolean(toggle_button_floder_name, false);
        }


        setSpinnerItem(music_all_spinner, music_spinner_item_str);
        setSpinnerItem(music_sheet_spinner, music_spinner_item_str);
        setSpinnerItem(music_love_spinner, music_spinner_item_str);
        setSpinnerItem(music_floder_spinner, music_spinner_item_str);
        setAutoTimingSpinnerItem(music_timing_type, music_timing_type_str, true);

        //setting
        music_all_spinner.setSelection(music_all_num);
        music_sheet_spinner.setSelection(sheet_num);
        music_love_spinner.setSelection(love_num);
        music_floder_spinner.setSelection(floder_num);
        music_timing_type.setSelection(1);

//        toggle_button_floder_time.toggleOff();
//        toggle_button_floder_size.toggleOn();
//        toggle_button_floder_name.toggleOff();
//        toggle_button_auto_timing_time.toggleOff();

        if (floder_time_num > 0) {
            toggle_button_floder_time.setToggle(true);
        } else {
            toggle_button_floder_time.setToggle(false);
        }
        if (floder_size_num > 0) {
            toggle_button_floder_size.setToggle(true);
        } else {
            toggle_button_floder_size.setToggle(false);
        }

        toggle_button_floder_name.setToggle(floder_name);
        toggle_button_auto_timing_time.setToggle(false);
        toggle_button_date_time.setToggle(show_date_time);
        toggle_button_floder.setToggle(show_floder);
        toggle_button_headset_start_play.setToggle(headset_start_play);
        toggle_button_headset_stop_play.setToggle(headset_stop_play);
        toggle_button_close_app.setToggle(close_app);

        if (show_floder) {
            floder_spinner_layout.setVisibility(View.VISIBLE);
        } else {
            floder_spinner_layout.setVisibility(View.GONE);
        }

        upDateFloderUiTime(toggle_button_floder_time.getToggle());
        upDateFloderUiSize(toggle_button_floder_size.getToggle());
        upDateFloderUiName(toggle_button_floder_name.getToggle());
        upDateAutoTimingUi(toggle_button_auto_timing_time.getToggle());
        upDateTimingTypeUi();

        toggle_button_date_time.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    SharedUtile.putSharedBoolean(SetActivity.this, "is_show_date_time", true);
                } else {
                    SharedUtile.putSharedBoolean(SetActivity.this, "is_show_date_time", false);
                }
            }
        });

        toggle_button_floder.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    SharedUtile.putSharedBoolean(SetActivity.this, "is_show_floder", true);
                    floder_spinner_layout.setVisibility(View.VISIBLE);
                } else {
                    SharedUtile.putSharedBoolean(SetActivity.this, "is_show_floder", false);
                    floder_spinner_layout.setVisibility(View.GONE);
                }
            }
        });

        toggle_button_headset_start_play.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    SharedUtile.putSharedBoolean(SetActivity.this, "headset_start_play", true);
                } else {
                    SharedUtile.putSharedBoolean(SetActivity.this, "headset_start_play", false);
                }
            }
        });
        toggle_button_headset_stop_play.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    SharedUtile.putSharedBoolean(SetActivity.this, "headset_stop_play", true);
                } else {
                    SharedUtile.putSharedBoolean(SetActivity.this, "headset_stop_play", false);
                }
            }
        });
        toggle_button_close_app.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                if (on) {
                    SharedUtile.putSharedBoolean(SetActivity.this, "close_app", true);
                } else {
                    SharedUtile.putSharedBoolean(SetActivity.this, "close_app", false);
                }
            }
        });

        music_all_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedUtile.putSharedInt(SetActivity.this, "music_all_num", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        music_sheet_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedUtile.putSharedInt(SetActivity.this, "sheet_num", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        music_love_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedUtile.putSharedInt(SetActivity.this, "love_num", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        music_floder_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedUtile.putSharedInt(SetActivity.this, "floder_num", position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        toggle_button_floder_time.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                upDateFloderUiTime(on);
            }
        });

        toggle_button_floder_size.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                upDateFloderUiSize(on);
            }
        });

        toggle_button_floder_name.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                upDateFloderUiName(on);
            }
        });

        toggle_button_auto_timing_time.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                upDateAutoTimingUi(on);
            }
        });

        music_timing_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                upDateTimingTypeUi();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        toggle_button_floder_time_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                time_seek_bar_text.setText(getIntToTime(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedUtile.putSharedInt(SetActivity.this, "floder_time_num", seekBar.getProgress());
            }
        });

        toggle_button_floder_size_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                size_seek_bar_text.setText(getIntToMB(seekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedUtile.putSharedInt(SetActivity.this, "floder_size_num", seekBar.getProgress());
            }
        });

        close_set_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toggle_button_timing_type_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (music_timing_type.getSelectedItemPosition() == 0) {
                    timing_type_min_text.setText(seekBar.getProgress() + "分钟");
                } else {
                    timing_type_min_text.setText(seekBar.getProgress() + "首");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        all_scroll_view.fullScroll(ScrollView.FOCUS_UP);
        handler.sendEmptyMessage(0x12345678);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (toggle_button_date_time.getToggle() != show_date_time || toggle_button_floder.getToggle() != show_floder) {
            Intent temp_intent = new Intent();
            temp_intent.setAction("com.android.jiaqiao");
            temp_intent.putExtra("type", MainActivity.UPDATE_FRAGMENT_MAIN_SET);
            sendBroadcast(temp_intent);
        }
        if (music_all_num != music_all_spinner.getSelectedItemPosition()) {
            Intent temp_intent = new Intent();
            temp_intent.setAction("com.android.jiaqiao");
            temp_intent.putExtra("type", MainActivity.UPDATE_FRAGMENT_MUSIC_ALL_SET);
            sendBroadcast(temp_intent);
        }
        if (sheet_num != music_sheet_spinner.getSelectedItemPosition()) {
            Intent temp_intent = new Intent();
            temp_intent.setAction("com.android.jiaqiao");
            temp_intent.putExtra("type", MainActivity.UPDATE_FRAGMENT_MUSIC_SHEET_SET);
            sendBroadcast(temp_intent);
        }
        if (love_num != music_love_spinner.getSelectedItemPosition()) {
            Intent temp_intent = new Intent();
            temp_intent.setAction("com.android.jiaqiao");
            temp_intent.putExtra("type", MainActivity.UPDATE_FRAGMENT_MUSIC_LOVE_SET);
            sendBroadcast(temp_intent);
        }
        if (floder_num != music_floder_spinner.getSelectedItemPosition()) {
            Intent temp_intent = new Intent();
            temp_intent.setAction("com.android.jiaqiao");
            temp_intent.putExtra("type", MainActivity.UPDATE_FRAGMENT_MUSIC_FLODER_SET);
            sendBroadcast(temp_intent);
        }
        if (floder_time_num != SharedUtile.getSharedInt(this, "floder_time_num", 15) || floder_size_num != SharedUtile.getSharedInt(this, "floder_size_num", 0) || floder_name_str != SharedUtile.getSharedString(this, "floder_name_str", "") || floder_name != SharedUtile.getSharedBoolean(this, "floder_name", true)) {
            Intent select_music_intent = new Intent(SetActivity.this, SelectMusicService.class);
            startService(select_music_intent);
        }
    }

    public void setSpinnerItem(Spinner spinner, String[] spinner_item) {
        if (spinner_item.length > 0) {
            ArrayAdapter<String> music_all_spinner_adapter = new ArrayAdapter<String>(this, R.layout.spinner_text_view, spinner_item);
            music_all_spinner_adapter.setDropDownViewResource(R.layout.spinner_text_view_002);
            spinner.setAdapter(music_all_spinner_adapter);
        }
    }

    public void setAutoTimingSpinnerItem(Spinner spinner, String[] spinner_item, boolean is) {
        if (spinner_item.length > 0) {
            if (is) {
                ArrayAdapter<String> music_all_spinner_adapter = new ArrayAdapter<String>(this, R.layout.spinner_text_view, spinner_item);
                music_all_spinner_adapter.setDropDownViewResource(R.layout.spinner_text_view_002);
                spinner.setAdapter(music_all_spinner_adapter);
            } else {
                ArrayAdapter<String> music_all_spinner_adapter = new ArrayAdapter<String>(this, R.layout.spinner_text_view_001, spinner_item);
                music_all_spinner_adapter.setDropDownViewResource(R.layout.spinner_text_view_002);
                spinner.setAdapter(music_all_spinner_adapter);
            }
        }
    }

    public void upDateFloderUiTime(boolean is_toggle) {
        if (is_toggle) {
            toggle_button_floder_time_linear.setVisibility(View.VISIBLE);
            if (floder_time_num <= 0) {
                SharedUtile.putSharedInt(SetActivity.this, "floder_time_num", 60);
                toggle_button_floder_time_seek_bar.setProgress(60);
            } else {
                toggle_button_floder_time_seek_bar.setProgress(floder_time_num);
            }
            time_seek_bar_text.setText(getIntToTime(toggle_button_floder_time_seek_bar.getProgress()));
        } else {
            SharedUtile.putSharedInt(SetActivity.this, "floder_time_num", 0);
            toggle_button_floder_time_linear.setVisibility(View.GONE);
        }
    }

    public void upDateFloderUiSize(boolean is_toggle) {
        if (is_toggle) {
            toggle_button_floder_size_linear.setVisibility(View.VISIBLE);
            if (SharedUtile.getSharedInt(SetActivity.this, "floder_size_num", 0) <= 0) {
                SharedUtile.putSharedInt(SetActivity.this, "floder_size_num", 1000);
                toggle_button_floder_size_seek_bar.setProgress(1000);
            } else {
                toggle_button_floder_size_seek_bar.setProgress(SharedUtile.getSharedInt(SetActivity.this, "floder_size_num", 0));
            }
            size_seek_bar_text.setText(getIntToMB(toggle_button_floder_size_seek_bar.getProgress()));
        } else {
            SharedUtile.putSharedInt(SetActivity.this, "floder_size_num", 0);
            toggle_button_floder_size_linear.setVisibility(View.GONE);
        }
    }

    public void upDateFloderUiName(boolean is_toggle) {
        if (is_toggle) {
            floder_name_list.setVisibility(View.VISIBLE);
        } else {
            floder_name_list.setVisibility(View.GONE);
            SharedUtile.putSharedBoolean(this, "floder_name", false);
        }
    }

    public void upDateAutoTimingUi(boolean is_toggle) {
        if (is_toggle) {
            auto_timing_text_001.setTextColor(getResources().getColor(R.color.font_seccend_color));
            auto_timing_text_002.setTextColor(getResources().getColor(R.color.font_seccend_color));
            auto_timing_text_003.setTextColor(getResources().getColor(R.color.font_seccend_color));
            auto_timing_text_004.setTextColor(getResources().getColor(R.color.font_seccend_color));
            toggle_button_timing_type_linear.setVisibility(View.VISIBLE);
            music_timing_type.setEnabled(true);
            setAutoTimingSpinnerItem(music_timing_type, music_timing_type_str, true);

            setToggleButtonBoolean(toggle_button_play_over_auto, true);
            setToggleButtonBoolean(toggle_button_close_screen_auto, true);
            setToggleButtonBoolean(toggle_button_in_headset_auto, true);
            toggle_button_play_over_auto.toggleOn();
            toggle_button_close_screen_auto.toggleOn();
            toggle_button_in_headset_auto.toggleOn();
            handler.sendEmptyMessage(123456);
        } else {
            auto_timing_text_001.setTextColor(getResources().getColor(R.color.fengexian_color));
            auto_timing_text_002.setTextColor(getResources().getColor(R.color.fengexian_color));
            auto_timing_text_003.setTextColor(getResources().getColor(R.color.fengexian_color));
            auto_timing_text_004.setTextColor(getResources().getColor(R.color.fengexian_color));
            toggle_button_timing_type_linear.setVisibility(View.GONE);
            music_timing_type.setEnabled(false);
            setAutoTimingSpinnerItem(music_timing_type, music_timing_type_str, false);

            setToggleButtonBoolean(toggle_button_play_over_auto, false);
            setToggleButtonBoolean(toggle_button_close_screen_auto, false);
            setToggleButtonBoolean(toggle_button_in_headset_auto, false);
            toggle_button_play_over_auto.toggleOff();
            toggle_button_close_screen_auto.toggleOff();
            toggle_button_in_headset_auto.toggleOff();
        }
    }

    public void upDateTimingTypeUi() {
        //music_timing_type.getSelectedItemPosition()，获取选中的子项
        if (music_timing_type.getSelectedItemPosition() == 0) {
            timing_type_min_text.setText("0分钟");
            timing_type_max_text.setText("90分钟");
            toggle_button_timing_type_seek_bar.setProgress(0);
            toggle_button_timing_type_seek_bar.setMax(90);
        } else {
            timing_type_min_text.setText("0首");
            timing_type_max_text.setText("15首");
            toggle_button_timing_type_seek_bar.setProgress(0);
            toggle_button_timing_type_seek_bar.setMax(15);
        }
    }

    public void setToggleButtonBoolean(ToggleButton toggle_button, boolean is_not) {
        //设置ToggleButton是否可以点击以及事件的有效性
        final boolean temp = is_not ? false : true;
        toggle_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return temp;
            }
        });
    }

    public String getIntToTime(int num) {
        if (num > 0) {
            String num01 = new String().format("%02d", num / 60);
            String num02 = new String().format("%02d", num % 60);
            return num01 + ":" + num02;
        }
        return "00:00";
    }

    public String getIntToMB(int num) {
        if (num > 0) {
            String num01 = new String().format("%01d", num / 1000);
            String num02 = new String().format("%02d", num % 1000 / 10);
            return num01 + "." + num02 + "MB";
        }
        return "0.00MB";
    }

    public String getInfoString(String string) {
        if (string.equals(sd_path)) {
            return "根目录";
        } else if (string.toLowerCase().indexOf("12530") > -1) {
            return "咪咕音乐";
        } else if (string.toLowerCase().indexOf("music") > -1 && string.toLowerCase().indexOf("baidu") > -1) {
            return "百度音乐";
        } else if (string.toLowerCase().indexOf("kgmusic") > -1) {
            return "酷狗音乐";
        } else if (string.toLowerCase().indexOf("kuwomusic") > -1) {
            return "酷我音乐";
        } else if (string.toLowerCase().indexOf("cloudmusic") > -1) {
            return "网易云音乐";
        } else if (string.toLowerCase().indexOf("qqmusic") > -1) {
            return "QQ音乐";
        } else if (string.toLowerCase().indexOf("xiami") > -1) {
            return "虾米音乐";
        } else {
            return string.substring(string.lastIndexOf("/") + "/".length());
        }

    }

    public void upDtaeFloderNameList() {
//        if (list_folder_all.size() > 0 && floder_name_str.length() > 0) {
//            for (int i = 0; i < list_folder_all.size(); i++) {
//                if (floder_name_str.indexOf((list_folder_all.get(i).get("folder_name_path")+PublicDate.separate_str)) > -1) {
//                    list_folder_all.get(i).put("is_click", false);
//                } else {
//                    list_folder_all.get(i).put("is_click", true);
//                }
//            }
//        }
        String name_temp = floder_name_str;
        while (true) {
            if (name_temp.length() <= PublicDate.separate_str.length()) {
                break;
            }
            String temp = name_temp.substring(0, name_temp.indexOf("#"));
            HashMap<String, Object> folder_map = new HashMap<>();
            folder_map.put("folder_name", getInfoString(temp));
            folder_map.put("folder_name_list", null);
            folder_map.put("folder_name_path", temp);
            folder_map.put("is_click", false);
            list_folder_all.add(folder_map);
            name_temp = name_temp.substring(name_temp.indexOf("#") + "#".length());
        }


    }

    public String deleteStrFromStr(String a, String b) {
        String str01 = a.substring(0, a.indexOf(b));
        String str02 = a.substring(a.indexOf(b) + b.length() + PublicDate.separate_str.length());
        return str01 + str02;
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        /*
        * 代码设置ListView的高度，使ListView的高度固定，可以在ScrollView中完全显示，而不是显示ListView中一个Item
        *
        * */
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
//        listView.setAdapter(listAdapter);

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

}
