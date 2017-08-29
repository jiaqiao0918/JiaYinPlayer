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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Adapter.MusicSheetAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.Utils.MusicPlayUtil;
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

import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/8/20/0020.
 */

public class MusicEditAddMusicSheetActivity extends Activity {

    private ListView show_music_sheet_name_list;
    private MusicSheetAdapter music_sheet_adapter;
    private Button add_to_music_play_list;

    private ArrayList<SheetInfo> music_sheet_info_list = new ArrayList<SheetInfo>();

    private String path = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_edit_add_music_sheet);
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

        path = PublicDate.files_dir + "/music_sheet/music_sheet_name.txt";
        music_sheet_info_list = getMusicSheetToArrayList(path);
        show_music_sheet_name_list = (ListView) findViewById(R.id.show_music_sheet_name_list);
        add_to_music_play_list = (Button) findViewById(R.id.add_to_music_play_list);
        if (getIntent().getBooleanExtra("is_music_play_list02", false)) {
            View add_to_music_play_view = (View) findViewById(R.id.add_to_music_play_view);
            add_to_music_play_view.setVisibility(View.GONE);
            add_to_music_play_list.setVisibility(View.GONE);

        }

        add_to_music_play_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = 0;
                ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
                ArrayList<MusicInfo> music_temp = new ArrayList<MusicInfo>();
                ArrayList<Integer> music_edit_temp_select = new ArrayList<Integer>();
                music_edit_temp = PublicDate.public_music_edit_temp;
                music_edit_temp_select = PublicDate.public_music_edit_temp_select;
                if (music_edit_temp.size()>0&&music_edit_temp_select.size()>0) {
                    music_temp = PublicDate.music_play;
                    for (int i = 0; i < music_edit_temp_select.size(); i++) {
                        music_temp.add(music_edit_temp.get(music_edit_temp_select.get(i)));
                        num++;
                    }
                }
                if(num>0){
                    Toast.makeText(MusicEditAddMusicSheetActivity.this, "已添加" + num + "首歌曲！！", Toast.LENGTH_SHORT).show();

                    PublicDate.music_play = music_temp;
                    PublicDate.music_play_now = PublicDate.music_play.get(PublicDate.music_play_list_position);
                    MusicPlayUtil.saveMusicPlayList();
                    PublicDate.update_music_play = true;

                    int temp_position = MusicPlayUtil.selectMusicPosition(music_edit_temp,PublicDate.music_play_now);
                    if(temp_position>-1){
                        PublicDate.music_play_list_position = temp_position;
                        getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position", PublicDate.music_play_list_position).commit();
                    }

                    //通知歌单更新
                    // 发送广播
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", MainActivity.UPDATE_SHEET);
                    temp_intent.putExtra("is_update_sheet", true);
                    sendBroadcast(temp_intent);
                }
                finish();
            }
        });

        if (music_sheet_info_list.size() > 0) {
            music_sheet_adapter = new MusicSheetAdapter(this, music_sheet_info_list);
            show_music_sheet_name_list.setAdapter(music_sheet_adapter);
            show_music_sheet_name_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                    ArrayList<MusicInfo> music_edit_temp = new ArrayList<MusicInfo>();
                    music_edit_temp = PublicDate.public_music_edit_temp;
                    music_edit_select = PublicDate.public_music_edit_temp_select;
                    if (music_edit_select.size() > 0) {
                        path = getPath(music_sheet_info_list.get(position).getSheet_id());
                        int num = 0;
                        for (int i = 0; i < music_edit_select.size(); i++) {
                            String add_context = music_edit_temp.get(music_edit_select.get(i)).getMusic_id() + separate_str + music_edit_temp.get(music_edit_select.get(i)).getMusic_title();
                            if (!isHavaContextFromPath(path, add_context)) {
                                addTextToFile(path, add_context);
                                num++;
                            }
                        }
                        if (num > 0) {
                            Toast.makeText(MusicEditAddMusicSheetActivity.this, num + "首歌曲添加成功！！", Toast.LENGTH_SHORT).show();

                            //发送广播
                            Intent temp_intent = new Intent();
                            temp_intent.setAction("com.android.jiaqiao");
                            temp_intent.putExtra("type", MainActivity.ALL_MUSIC_UPDATE);
                            temp_intent.putExtra("is_update", true);
                            sendBroadcast(temp_intent);
                        }
                    }
                    PublicDate.public_music_edit_temp = null;
                    PublicDate.public_music_edit_temp_select = null;
                    finish();
                }
            });
        } else {
            TextView no_music_sheet = (TextView) findViewById(R.id.no_music_sheet);
            no_music_sheet.setVisibility(View.VISIBLE);
            show_music_sheet_name_list.setVisibility(View.GONE);
        }

    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(R.anim.dialog_exit_anim, 0);
        //设置ActivityToDialog的退出动画，不知道为什么在xml文件中设置退出动画会错位
    }

    public void cancel_button(View view) {
        PublicDate.public_music_edit_temp = null;
        PublicDate.public_music_edit_temp_select = null;
        this.finish();
    }

    public void create_new_music_sheet(View view) {
        startActivity(new Intent(MusicEditAddMusicSheetActivity.this, MusicEditAddMusicSheetNameActivity.class));
        this.finish();
    }

    public ArrayList<SheetInfo> getMusicSheetToArrayList(String path_name) {
        ArrayList<SheetInfo> list_temp = new ArrayList<>();
        try {
            FileReader fr = new FileReader(path_name);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (temp.length() > 0 && temp.indexOf(separate_str) > -1) {
                    String[] name = temp.split(separate_str);
                    if (name.length >= 2) {
                        list_temp.add(new SheetInfo(name[0], name[1]));
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
        return list_temp;
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