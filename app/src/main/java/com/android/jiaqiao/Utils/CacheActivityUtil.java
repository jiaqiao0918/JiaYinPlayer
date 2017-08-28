package com.android.jiaqiao.Utils;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jiaqiao on 2017/8/28/0028.
 */

public class CacheActivityUtil {

    public static List<Activity> activityList = new LinkedList<Activity>();

    //在activity创建时将activity添加进来
    public static void addActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(activity);
        }
    }

    // 结束所有Activity
    public static void finishActivity() {
        for (Activity activity : activityList) {
            activity.finish();
        }
    }
}