package com.android.jiaqiao.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jiaqiao on 2017/9/4/0004.
 */

public class UpdateService extends Service {

    public static final int UPDATE_LIST_UI = 900000001;
    public static final int UPDATE_UI = 900000001;

    public static final int TO_UPDATE_LIST_UI = 900000001;
    public static final int TO_UPDATE_UI = 900000001;

    private UpdateReceiver mReceiver;
    private IntentFilter mFilter;
    private HeadsetPlugReceiver headsetPlugReceiver;
    private IntentFilter intentFilter;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mReceiver = new UpdateReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        registerReceiver(mReceiver, mFilter);

        headsetPlugReceiver = new HeadsetPlugReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        registerReceiver(headsetPlugReceiver, intentFilter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(headsetPlugReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    class UpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case UpdateService.TO_UPDATE_UI:
                    Intent temp_intent = new Intent();
                    temp_intent.setAction("com.android.jiaqiao");
                    temp_intent.putExtra("type", UpdateService.UPDATE_UI);
                    sendBroadcast(temp_intent);
                    break;

            }
        }
    }

    public class HeadsetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("into","asdasd");
            if (intent.hasExtra("state")) {
                if (intent.getIntExtra("state", 0) == 0) {
                    Toast.makeText(context, "无耳机", Toast.LENGTH_LONG).show();
                } else if (intent.getIntExtra("state", 0) == 1) {
                    Toast.makeText(context, "耳机插入", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
