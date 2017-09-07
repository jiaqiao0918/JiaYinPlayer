package com.android.jiaqiao.jiayinplayer;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by jiaqiao on 2017/9/4/0004.
 */

public class SetActivity extends AppCompatActivity {

    private Spinner music_all_spinner,music_sheet_spinner;

    private String[] music_spinner_item_str = new String[]{"歌名", "歌手", "添加日期"};

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


        setSpinnerItem(music_all_spinner, music_spinner_item_str);
        setSpinnerItem(music_sheet_spinner, music_spinner_item_str);
        music_all_spinner.setSelection(1);
        music_sheet_spinner.setSelection(2);
    }

    public void setSpinnerItem(Spinner spinner,String[] spinner_item) {
        if (spinner_item.length > 0) {
            ArrayAdapter<String> music_all_spinner_adapter = new ArrayAdapter<String>(this, R.layout.spinner_text_view, spinner_item);
            music_all_spinner_adapter.setDropDownViewResource(R.layout.spinner_text_view_002);
            spinner.setAdapter(music_all_spinner_adapter);
        }
    }
}
