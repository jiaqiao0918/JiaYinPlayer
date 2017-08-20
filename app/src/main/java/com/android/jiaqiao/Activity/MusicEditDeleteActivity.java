package com.android.jiaqiao.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Service.SelectMusicService;
import com.android.jiaqiao.jiayinplayer.MainActivity;
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


/**
 * Created by jiaqiao on 2017/7/2/0002.
 */

public class MusicEditDeleteActivity extends Activity {



    private boolean is_all_music = false;
    private boolean is_delete_yuan = false;

    private LinearLayout delete_is_yuan;
    private ImageView is_selected;
    private TextView delete_title, delete_context;

    private String path = "";
    private String music_sheet_id = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_edit_delete_music);
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


        is_all_music = getIntent().getBooleanExtra("is_all_music", false);
        music_sheet_id = getIntent().getStringExtra("music_sheet_id");
        is_selected = (ImageView) findViewById(R.id.is_selected);
        delete_title = (TextView) findViewById(R.id.delete_title);
        delete_context = (TextView) findViewById(R.id.delete_context);
        delete_is_yuan = (LinearLayout) findViewById(R.id.delete_is_yuan);
        if (is_all_music) {
            delete_title.setText("删除");
            delete_context.setText("确定删除选中项吗？");
            delete_is_yuan.setVisibility(View.GONE);
        } else {
            delete_title.setText("移除");
            delete_context.setText("确定移除选中项吗？");
            delete_is_yuan.setVisibility(View.VISIBLE);
            delete_is_yuan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (is_delete_yuan) {
                        is_delete_yuan = false;
                    } else {
                        is_delete_yuan = true;
                    }
                    updateUi();
                }
            });
        }
    }

    public void cancel_button(View view) {
        this.finish();
    }

    public void determine_button(View view) {
        boolean is_delete_yuan_over=false;
        int num = 0;
        if (is_all_music) {
            ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
            ArrayList<Integer> music_edit_temp_select = new ArrayList<Integer>();
            music_edit_temp = PublicDate.music_edit_temp;
            music_edit_temp_select = PublicDate.music_edit_temp_select;
            for (int i = 0; i < music_edit_temp_select.size(); i++) {
                String need_delete_path = music_edit_temp.get(music_edit_temp_select.get(i)).getMusic_path();
                deleteFile(need_delete_path);
                is_delete_yuan_over=true;
                num++;
            }
        } else {
            ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
            ArrayList<Integer> music_edit_temp_select = new ArrayList<Integer>();
            music_edit_temp = PublicDate.music_edit_temp;
            music_edit_temp_select = PublicDate.music_edit_temp_select;
            for (int i = 0; i < music_edit_temp_select.size(); i++) {
                String music_id = music_edit_temp.get(music_edit_temp_select.get(i)).getMusic_id()+"";
                path = getPath(music_sheet_id);
                deleteSelectStr(path, music_id);
                num++;
                Log.i("into",is_delete_yuan+"");
                if (is_delete_yuan) {
                    String need_delete_path = music_edit_temp.get(music_edit_temp_select.get(i)).getMusic_path();
                    deleteFile(need_delete_path);
                    is_delete_yuan_over=true;
                }
            }
        }
        if(is_all_music){
            Toast.makeText(MusicEditDeleteActivity.this,"已删除"+num+"首歌曲！！",Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MusicEditDeleteActivity.this,"已移除"+num+"首歌曲！！",Toast.LENGTH_SHORT).show();

            //通知歌单更新
            // 发送广播
            Intent temp_intent = new Intent();
            temp_intent.setAction("com.android.jiaqiao.SelectMusicService");
            temp_intent.putExtra("type", MainActivity.UPDATE_SHEET);
            temp_intent.putExtra("is_update_sheet", true);
            sendBroadcast(temp_intent);
        }
        if(is_delete_yuan_over){
            //启动service
            Intent select_music_intent = new Intent(this, SelectMusicService.class);
            startService(select_music_intent);
        }

        finish();
    }

    public void updateUi() {
        if (is_delete_yuan) {
            is_selected.setImageResource(R.drawable.is_selected);
        } else {
            is_selected.setImageResource(R.drawable.not_selected);
        }
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }

    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
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

}
