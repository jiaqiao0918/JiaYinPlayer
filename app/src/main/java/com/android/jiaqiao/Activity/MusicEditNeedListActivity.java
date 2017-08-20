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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Adapter.MusicSheetListViewAdapter;
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

import static com.android.jiaqiao.jiayinplayer.PublicDate.music_edit_temp;
import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/7/3/0003.
 */

public class MusicEditNeedListActivity extends Activity {

    private Button selected_all_button;
    private ListView show_all_music_list;
    private ImageView activity_muisc_edit_add, activity_muisc_edit_love, activity_muisc_edit_delete;
    private TextView edit_name;
    private LinearLayout not_love_music;

    private ArrayList<MusicInfo> music_temp = new ArrayList<MusicInfo>();
    private MusicSheetListViewAdapter adapter;

    private int selectec_num = 0;
    private String path = "";
    private String music_sheet_id01="";

    private boolean is_all_music_01 = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_edit_need_list);
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

        is_all_music_01 = getIntent().getBooleanExtra("is_all_music_01",false);
        music_sheet_id01 = getIntent().getStringExtra("music_sheet_id_01");
        music_temp = music_edit_temp;
        for (int i = 0; i < music_temp.size(); i++) {
            music_temp.get(i).setIs_selected(false);
        }
        show_all_music_list = (ListView) findViewById(R.id.show_all_music_list);
        selected_all_button = (Button) findViewById(R.id.selected_all_button);
        activity_muisc_edit_add = (ImageView) findViewById(R.id.activity_muisc_edit_add);
        activity_muisc_edit_love = (ImageView) findViewById(R.id.activity_muisc_edit_love);
        activity_muisc_edit_delete = (ImageView) findViewById(R.id.activity_muisc_edit_delete);
        edit_name = (TextView) findViewById(R.id.edit_name);
        not_love_music = (LinearLayout) findViewById(R.id.not_love_music);


        if(music_sheet_id01.trim().equals("love".trim())){
            not_love_music.setVisibility(View.GONE);
        }
        activity_muisc_edit_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                for (int i = 0; i < music_temp.size(); i++) {
                    if (music_temp.get(i).is_selected()) {
                        music_edit_select.add(i);
                    }
                }
                if (music_edit_select.size() > 0 && music_edit_select != null) {
                    PublicDate.music_edit_temp_select = music_edit_select;
                    startActivity(new Intent(MusicEditNeedListActivity.this, MusicEditAddMusicSheetActivity.class));
                    finish();
                }
            }
        });
        activity_muisc_edit_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                for (int i = 0; i < music_temp.size(); i++) {
                    if (music_temp.get(i).is_selected()) {
                        music_edit_select.add(i);
                    }
                }
                if (music_edit_select.size() > 0) {
                    ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
                    music_edit_temp = PublicDate.music_edit_temp;
                    path = getPath("love");
                    int num = 0;
                    for (int i = 0; i < music_edit_select.size(); i++) {
                        String add_context = music_edit_temp.get(music_edit_select.get(i)).getMusic_id() + separate_str + music_edit_temp.get(music_edit_select.get(i)).getMusic_title();
                        if (!isHavaContextFromPath(path, add_context)) {
                            addTextToFile(path, add_context);
                            num++;
                        }
                    }
                    if (num > 0) {
                        Toast.makeText(MusicEditNeedListActivity.this, num + "首歌曲收藏成功！！", Toast.LENGTH_SHORT).show();
                    }
                }
                music_edit_temp = null;
                PublicDate.music_edit_temp_select = null;
                finish();
            }
        });
        activity_muisc_edit_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                for (int i = 0; i < music_temp.size(); i++) {
                    if (music_temp.get(i).is_selected()) {
                        music_edit_select.add(i);
                    }
                }
                if (music_edit_select.size() > 0 && music_edit_select != null) {
                    PublicDate.music_edit_temp_select = music_edit_select;
                    startActivity(new Intent(MusicEditNeedListActivity.this, MusicEditDeleteActivity.class).putExtra("is_all_music",is_all_music_01).putExtra("music_sheet_id",music_sheet_id01));
                    finish();
                }
            }
        });

        edit_name.setText(getIntent().getStringExtra("edit_name_intent"));

        adapter = new MusicSheetListViewAdapter(this, music_temp);
        show_all_music_list.setAdapter(adapter);
        show_all_music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (music_temp.get(position).is_selected()) {
                    music_temp.get(position).setIs_selected(false);
                    selectec_num--;
                    if (selectec_num < 0) {
                        selectec_num = 0;
                    }
                } else {
                    music_temp.get(position).setIs_selected(true);
                    selectec_num++;
                    if (selectec_num > music_temp.size()) {
                        selectec_num = music_temp.size();
                    }
                }
                adapter.updataView(position, show_all_music_list);
                if (selectec_num >= music_temp.size()) {
                    selected_all_button.setText("全不选");
                } else {
                    selected_all_button.setText("全选");
                }
            }
        });


    }

    public void cancel_button(View view) {
        this.finish();
    }

    public void selected_all(View view) {
        if (selectec_num != music_temp.size()) {
            for (int i = 0; i < music_temp.size(); i++) {
                music_temp.get(i).setIs_selected(true);
                adapter.updataView(i, show_all_music_list);
            }
            selectec_num = music_temp.size();
            selected_all_button.setText("全不选");
        } else {
            for (int i = 0; i < music_temp.size(); i++) {
                music_temp.get(i).setIs_selected(false);
                adapter.updataView(i, show_all_music_list);
            }
            selectec_num = 0;
            selected_all_button.setText("全选");
        }
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
