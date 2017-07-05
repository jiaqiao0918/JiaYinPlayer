package com.android.jiaqiao.Activity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/7/5/0005.
 */

public class DeleteMusicSheetActivity extends Activity {
    private String sheet_path = "";
    private String sheet_id_path = "";
    private String sheet_name = null;
    private String sheet_id = "";

    private TextView music_sheet_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_music_sheet);
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
        sheet_id = getIntent().getStringExtra("sheet_id");
        sheet_path = getSheetPath();
        sheet_id_path = getFromIdToPath(sheet_id);
        music_sheet_name = (TextView) findViewById(R.id.music_sheet_name);
        sheet_name = getFromIdToSheetName(sheet_id);
        if (sheet_name != null || sheet_name.length() > 0) {
            music_sheet_name.setText("确定删除 " + sheet_name + " 吗？");
        } else {
            this.finish();
        }
    }


    public String getFromIdToPath(String sheet_id) {
        File file = new File(this.getFilesDir() + "/music_sheet_list");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file.getPath() + "/" + sheet_id + ".txt");
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file2.getPath().toString();
    }

    public String getSheetPath() {
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
        return file2.getPath().toString();
    }

    public String getFromIdToSheetName(String sheet_id) {
        try {
            FileReader fr = new FileReader(sheet_path);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (temp.length() > 0 && temp.indexOf(sheet_id + separate_str) > -1) {
                    String[] music_temp = temp.split(separate_str);
                    if (music_temp.length >= 2) {
                        return music_temp[1];
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
        return null;
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }

    public void cancel_button(View view) {
        this.finish();
    }

    public void determine_button(View view) {
        deleteSelectStr(sheet_path, sheet_id);
        deleteTxtFile(sheet_id_path);
        PublicDate.delete_sheet_over=true;
        this.finish();
    }

    public void deleteTxtFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public void deleteSelectStr(String path, String select_str) {
        /*
         *
		 * 删除一行思路：将整个txt的内容保存到list中，再清空txt的内容，然后将list中的数据保存到txt中，保存时进行筛选过滤需要删除的某行
		 * */
        ArrayList<String> temp_list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.indexOf(PublicDate.separate_str) > -1) {
                    temp_list.add(line);
                }
            }
            br.close();
            fr.close();

            clearTxtAll(path);// 清空txt内容

            for (int i = 0; i < temp_list.size(); i++) {
                String temp_str = temp_list.get(i).toString();
                if (temp_str.indexOf(select_str + PublicDate.separate_str) <= -1) {
                    addTextToFile(path, temp_str);
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clearTxtAll(String path) {
        // 清空txt文件
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件,false表示覆盖的方式写入
            writer = new FileWriter(path, false);
            writer.write("");
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
