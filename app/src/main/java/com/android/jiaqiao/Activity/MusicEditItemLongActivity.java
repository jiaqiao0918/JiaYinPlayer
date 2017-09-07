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
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.JavaBean.MusicInfo;
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

public class MusicEditItemLongActivity extends Activity {
    private TextView item_long_music_title, is_all_music_delete;
    private LinearLayout activity_item_long_add, activity_item_long_love, activity_item_long_delete;

    private ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
    private ArrayList<Integer> music_edit_temp_select = new ArrayList<Integer>();

    private String path = "";
    private String music_sheet_id01 = "";
    private boolean is_all_music_01 = false;
    private boolean is_music_play_list = false;
    private boolean is_main_play_01=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_edit_item_long);
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


        is_all_music_01 = getIntent().getBooleanExtra("is_all_music_01", false);
        music_sheet_id01 = getIntent().getStringExtra("music_sheet_id_01");
        is_music_play_list = getIntent().getBooleanExtra("is_music_play_list01",false);
        is_main_play_01 = getIntent().getBooleanExtra("is_main_play",false);
        activity_item_long_add = (LinearLayout) findViewById(R.id.activity_item_long_add);
        activity_item_long_love = (LinearLayout) findViewById(R.id.activity_item_long_love);
        activity_item_long_delete = (LinearLayout) findViewById(R.id.activity_item_long_delete);
        item_long_music_title = (TextView) findViewById(R.id.item_long_music_title);
        is_all_music_delete = (TextView) findViewById(R.id.is_all_music_delete);
        music_edit_temp = PublicDate.public_music_edit_temp;
        music_edit_temp_select = PublicDate.public_music_edit_temp_select;
        if (music_edit_temp.size() > 0 && music_edit_temp != null) {
            if (music_edit_temp_select.size() > 0 && music_edit_temp_select != null) {
                item_long_music_title.setText(music_edit_temp.get(music_edit_temp_select.get(0)).getMusic_title().toString());
            }
        }

        if (is_all_music_01) {
            is_all_music_delete.setText("删除");
        }

        if (music_sheet_id01!=null) {
            if (music_sheet_id01.trim().equals("love".trim())) {
                activity_item_long_love.setVisibility(View.GONE);
                View is_love = (View) findViewById(R.id.is_love);
                is_love.setVisibility(View.GONE);
            }
        }

        activity_item_long_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (music_edit_temp_select.size() > 0 && music_edit_temp_select != null) {
                    startActivity(new Intent(MusicEditItemLongActivity.this, MusicEditAddMusicSheetActivity.class).putExtra("is_music_play_list02",is_music_play_list).putExtra("is_main_play_02",is_main_play_01));
                    finish();
                }
            }
        });
        activity_item_long_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (music_edit_temp_select.size() > 0) {
                    ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
                    music_edit_temp = PublicDate.public_music_edit_temp;
                    path = getPath("love");
                    int num = 0;
                    for (int i = 0; i < music_edit_temp_select.size(); i++) {
                        String add_context = music_edit_temp.get(music_edit_temp_select.get(i)).getMusic_id() + separate_str + music_edit_temp.get(music_edit_temp_select.get(i)).getMusic_title();
                        if (!isHavaContextFromPath(path, add_context)) {
                            addTextToFile(path, add_context);
                            num++;
                        }
                    }
                    if (num > 0) {
                        Toast.makeText(MusicEditItemLongActivity.this, num + "首歌曲收藏成功！！", Toast.LENGTH_SHORT).show();
                    }
                }
                PublicDate.public_music_edit_temp = null;
                PublicDate.public_music_edit_temp_select = null;
                finish();
            }
        });
        activity_item_long_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (music_edit_temp_select.size() > 0 && music_edit_temp_select != null) {
                    startActivity(new Intent(MusicEditItemLongActivity.this, MusicEditDeleteActivity.class).putExtra("is_all_music", is_all_music_01).putExtra("is_music_play_list02",is_music_play_list).putExtra("music_sheet_id", music_sheet_id01));
                    finish();
                }
            }
        });

    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }

    public String getPath(String sheet_id) {
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

    public boolean isHavaContextFromPath(String path_name, String context) {
        try {
            FileReader fr = new FileReader(path_name);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (context.trim().equals(line.trim())) {
                    return true;
                }
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
