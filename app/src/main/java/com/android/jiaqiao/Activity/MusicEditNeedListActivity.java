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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.jiaqiao.Adapter.MusicSheetListViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/7/3/0003.
 */

public class MusicEditNeedListActivity extends Activity {

    private Button selected_all_button;
    private ListView show_all_music_list;
    private ImageView test_image_view;

    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    private MusicSheetListViewAdapter adapter;

    private int selectec_num = 0;
    private String path = "";

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

        music_all = PublicDate.music_all;
        for (int i =0;i<music_all.size();i++){
            music_all.get(i).setIs_selected(false);
        }
        show_all_music_list = (ListView) findViewById(R.id.show_all_music_list);
        selected_all_button = (Button) findViewById(R.id.selected_all_button);
        test_image_view =(ImageView) findViewById(R.id.test_image_view);
        test_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MusicEditNeedListActivity.this,"单击",Toast.LENGTH_SHORT).show();
            }
        });
        adapter = new MusicSheetListViewAdapter(this, music_all);
        show_all_music_list.setAdapter(adapter);
        show_all_music_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (music_all.get(position).is_selected()) {
                    music_all.get(position).setIs_selected(false);
                    selectec_num--;
                    if (selectec_num < 0) {
                        selectec_num = 0;
                    }
                } else {
                    music_all.get(position).setIs_selected(true);
                    selectec_num++;
                    if (selectec_num > music_all.size()) {
                        selectec_num = music_all.size();
                    }
                }
                adapter.updataView(position, show_all_music_list);
                if (selectec_num >= music_all.size()) {
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
        if (selectec_num != music_all.size()) {
            for (int i = 0; i < music_all.size(); i++) {
                music_all.get(i).setIs_selected(true);
                adapter.updataView(i, show_all_music_list);
            }
            selectec_num = music_all.size();
            selected_all_button.setText("全不选");
        } else {
            for (int i = 0; i < music_all.size(); i++) {
                music_all.get(i).setIs_selected(false);
                adapter.updataView(i, show_all_music_list);
            }
            selectec_num = 0;
            selected_all_button.setText("全选");
        }
    }

    public void selected_over(View view) {
        if (selectec_num > 0) {
            path = getPath(PublicDate.temp_sheet_info.getSheet_id());
            int num = 0;
            for (int i = 0; i < music_all.size(); i++) {
                if (music_all.get(i).is_selected()) {
                    num++;
                    addTextToFile(path, music_all.get(i).getMusic_id() + PublicDate.separate_str + music_all.get(i).getMusic_title());

                }
            }
            if (num > 0) {
                Toast.makeText(MusicEditNeedListActivity.this, num + "首歌添加成功！！", Toast.LENGTH_SHORT).show();
            }
        }


        this.finish();
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
}
