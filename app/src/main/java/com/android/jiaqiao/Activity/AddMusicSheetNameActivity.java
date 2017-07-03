package com.android.jiaqiao.Activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by jiaqiao on 2017/7/2/0002.
 */

public class AddMusicSheetNameActivity extends Activity {
    private EditText music_sheet_name;
    private ArrayList<String> music_sheet_name_list = new ArrayList<>();
    private String path = null;
    private String edit_text_show_str = "";
    private String music_sheet_name_all = "";

    private InputFilter filter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//返回null表示接收输入的字符,返回空字符串表示不接受输入的字符
            if (source.equals(" ") || source.toString().contentEquals("\n")) {//输入空格和回车键无效
                return "";
            } else {
                return null;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_sheet_name);
        this.overridePendingTransition(R.anim.dialog_enter_anim, 0);//设置ActivityToDialog的进入动画
        // 获取对话框的Window对象
        Window mWindow = this.getWindow();
        mWindow.setGravity(Gravity.BOTTOM);//设置Dialog在底部
        //activity to diaolog充满整个屏幕的宽度
        Display display = mWindow.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        WindowManager.LayoutParams lp = mWindow.getAttributes();
        lp.width = size.x; //设置dialog的宽度为当前手机屏幕的宽度,getwidth() 过时
        getWindow().setAttributes(lp);

        music_sheet_name = (EditText) findViewById(R.id.msuic_sheet_name);
        File file = new File(this.getFilesDir() + "/music_sheet");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file.getPath() + "/music_sheet_name.txt");
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        path = file2.getPath().toString();
        music_sheet_name_all = getTextToString(path);
        int while_num = 0;
        while (true) {
            while_num++;
            edit_text_show_str = "新建歌单" + while_num;
            String temp = edit_text_show_str + PublicDate.separate_str;
            if (music_sheet_name_all.indexOf(temp) <= -1) {
                break;
            }
        }
        music_sheet_name.setText(edit_text_show_str);
        music_sheet_name.setInputType(InputType.TYPE_CLASS_TEXT);
        music_sheet_name.setFilters(new InputFilter[]{filter});
        music_sheet_name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});//设置文本最大长度，16个中文或者16个字母
    }

    public void cancel_button(View view) {
        this.finish();
    }

    public void determine_button(View view) {
        String music_sheet_name_temp = music_sheet_name.getText().toString();
        if (music_sheet_name_temp.trim().length() > 0) {

            if (music_sheet_name_all.indexOf(music_sheet_name_temp) <= -1) {
                String now_time = getNowTime();
                addTextToFile(path, now_time + PublicDate.separate_str + music_sheet_name_temp);
                PublicDate.temp_sheet_info = new SheetInfo(now_time, music_sheet_name_temp);
                PublicDate.is_update_music_sheet_list = true;
                this.finish();
            } else {
                Toast.makeText(this, "歌单名重复，请修改！！", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "歌单名不能为空！！", Toast.LENGTH_SHORT).show();
        }


    }

    public String getTextToString(String path_name) {
        String now = "";
        try {
            FileReader fr = new FileReader(path_name);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (temp.length() > 0 && temp.indexOf(PublicDate.separate_str) > -1) {
                    String[] name = temp.split(PublicDate.separate_str);
                    if (name.length >= 2) {
                        now += name[1] + PublicDate.separate_str;
                    }
                }
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return now;
    }

    public void addTextToFile(String path_name, String context) {
        try {
            File f = new File(path_name);
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileWriter fw = new FileWriter(f, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(context);
            pw.flush();
            fw.flush();
            pw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        showSoftInputFromWindow(this, music_sheet_name);
    }

    public static void showSoftInputFromWindow(Activity activity, EditText editText) {
        //设置EditText获取焦点，并弹出软键盘
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.selectAll();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    @Override
    public void finish() {
        super.finish();

        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }

    public String getNowTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");// 设置日期格式
        return df.format(new Date());
    }
}
