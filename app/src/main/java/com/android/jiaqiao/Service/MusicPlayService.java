package com.android.jiaqiao.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;

import java.io.File;

/**
 * Created by jiaqiao on 2017/8/26/0026.
 */

public class MusicPlayService extends Service {

    public static final int PLAY_MUSIC = 500000001;
    public static final int PLAY_LAST_MUSIC = 500000002;
    public static final int PLAY_NEXT_MUSIC = 500000003;
    public static final int START_STOP_MUSIC = 500000004;
    public static final int SEEK_TO = 500000005;

    public static final int GET_MUSIC_PLAY_TIME = 500000006;
    public static final int IS_PLAY = 500000007;
    public static final int UPDATE_PLAY_MODE = 500000008;

    public static final int PLAY_MODE_ORDER = 600000001;//循环
    public static final int PLAY_MODE_RANDOM = 600000002;//随机
    public static final int PLAY_MODE_SINGLE = 600000003;//单曲


    private MediaPlayer music_media_player;
    private int play_position = 0;
    private int need_seek_to = 0;
    private boolean stop_seet_to = false;
    private Thread time_thread;
    private boolean start_stop_thread = true;

    private MusicPlayReceiver mReceiver;
    private IntentFilter mFilter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        play_position = PublicDate.music_play_list_position;
        PublicDate.is_music_play_destroy = false;
        mReceiver = new MusicPlayReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        registerReceiver(mReceiver, mFilter);
        start_stop_thread = true;
        intentIsPlaying(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        time_thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (music_media_player != null) {
                        if (music_media_player.isPlaying()) {
                            Intent temp_intent = new Intent();
                            temp_intent.setAction("com.android.jiaqiao");
                            temp_intent.putExtra("type", MusicPlayService.GET_MUSIC_PLAY_TIME);
                            temp_intent.putExtra("play_time", music_media_player.getCurrentPosition());
                            sendBroadcast(temp_intent);
                        }
                    }
                    try {
                        time_thread.sleep(1000);//毫秒
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        time_thread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        PublicDate.is_music_play_destroy = true;
        if (music_media_player != null) {
            if (music_media_player.isPlaying()) {
                music_media_player.stop();
            }
            music_media_player.release();
            music_media_player = null;
        }
        if (time_thread.isAlive()) {
            time_thread.interrupt();
        }
        unregisterReceiver(mReceiver);
    }

    public void playMusicFromPathTime(String path, int time) {
        if (music_media_player != null) {
            if (music_media_player.isPlaying()) {
                music_media_player.stop();
                music_media_player.release();
            }
        }
//        Log.i("into", path);
        music_media_player = null;
        music_media_player = MediaPlayer.create(this, Uri.fromFile(new File(path)));
        music_media_player.seekTo(time);
        music_media_player.start();
        music_media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                intentIsPlaying(false);
                start_stop_thread = false;
                switch (PublicDate.play_mode) {
                    case MusicPlayService.PLAY_MODE_ORDER:
                    case MusicPlayService.PLAY_MODE_RANDOM:
                        playNextMusic();
                        break;
                }
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.PLAY_MUSIC);
                sendBroadcast(temp_intent);
                intentIsPlaying(true);

            }
        });

        if (time_thread.isAlive()) {
            start_stop_thread = true;
        } else {
            time_thread.start();
        }
        intentIsPlaying(true);
    }

    public void playLastMusic() {
        Intent temp_intent = new Intent();
        temp_intent.setAction("com.android.jiaqiao");
        temp_intent.putExtra("type", MainActivity.SERVICE_UPDATE_MUSIC_PLAY);
        temp_intent.putExtra("service_is_update", true);
        temp_intent.putExtra("update_mode", false);
        sendBroadcast(temp_intent);
    }

    public void playNextMusic() {
        Intent temp_intent = new Intent();
        temp_intent.setAction("com.android.jiaqiao");
        temp_intent.putExtra("type", MainActivity.SERVICE_UPDATE_MUSIC_PLAY);
        temp_intent.putExtra("service_is_update", true);
        temp_intent.putExtra("update_mode", true);
        sendBroadcast(temp_intent);
    }

    public void startStopMusic() {
        if (music_media_player != null) {
            if (music_media_player.isPlaying()) {
                music_media_player.pause();//暂停播放
                need_seek_to = music_media_player.getCurrentPosition();
                intentIsPlaying(false);
            } else {
                if (stop_seet_to) {
                    playMusicFromPathTime(PublicDate.music_play_now.getMusic_path(), need_seek_to);
                    stop_seet_to = false;
                } else {
                    music_media_player.start();//继续播放
                }
                intentIsPlaying(true);
            }
        } else {
            playMusicFromPathTime(PublicDate.music_play_now.getMusic_path(), 0);
        }
    }

    public void seekToTime(int time) {
        if (time > -1) {
            if (music_media_player != null) {
                if (music_media_player.isPlaying()) {
                    music_media_player.seekTo(time);
                } else {
                    playMusicFromPathTime(PublicDate.music_play_now.getMusic_path(), time);
                }

            } else {
                playMusicFromPathTime(PublicDate.music_play_now.getMusic_path(), time);
            }
        }
    }

    public void intentIsPlaying(boolean is_play) {
        Intent temp_intent = new Intent();
        temp_intent.setAction("com.android.jiaqiao");
        temp_intent.putExtra("type", MusicPlayService.IS_PLAY);
        temp_intent.putExtra("is_playing", is_play);
        sendBroadcast(temp_intent);
    }

    class MusicPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MusicPlayService.PLAY_MUSIC:
                    playMusicFromPathTime(PublicDate.music_play_now.getMusic_path(), 0);
                    break;
                case MusicPlayService.PLAY_LAST_MUSIC:
                    playMusicFromPathTime(PublicDate.music_play_now.getMusic_path(), 0);
                    playLastMusic();
                    break;
                case MusicPlayService.PLAY_NEXT_MUSIC:
                    playMusicFromPathTime(PublicDate.music_play_now.getMusic_path(), 0);
                    playNextMusic();
                    break;
                case MusicPlayService.START_STOP_MUSIC:
                    startStopMusic();
                    break;
                case MusicPlayService.SEEK_TO:
                    int time = intent.getIntExtra("seek_to_time", -1);
                    seekToTime(time);
                    break;
            }
        }
    }

}
