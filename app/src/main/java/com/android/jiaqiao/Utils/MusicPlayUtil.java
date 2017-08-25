package com.android.jiaqiao.Utils;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.PublicDate;

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
 * Created by jiaqiao on 2017/8/25/0025.
 */

public class MusicPlayUtil {
    private static ArrayList<MusicInfo> music_play = new ArrayList<MusicInfo>();

    public static void saveMusicPlayList() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                music_play = PublicDate.music_play;
                if (music_play.size() > 0) {
                    String path = getSaveMusicPlayPath();
                    for (int i = 0; i < music_play.size(); i++) {
                        String add_context = music_play.get(i).getMusic_id() + separate_str + music_play.get(i).getMusic_title();
                        addTextToFile(path, add_context);
                    }

                }
                this.interrupt();//中断线程
            }
        }.start();
    }

    public static void addTextToFile(String path_name, String context) {
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

    public static String getSaveMusicPlayPath() {
        File file = new File(PublicDate.path_files_dir + "/music_play");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file.getPath() + "/music_play.txt");
        if (file2.exists()) {
            file2.delete();
        }
        try {
            file2.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file2.getPath().toString();
    }

    public static String getGetMusicPlayPath() {
        File file = new File(PublicDate.path_files_dir + "/music_play");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file.getPath() + "/music_play.txt");
        if (!file2.exists()) {
            try {
                file2.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file2.getPath().toString();
    }

    public static ArrayList<MusicInfo> getMusicPlayList() {
        ArrayList<MusicInfo> list_temp = new ArrayList<MusicInfo>();
        try {
            FileReader fr = new FileReader(getGetMusicPlayPath());
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (temp.length() > 0 && temp.indexOf(separate_str) > -1) {
                    String[] music_temp = temp.split(separate_str);
                    if (music_temp.length >= 2) {
                        MusicInfo music_info_temp = getFromListGetMusicInfo(music_temp[0], music_temp[1]);
                        if (music_info_temp != null) {
                            list_temp.add(music_info_temp);
                        }
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

    public static MusicInfo getFromListGetMusicInfo(String music_id, String music_title) {
        ArrayList<MusicInfo> list_temp = PublicDate.public_music_all;
        for (int i = 0; i < list_temp.size(); i++) {
            if ((list_temp.get(i).getMusic_id() + "").equals(music_id) && list_temp.get(i).getMusic_title().equals(music_title)) {
                return list_temp.get(i);
            }
        }
        return null;
    }

    public static int selectMusicPosition(ArrayList<MusicInfo> select_list, MusicInfo select_music) {
        if (select_list.size() > 0) {
            for (int i =0;i<select_list.size();i++){
                MusicInfo music_temp = select_list.get(i);
                if(music_temp.getMusic_id()==select_music.getMusic_id()&&music_temp.getMusic_title().trim().equals(select_music.getMusic_title().trim())){
                    return i;
                }
            }
        }
        return -1;

    }
}
