package com.android.jiaqiao.Utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * 本类 采用泛型 广泛接受 ArrayList 直接做缓存，即是开发中经常用到的 bean；
 * <p>
 * 使用注意： 传进来的 ArrayList 所绑定的 种子，一定要是 已经继承 Serializable 接口的；
 * <p>
 * 使用文本流做缓存。
 */

public class DataInfoCache {

    /**
     * 定义一些你项目里面的 缓存文件名字 ，自定义，不要也没关系，调用函数再传入也行
     */

    public static String QzInfo = "Qz_List_Info";
    public static String CyInfo = "Cy_List_Info";
    private static String DataCache = "Data_Cache_File";

    /**
     * 保存 一组 数据
     *
     * @param ctx       上下文
     * @param data      种子
     * @param cacheName 缓存文件名
     */
    public static <T> void saveListCache(Context ctx, ArrayList<T> data, String cacheName) {
        new DataCache<T>().saveGlobal(ctx, data, cacheName);
    }

    /**
     * 直接根据 缓存文件名获取
     */
    public static <T> ArrayList<T> loadListCache(Context ctx, String cacheName) {
        return new DataCache<T>().loadGlobal(ctx, cacheName);
    }

    /**
     * 获取 一组 数据
     *
     * @param <T> 数据缓存 save or load
     */
    static class DataCache<T> {
        public void save(Context ctx, ArrayList<T> data, String name) {
            save(ctx, data, name, "");
        }

        public void saveGlobal(Context ctx, ArrayList<T> data, String name) {
            save(ctx, data, name, DataCache);
        }

        private void save(Context ctx, ArrayList<T> data, String name, String folder) {
            if (ctx == null) {
                return;
            }
            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }
            if (file.exists()) {
                file.delete();
            }
            Log.d("zzzzz", file.getAbsolutePath());
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
                oos.writeObject(data);
                oos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public ArrayList<T> load(Context ctx, String name) {
            return load(ctx, name, "");
        }

        public ArrayList<T> loadGlobal(Context ctx, String name) {
            return load(ctx, name, DataCache);
        }

        private ArrayList<T> load(Context ctx, String name, String folder) {
            ArrayList<T> data = null;

            File file;
            if (!folder.isEmpty()) {
                File fileDir = new File(ctx.getFilesDir(), folder);
                if (!fileDir.exists() || !fileDir.isDirectory()) {
                    fileDir.mkdir();
                }
                file = new File(fileDir, name);
            } else {
                file = new File(ctx.getFilesDir(), name);
            }
            Log.d("zzzzz", "file " + file.getAbsolutePath());
            if (file.exists()) {
                try {
                    Log.d("zzzzz", "write object");
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                    data = (ArrayList<T>) ois.readObject();
                    ois.close();
                } catch (Exception e) {
                    Log.d("zzzzz", e.toString());
                }
            }
            if (data == null) {     /** 如果没有 */
                Log.d("zzzzz", "data == null");
                data = new ArrayList<T>();
            }
            return data;
        }
    }
}
