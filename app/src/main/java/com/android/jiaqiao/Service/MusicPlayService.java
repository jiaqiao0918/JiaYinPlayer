package com.android.jiaqiao.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.android.jiaqiao.Others.MediaButtonReceiver;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.Utils.SharedUtile;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.MusicPlayActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static com.android.jiaqiao.Utils.MusicPlayUtil.addTextToFile;
import static com.android.jiaqiao.jiayinplayer.PublicDate.music_play_now;
import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/8/26/0026.
 */

public class MusicPlayService extends Service {

    public static final int PLAY_MUSIC = 5000001;
    public static final int PLAY_LAST_MUSIC = 5000002;
    public static final int PLAY_NEXT_MUSIC = 5000003;
    public static final int START_STOP_MUSIC = 5000004;
    public static final int SEEK_TO = 5000005;

    public static final int GET_MUSIC_PLAY_TIME = 5000006;
    public static final int IS_PLAY = 5000007;
    public static final int UPDATE_PLAY_MODE = 5000008;

    //播放模式
    public static final int PLAY_MODE_ORDER = 6000001;//循环
    public static final int PLAY_MODE_RANDOM = 6000002;//随机
    public static final int PLAY_MODE_SINGLE = 6000003;//单曲

    //notification的点击事件
    public static final int MUSIC_PLAY_LAST = 700000001;
    public static final int MUSIC_PLAY_NEXT = 700000002;
    public static final int MUSCI_PLAY_IS_NOT = 700000003;
    public static final int NOTIFICATION_CLOSE = 700000004;
    public static final int MUSIC_PLAY_MODE = 700000005;
    public static final int MUSIC_IS_NOT_LOVE = 700000006;
//    public static final int UPDATE_NOTIFICATION = 700000007;


    private MediaPlayer music_media_player;
    private int play_position = 0;
    private int need_seek_to = 0;
    private boolean stop_seet_to = false;
    private Thread time_thread;
    private boolean start_stop_thread = true;
    private boolean is_playing = false;
    private boolean is_notification_update = false;
    private boolean is_love = false;

    private MusicPlayReceiver mReceiver;
    private IntentFilter mFilter;

    private HeadsetPlugReceiver headsetPlugReceiver;
    private IntentFilter intentFilter02;

    private RemoteViews notification_64_view, notification_100_view;
    private NotificationManager notify_manager;
    private Notification notify;

    private AudioManager mAudioManager;
    private ComponentName mComponent;

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


        //耳机插入拔出
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, intentFilter);

        //耳机按键
        // 获得AudioManager对象
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //构造一个ComponentName，指向MediaoButtonReceiver类
        mComponent = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mComponent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runningNotification();
        if (time_thread == null) {
            time_thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        if (start_stop_thread) {
                            if (music_media_player != null) {
                                if (music_media_player.isPlaying()) {
                                    Intent temp_intent = new Intent();
                                    temp_intent.setAction("com.android.jiaqiao");
                                    temp_intent.putExtra("type", MusicPlayService.GET_MUSIC_PLAY_TIME);
                                    temp_intent.putExtra("play_time", music_media_player.getCurrentPosition());
                                    sendBroadcast(temp_intent);
                                }
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
        }
        return START_STICKY;
    }

    public void runningNotification() {
        if (is_playing) {
            if (!PublicDate.is_notification_running) {
                notify_manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                NotificationCompat.Builder notification = new NotificationCompat.Builder(this).setVisibility(Notification.VISIBILITY_PUBLIC);//.setVisibility(Notification.VISIBILITY_PUBLIC);可以锁屏上显示，前提是手机系统支持
                notification_64_view = new RemoteViews(this.getPackageName(), R.layout.notification_64_layout);
                notification_100_view = new RemoteViews(this.getPackageName(), R.layout.notification_100_layout);

                updateNotificationUi();

                //跳转到播放界面
                Intent intent_to_music_play = new Intent(this, MusicPlayActivity.class);
                PendingIntent intent_go = PendingIntent.getActivity(this, 0, intent_to_music_play, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_64_view.setOnClickPendingIntent(R.id.notification_64_all, intent_go);

                //上一首
                Intent play_last = new Intent();
                play_last.setAction("com.android.jiaqiao");
                play_last.putExtra("type", MusicPlayService.MUSIC_PLAY_LAST);
                PendingIntent intent_play_last = PendingIntent.getBroadcast(this, 1, play_last, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_64_view.setOnClickPendingIntent(R.id.notification_music_play_last, intent_play_last);

                //播放暂停
                Intent play_is_not = new Intent();
                play_is_not.setAction("com.android.jiaqiao");
                play_is_not.putExtra("type", MusicPlayService.MUSCI_PLAY_IS_NOT);
                PendingIntent intent_play_is_not = PendingIntent.getBroadcast(this, 2, play_is_not, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_64_view.setOnClickPendingIntent(R.id.notification_music_play_is_not, intent_play_is_not);

                //下一首
                Intent play_next = new Intent();
                play_next.setAction("com.android.jiaqiao");
                play_next.putExtra("type", MusicPlayService.MUSIC_PLAY_NEXT);
                PendingIntent intent_play_next = PendingIntent.getBroadcast(this, 3, play_next, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_64_view.setOnClickPendingIntent(R.id.notification_music_play_next, intent_play_next);

                //关闭
                Intent notification_close = new Intent();
                notification_close.setAction("com.android.jiaqiao");
                notification_close.putExtra("type", MusicPlayService.NOTIFICATION_CLOSE);
                PendingIntent intent_notification_close = PendingIntent.getBroadcast(this, 4, notification_close, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_64_view.setOnClickPendingIntent(R.id.notification_close, intent_notification_close);


                //big

                notification_100_view.setOnClickPendingIntent(R.id.notification_100_all, intent_go);
                notification_100_view.setOnClickPendingIntent(R.id.notification_100_music_album_image, intent_go);
//上一首
                Intent play_last_100 = new Intent();
                play_last_100.setAction("com.android.jiaqiao");
                play_last_100.putExtra("type", MusicPlayService.MUSIC_PLAY_LAST);
                PendingIntent intent_play_last_100 = PendingIntent.getBroadcast(this, 101, play_last_100, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_100_view.setOnClickPendingIntent(R.id.notification_100_music_play_last, intent_play_last_100);

//                播放暂停
                Intent play_is_not_100 = new Intent();
                play_is_not_100.setAction("com.android.jiaqiao");
                play_is_not_100.putExtra("type", MusicPlayService.MUSCI_PLAY_IS_NOT);
                PendingIntent intent_play_is_not_100 = PendingIntent.getBroadcast(this, 102, play_is_not_100, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_100_view.setOnClickPendingIntent(R.id.notification_100_music_play_is_not, intent_play_is_not_100);

//                下一首
                Intent play_next_100 = new Intent();
                play_next_100.setAction("com.android.jiaqiao");
                play_next_100.putExtra("type", MusicPlayService.MUSIC_PLAY_NEXT);
                PendingIntent intent_play_next_100 = PendingIntent.getBroadcast(this, 103, play_next_100, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_100_view.setOnClickPendingIntent(R.id.notification_100_music_play_next, intent_play_next_100);

                //                收藏
                Intent play_love_100 = new Intent();
                play_love_100.setAction("com.android.jiaqiao");
                play_love_100.putExtra("type", MusicPlayService.MUSIC_IS_NOT_LOVE);
                PendingIntent intent_play_love_100 = PendingIntent.getBroadcast(this, 104, play_love_100, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_100_view.setOnClickPendingIntent(R.id.notification_100_music_love, intent_play_love_100);

                //                播放模式
                Intent play_mode_100 = new Intent();
                play_mode_100.setAction("com.android.jiaqiao");
                play_mode_100.putExtra("type", MusicPlayService.MUSIC_PLAY_MODE);
                PendingIntent intent_play_mode_100 = PendingIntent.getBroadcast(this, 105, play_mode_100, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_100_view.setOnClickPendingIntent(R.id.notification_100_music_play_mode, intent_play_mode_100);


//                关闭
                Intent notification_close_100 = new Intent();
                notification_close_100.setAction("com.android.jiaqiao");
                notification_close_100.putExtra("type", MusicPlayService.NOTIFICATION_CLOSE);
                PendingIntent intent_notification_close_100 = PendingIntent.getBroadcast(this, 106, notification_close_100, PendingIntent.FLAG_UPDATE_CURRENT);
                notification_100_view.setOnClickPendingIntent(R.id.notification_100_close, intent_notification_close_100);


                notification.setContent(notification_64_view);
                notification.setCustomBigContentView(notification_100_view);
                notification.setSmallIcon(R.drawable.icon); // 设置顶部图标
                notify = notification.build();
                notify.flags = Notification.FLAG_ONGOING_EVENT;
                notify_manager.notify(100, notify);
                PublicDate.is_notification_running = true;
            }
        } else {
            PublicDate.is_notification_running = false;
        }
    }

    public void updateNotificationUi() {
        Bitmap album_bitmap = MusicUtils.getArtwork(this, music_play_now.getMusic_id(), music_play_now.getMusic_album_id(), true);
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        if (album_bitmap != null) {
            //将bitmap 分辨率压缩0.5f（一般）
            album_bitmap = Bitmap.createBitmap(album_bitmap, 0, 0, album_bitmap.getWidth(),
                    album_bitmap.getHeight(), matrix, true);
        } else {
            album_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_album_image);
            album_bitmap = Bitmap.createBitmap(album_bitmap, 0, 0, album_bitmap.getWidth(),
                    album_bitmap.getHeight(), matrix, true);
        }
        if (notification_64_view != null) {
            //设置界面64
            if (album_bitmap != null) {
                notification_64_view.setImageViewBitmap(R.id.notification_music_album_image, album_bitmap);
            } else {
                notification_64_view.setImageViewResource(R.id.notification_music_album_image, R.drawable.no_album_image);
            }
            notification_64_view.setTextViewText(R.id.notification_music_tittle, music_play_now.getMusic_title());
            String artist_album = music_play_now.getMusic_artist();
            if (music_play_now.getMusic_album().trim().replaceAll(" ", "").length() > 0) {
                artist_album += " - " + music_play_now.getMusic_album();
            }
            notification_64_view.setTextViewText(R.id.notification_music_artist, artist_album);
            if (music_media_player != null && is_playing) {
                notification_64_view.setImageViewResource(R.id.notification_music_play_is_not, R.drawable.notification_music_play_is);
            } else {
                notification_64_view.setImageViewResource(R.id.notification_music_play_is_not, R.drawable.notification_music_play_not);
            }
        }
        if (notification_100_view != null) {
            //设置界面100
            if (album_bitmap != null) {
                notification_100_view.setImageViewBitmap(R.id.notification_100_music_album_image, album_bitmap);
            } else {
                notification_100_view.setImageViewResource(R.id.notification_100_music_album_image, R.drawable.no_album_image);
            }
            notification_100_view.setTextViewText(R.id.notification_100_music_tittle, music_play_now.getMusic_title());
            String artist_album = music_play_now.getMusic_artist();
            if (music_play_now.getMusic_album().trim().replaceAll(" ", "").length() > 0) {
                artist_album += " - " + music_play_now.getMusic_album();
            }
            notification_100_view.setTextViewText(R.id.notification_100_music_artist, artist_album);
            if (music_media_player != null && is_playing) {
                notification_100_view.setImageViewResource(R.id.notification_100_music_play_is_not, R.drawable.notification_music_play_is);
            } else {
                notification_100_view.setImageViewResource(R.id.notification_100_music_play_is_not, R.drawable.notification_music_play_not);
            }
            updateNotificationPlayMode();
            updateNotificationLove();
        }
    }

    public void updateNotificationPlayMode() {
        switch (PublicDate.play_mode) {
            case MusicPlayService.PLAY_MODE_ORDER:
                notification_100_view.setImageViewResource(R.id.notification_100_music_play_mode, R.drawable.notification_music_play_mode_order);
                break;
            case MusicPlayService.PLAY_MODE_RANDOM:
                notification_100_view.setImageViewResource(R.id.notification_100_music_play_mode, R.drawable.notification_music_play_mode_random);
                break;
            case MusicPlayService.PLAY_MODE_SINGLE:
                notification_100_view.setImageViewResource(R.id.notification_100_music_play_mode, R.drawable.notification_music_play_mode_single);
                break;
        }
    }

    public void updateNotificationLove() {
        if (notification_100_view != null) {
            if (is_love) {
                notification_100_view.setImageViewResource(R.id.notification_100_music_love, R.drawable.notification_music_play_is_love);
            } else {
                notification_100_view.setImageViewResource(R.id.notification_100_music_love, R.drawable.notification_music_play_not_love);
            }
        }
    }

    public void updateNotificationPlay() {
        if (notification_64_view != null) {
            if (is_playing) {
                notification_64_view.setImageViewResource(R.id.notification_music_play_is_not, R.drawable.notification_music_play_is);
                notification_100_view.setImageViewResource(R.id.notification_100_music_play_is_not, R.drawable.notification_music_play_is);

            } else {
                notification_64_view.setImageViewResource(R.id.notification_music_play_is_not, R.drawable.notification_music_play_not);
                notification_100_view.setImageViewResource(R.id.notification_100_music_play_is_not, R.drawable.notification_music_play_is);
            }
        }
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
        if (time_thread != null) {
            time_thread.interrupt();
            start_stop_thread = false;

        }
        if (notify_manager != null) {
            notify_manager.cancel(100);
            PublicDate.is_notification_running = false;
        }
        unregisterReceiver(mReceiver);
        unregisterReceiver(headsetPlugReceiver);
        //注销方法
        mAudioManager.unregisterMediaButtonEventReceiver(mComponent);
    }

    public void playMusicFromPathTime(String path, int time) {
        if (music_media_player != null) {
            if (music_media_player.isPlaying()) {
                music_media_player.stop();
                music_media_player.release();
            }
        }

        music_media_player = null;
        music_media_player = MediaPlayer.create(this, Uri.fromFile(new File(path)));
        music_media_player.seekTo(time);
        music_media_player.start();
        music_media_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if (!PublicDate.is_timing_destroy) {
                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", TimingService.TIMING_MUSIC_POSITION);
                    sendBroadcast(temp_intent02);
                }
                intentIsPlaying(false);
                start_stop_thread = false;
                switch (PublicDate.play_mode) {
                    case MusicPlayService.PLAY_MODE_ORDER:
                    case MusicPlayService.PLAY_MODE_RANDOM:
                        autoPlayNextMusic();
                        break;
                }
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MusicPlayService.PLAY_MUSIC);
                sendBroadcast(temp_intent);
                intentIsPlaying(true);
                start_stop_thread = true;
                is_playing = true;
                PublicDate.is_play = true;
                is_notification_update = true;

            }
        });
        is_playing = true;
        PublicDate.is_play = true;
        runningNotification();
        if (time_thread != null) {
            start_stop_thread = true;
        } else {
            time_thread.start();
        }
        if (is_notification_update) {
            updateNotificationUi();
            notify_manager.notify(100, notify);//刷新通知
            is_notification_update = false;
        }

        start_stop_thread = true;
        intentIsPlaying(true);
        runningNotification();

        is_love = selectIsLoveMusic();
        updateNotificationLove();
        if (notify_manager != null) {
            notify_manager.notify(100, notify);//刷新通知
        }
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

    public void autoPlayNextMusic() {
        int temp = 0;
        if (PublicDate.play_mode != MusicPlayService.PLAY_MODE_RANDOM) {
            temp = (PublicDate.music_play_list_position + 1 + PublicDate.music_play.size()) % PublicDate.music_play.size();
        } else {
            temp = PublicDate.play_randoms.get(3);
        }
        PublicDate.music_play.get(PublicDate.music_play_list_position).setIs_playing(false);
        PublicDate.music_play.get(temp).setIs_playing(true);
        PublicDate.music_play_list_position = temp;
        PublicDate.music_play_now = PublicDate.music_play.get(temp);

//        SharedUtile.putSharedInt(MusicPlayService.this,"music_play_list_position", PublicDate.music_play_list_position); 
        SharedUtile.putSharedInt(MusicPlayService.this, "music_play_list_position", PublicDate.music_play_list_position);

        Intent temp_intent = new Intent();
        temp_intent.setAction("com.android.jiaqiao");
        temp_intent.putExtra("type", MainActivity.AUTO_PLAY_NEXT);
        sendBroadcast(temp_intent);

    }

    public void startStopMusic() {
        if (music_media_player != null) {
            if (music_media_player.isPlaying()) {
                music_media_player.pause();//暂停播放
                need_seek_to = music_media_player.getCurrentPosition();
                intentIsPlaying(false);
                is_playing = false;
                PublicDate.is_play = false;
                start_stop_thread = false;

                if (!PublicDate.is_timing_destroy) {
                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", TimingService.TIMING_ONLY_DESTROY);
                    sendBroadcast(temp_intent02);
                }

            } else {
                if (stop_seet_to) {
                    playMusicFromPathTime(music_play_now.getMusic_path(), need_seek_to);
                    stop_seet_to = false;
                } else {
                    music_media_player.start();//继续播放
                }
                intentIsPlaying(true);
                is_playing = true;
                PublicDate.is_play = true;
                start_stop_thread = true;
                runningNotification();
            }
        } else {
            if (PublicDate.music_play_now != null) {
                playMusicFromPathTime(music_play_now.getMusic_path(), 0);
            }
        }
    }

    public void seekToTime(int time) {
        if (time > -1) {
            if (music_media_player != null) {
                if (music_media_player.isPlaying()) {
                    music_media_player.seekTo(time);
                } else {
                    playMusicFromPathTime(music_play_now.getMusic_path(), time);
                }

            } else {
                playMusicFromPathTime(music_play_now.getMusic_path(), time);
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

    public boolean selectIsLoveMusic() {
        try {
            FileReader fr = new FileReader(getPath("love"));
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.indexOf(PublicDate.music_play_now.getMusic_id() + "") > -1 && line.indexOf(PublicDate.music_play_now.getMusic_title()) > -1) {
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

    public String getPath(String sheet_id) {
        File file = new File(getFilesDir() + "/music_sheet_list");
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

    class MusicPlayReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MusicPlayService.PLAY_MUSIC:
                    playMusicFromPathTime(music_play_now.getMusic_path(), 0);

                    break;
                case MusicPlayService.PLAY_LAST_MUSIC:
                    playMusicFromPathTime(music_play_now.getMusic_path(), 0);
                    playLastMusic();

                    if (notify_manager != null) {
                        updateNotificationUi();
                        notify_manager.notify(100, notify);//刷新通知
                    }

                    break;
                case MusicPlayService.PLAY_NEXT_MUSIC:
                    playMusicFromPathTime(music_play_now.getMusic_path(), 0);
                    playNextMusic();

                    if (notify_manager != null) {
                        updateNotificationUi();
                        notify_manager.notify(100, notify);//刷新通知
                    }

                    break;
                case MusicPlayService.START_STOP_MUSIC:
                    startStopMusic();

                    if (notify_manager != null) {
                        updateNotificationPlay();//更新播放状态
                        notify_manager.notify(100, notify);//刷新通知
                    }

                    break;
                case MusicPlayService.SEEK_TO:
                    int time = intent.getIntExtra("seek_to_time", -1);
                    seekToTime(time);
                    break;
                case MusicPlayService.MUSIC_PLAY_LAST:

                    int temp = 0;
                    if (PublicDate.play_mode != MusicPlayService.PLAY_MODE_RANDOM) {
                        temp = (PublicDate.music_play_list_position - 1 + PublicDate.music_play.size()) % PublicDate.music_play.size();
                    } else {
                        temp = PublicDate.play_randoms.get(1);
                    }
                    PublicDate.music_play.get(PublicDate.music_play_list_position).setIs_playing(false);
                    PublicDate.music_play.get(temp).setIs_playing(true);
                    PublicDate.music_play_list_position = temp;
                    PublicDate.music_play_now = PublicDate.music_play.get(temp);
                    SharedUtile.putSharedInt(MusicPlayService.this, "music_play_list_position", PublicDate.music_play_list_position);

                    playMusicFromPathTime(music_play_now.getMusic_path(), 0);

                    Intent temp_intent04 = new Intent();
                    temp_intent04.setAction("com.android.jiaqiao");
                    temp_intent04.putExtra("type", UpdateService.TO_UPDATE_UI);
                    sendBroadcast(temp_intent04);

//                    Intent temp_intent04 = new Intent();
//                    temp_intent04.setAction("com.android.jiaqiao");
//                    temp_intent04.putExtra("type", MainActivity.AUTO_PLAY_NEXT);
//                    temp_intent04.putExtra("auto_update_mode", false);
//                    sendBroadcast(temp_intent04);

//                    updateNotificationUi();
//                    notify_manager.notify(100, notify);//刷新通知

                    break;
                case MusicPlayService.MUSCI_PLAY_IS_NOT:
                    startStopMusic();
                    updateNotificationPlay();
                    notify_manager.notify(100, notify);
                    break;
                case MusicPlayService.MUSIC_PLAY_NEXT:

                    int temp02 = 0;
                    if (PublicDate.play_mode != MusicPlayService.PLAY_MODE_RANDOM) {
                        temp02 = (PublicDate.music_play_list_position + 1 + PublicDate.music_play.size()) % PublicDate.music_play.size();
                    } else {
                        temp02 = PublicDate.play_randoms.get(3);
                    }
                    PublicDate.music_play.get(PublicDate.music_play_list_position).setIs_playing(false);
                    PublicDate.music_play.get(temp02).setIs_playing(true);
                    PublicDate.music_play_list_position = temp02;
                    PublicDate.music_play_now = PublicDate.music_play.get(temp02);
                    SharedUtile.putSharedInt(MusicPlayService.this, "music_play_list_position", PublicDate.music_play_list_position);

                    playMusicFromPathTime(music_play_now.getMusic_path(), 0);

                    Intent temp_intent03 = new Intent();
                    temp_intent03.setAction("com.android.jiaqiao");
                    temp_intent03.putExtra("type", UpdateService.TO_UPDATE_UI);
                    sendBroadcast(temp_intent03);

//                    Intent temp_intent03 = new Intent();
//                    temp_intent03.setAction("com.android.jiaqiao");
//                    temp_intent03.putExtra("type", MainActivity.AUTO_PLAY_NEXT);
//                    sendBroadcast(temp_intent03);
//
//                    updateNotificationUi();
//                    notify_manager.notify(100, notify);//刷新通知

                    break;
                case MusicPlayService.NOTIFICATION_CLOSE:
                    if (music_media_player != null && is_playing) {
                        startStopMusic();
                    }
                    if (notify_manager != null) {
                        notify_manager.cancel(100);
                        PublicDate.is_notification_running = false;
                    }

                    break;
                case MusicPlayService.MUSIC_PLAY_MODE:
                    if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_ORDER) {
                        PublicDate.play_mode = MusicPlayService.PLAY_MODE_RANDOM;
                    } else if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_RANDOM) {
                        PublicDate.play_mode = MusicPlayService.PLAY_MODE_SINGLE;
                    } else if (PublicDate.play_mode == MusicPlayService.PLAY_MODE_SINGLE) {
                        PublicDate.play_mode = MusicPlayService.PLAY_MODE_ORDER;
                    }
//                    //发送广播
//                    Intent temp_intent = new Intent();
//                    temp_intent.setAction("com.android.jiaqiao");
//                    temp_intent.putExtra("type", MusicPlayService.UPDATE_PLAY_MODE);
//                    sendBroadcast(temp_intent);
//                    getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("play_mode", PublicDate.play_mode).commit();
                    SharedUtile.putSharedInt(MusicPlayService.this, "play_mode", PublicDate.play_mode);
                    updateNotificationPlayMode();
                    notify_manager.notify(100, notify);//刷新通知
                    break;
                case MusicPlayService.MUSIC_IS_NOT_LOVE:

                    String path = getPath("love");
                    if (is_love) {
                        String music_id = music_play_now.getMusic_id() + "";
                        deleteSelectStr(path, music_id);
                        is_love = false;
                    } else {
                        String add_context = music_play_now.getMusic_id() + separate_str + music_play_now.getMusic_title();
                        if (!isHavaContextFromPath(path, add_context)) {
                            addTextToFile(path, add_context);
                        }
                        is_love = true;
                    }

                    //发送广播
                    Intent temp_intent02 = new Intent();
                    temp_intent02.setAction("com.android.jiaqiao");
                    temp_intent02.putExtra("type", MainActivity.ALL_MUSIC_UPDATE);
                    temp_intent02.putExtra("is_update", true);
                    sendBroadcast(temp_intent02);

                    updateNotificationLove();
                    notify_manager.notify(100, notify);//刷新通知
                    break;
//                case MusicPlayService.UPDATE_NOTIFICATION:
//                    if (notify_manager != null) {
//                        updateNotificationUi();
//                        notify_manager.notify(100, notify);//刷新通知
//                    }
//                    break;


                //Test
                case UpdateService.UPDATE_UI:

                    if (notify_manager != null) {
                        updateNotificationUi();
                        notify_manager.notify(100, notify);//刷新通知
                    }

                    break;


            }
        }
    }

    public class HeadsetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) {//拔出耳机
                    PublicDate.is_headset = false;
                    mAudioManager.unregisterMediaButtonEventReceiver(mComponent);

                    if (SharedUtile.getSharedBoolean(MusicPlayService.this, "headset_stop_play", true)) {
                        if (PublicDate.is_play) {
                            startStopMusic();
                        }
                    }

                } else if (intent.getIntExtra("state", 0) == 1) {//插入耳机
                    PublicDate.is_headset = true;
                    mAudioManager.registerMediaButtonEventReceiver(mComponent);

                    if (SharedUtile.getSharedBoolean(MusicPlayService.this, "headset_start_play", false)) {
                        if (!PublicDate.is_play) {
                            startStopMusic();
                        }
                    }
                }
            }
        }
    }

}
