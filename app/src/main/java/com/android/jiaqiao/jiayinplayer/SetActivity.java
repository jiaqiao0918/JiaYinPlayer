package com.android.jiaqiao.jiayinplayer;

import android.os.Build;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.jiaqiao.Adapter.SetFolderListViewAdapter;
import com.android.jiaqiao.View.ToggleButton.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jiaqiao on 2017/9/4/0004.
 */

public class SetActivity extends AppCompatActivity {


    private ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();

    private Spinner music_all_spinner, music_sheet_spinner, music_timing_type;
    private ToggleButton toggle_button_floder_time, toggle_button_floder_size, toggle_button_floder_name, toggle_button_auto_timing_time;
    private LinearLayout toggle_button_floder_time_linear, toggle_button_floder_size_linear;
    private SeekBar toggle_button_floder_time_seek_bar, toggle_button_floder_size_seek_bar, toggle_button_timing_type_seek_bar;
    private TextView time_seek_bar_text, size_seek_bar_text, timing_type_min_text, timing_type_max_text;
    private ListView floder_name_list;
    private SetFolderListViewAdapter adapter;
    private ScrollView all_scroll_view;

    private TextView auto_timing_text_001, auto_timing_text_002, auto_timing_text_003, auto_timing_text_004;
    private LinearLayout toggle_button_timing_type_linear;
    private ToggleButton toggle_button_play_over_auto, toggle_button_close_screen_auto, toggle_button_in_headset_auto;


    private String[] music_spinner_item_str = new String[]{"歌名", "歌手", "添加日期"};
    private String[] music_timing_type_str = new String[]{"定时", "定曲"};

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
        music_all_spinner = (Spinner) findViewById(R.id.music_all_spinner);
        music_sheet_spinner = (Spinner) findViewById(R.id.music_sheet_spinner);
        music_timing_type = (Spinner) findViewById(R.id.music_timing_type);
        toggle_button_floder_time = (ToggleButton) findViewById(R.id.toggle_button_floder_time);
        toggle_button_floder_size = (ToggleButton) findViewById(R.id.toggle_button_floder_size);
        toggle_button_floder_name = (ToggleButton) findViewById(R.id.toggle_button_floder_name);
        toggle_button_auto_timing_time = (ToggleButton) findViewById(R.id.toggle_button_auto_timing_time);
        toggle_button_floder_time_linear = (LinearLayout) findViewById(R.id.toggle_button_floder_time_linear);
        toggle_button_floder_size_linear = (LinearLayout) findViewById(R.id.toggle_button_floder_size_linear);
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

        list_folder_all = PublicDate.list_folder_all;
        if (list_folder_all.size() > 0) {
            adapter = new SetFolderListViewAdapter(this, list_folder_all);
            floder_name_list.setAdapter(adapter);
            floder_name_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if ((boolean) list_folder_all.get(position).get("is_click")) {
                        list_folder_all.get(position).put("is_click", false);
                    } else {
                        list_folder_all.get(position).put("is_click", true);
                    }
                    adapter.updataView(position, floder_name_list);
                }
            });
            setListViewHeightBasedOnChildren(floder_name_list);
            setToggleButtonBoolean(toggle_button_floder_name, true);
        } else {
            setToggleButtonBoolean(toggle_button_floder_name, false);
        }


        setSpinnerItem(music_all_spinner, music_spinner_item_str);
        setSpinnerItem(music_sheet_spinner, music_spinner_item_str);
        setAutoTimingSpinnerItem(music_timing_type, music_timing_type_str, true);

        //setting
        music_all_spinner.setSelection(1);
        music_sheet_spinner.setSelection(2);
        music_timing_type.setSelection(1);

        toggle_button_floder_time.toggleOff();
        toggle_button_floder_size.toggleOn();
        toggle_button_floder_name.toggleOff();
        toggle_button_auto_timing_time.toggleOff();

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
                upDateTimginTypeUi();
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

        upDateFloderUiTime(toggle_button_floder_time.getToggle());
        upDateFloderUiSize(toggle_button_floder_size.getToggle());
        upDateFloderUiName(toggle_button_floder_name.getToggle());
        upDateAutoTimingUi(toggle_button_auto_timing_time.getToggle());
        upDateTimginTypeUi();
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
        } else {
            toggle_button_floder_time_linear.setVisibility(View.GONE);
        }
    }

    public void upDateFloderUiSize(boolean is_toggle) {
        if (is_toggle) {
            toggle_button_floder_size_linear.setVisibility(View.VISIBLE);
        } else {
            toggle_button_floder_size_linear.setVisibility(View.GONE);
        }
    }

    public void upDateFloderUiName(boolean is_toggle) {
        if (is_toggle) {
            if (PublicDate.list_folder_all.size() > 0 && list_folder_all.size() <= 0) {
                list_folder_all = PublicDate.list_folder_all;
                adapter = new SetFolderListViewAdapter(this, list_folder_all);
                floder_name_list.setAdapter(adapter);
                floder_name_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if ((boolean) list_folder_all.get(position).get("is_click")) {
                            list_folder_all.get(position).put("is_click", false);
                        } else {
                            list_folder_all.get(position).put("is_click", true);
                        }
                        adapter.updataView(position, floder_name_list);
                    }
                });
                setListViewHeightBasedOnChildren(floder_name_list);
                floder_name_list.setVisibility(View.VISIBLE);
            }
            if (list_folder_all.size() > 0) {
                floder_name_list.setVisibility(View.VISIBLE);
            } else {
                setToggleButtonBoolean(toggle_button_floder_name, false);
            }

        } else {
            floder_name_list.setVisibility(View.GONE);
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

    public void upDateTimginTypeUi() {
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
            String num01 = new String().format("%01d", num / 100);
            String num02 = new String().format("%02d", num % 100);
            return num01 + "." + num02 + "MB";
        }
        return "0.00MB";
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
