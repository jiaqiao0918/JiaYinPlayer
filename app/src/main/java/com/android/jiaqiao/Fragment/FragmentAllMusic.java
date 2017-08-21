package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jiaqiao.Activity.MusicEditItemLongActivity;
import com.android.jiaqiao.Activity.MusicEditNeedListActivity;
import com.android.jiaqiao.Adapter.RecyclerViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Utils.FastBlurUtil;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jiaqiao on 2017/6/22/0022.
 */

public class FragmentAllMusic extends Fragment {
    private ArrayList<MusicInfo> music_all = new ArrayList<MusicInfo>();
    private ArrayList<MusicInfo> music_temp = new ArrayList<MusicInfo>();

    private View view;
    private RecyclerViewAdapter adapter;
    private RecyclerView show_all_music_list;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar fragment_all_music_toolbar;
    private AppBarLayout appBarLayout;
    private TextView all_music_title, show_all_list_size;
    private ImageView all_music_show_album_image;


    private int last_click_position = 0;
    private boolean is_update_image = true;

    private Palette.Swatch image_color;

    private FragmentAllMusicReceiver mReceiver;
    private IntentFilter mFilter;

    private String sd_path = Environment.getExternalStorageDirectory().getPath();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123456) {
                if (music_all.size() > 0) {
                    getMusicAlbumImage();
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_all_music_layout, null);
        ImageView back_last_fragment01 = (ImageView) view.findViewById(R.id.all_music_back_last_fragment01);
        ImageView back_last_fragment02 = (ImageView) view.findViewById(R.id.all_music_back_last_fragment02);
        ImageView fragment_all_music_modify = (ImageView) view.findViewById(R.id.fragment_all_music_modify);
        show_all_list_size = (TextView) view.findViewById(R.id.show_all_list_size);
        all_music_show_album_image = (ImageView) view.findViewById(R.id.all_music_show_album_image);

        music_all = PublicDate.public_music_all;
        show_all_list_size.setText((music_all.size()) + "首歌");

        back_last_fragment01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        back_last_fragment02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        fragment_all_music_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublicDate.public_music_edit_temp = music_all;
                startActivity(new Intent(getActivity(), MusicEditNeedListActivity.class).putExtra("is_all_music_01",true).putExtra("edit_name_intent", all_music_title.getText().toString()));
            }
        });


        //动态注册广播
        mReceiver = new FragmentAllMusicReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao.SelectMusicService");
        getActivity().registerReceiver(mReceiver, mFilter);


        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
        fragment_all_music_toolbar = (Toolbar) view.findViewById(R.id.fragment_all_music_toolbar);
        appBarLayout = (AppBarLayout) view.findViewById(R.id.appbarlayout);
        all_music_title = (TextView) view.findViewById(R.id.all_music_title);
//        list_view = (ListView) findViewById(R.id.list_view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(fragment_all_music_toolbar);
        collapsingToolbarLayout.setTitle(" ");
        //collapsingToolbarLayout.setContentScrimResource  设置过滤颜色
        collapsingToolbarLayout.setContentScrimResource(R.color.back_ground);
        fragment_all_music_toolbar.setBackgroundResource(R.color.back_ground);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int scrollRangle = appBarLayout.getTotalScrollRange();
                //初始verticalOffset为0，不能参与计算。
                if (verticalOffset == 0) {
                    all_music_title.setAlpha(0.0f);
                } else {
                    //保留一位小数
                    float alpha = Math.abs(Math.round(1.0f * verticalOffset / scrollRangle) * 10) / 10;
                    all_music_title.setAlpha(alpha);
                    fragment_all_music_toolbar.setAlpha(alpha);
                }
            }
        });

        if (music_all != null && music_all.size() > 0) {
            show_all_music_list = (RecyclerView) view.findViewById(R.id.show_all_music);
            // 创建默认的线性LayoutManager
            show_all_music_list.setLayoutManager(new LinearLayoutManager(getActivity()));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            show_all_music_list.setHasFixedSize(true);
            // 创建并设置Adapter
            adapter = new RecyclerViewAdapter(music_all);
            adapter.setOnItemClickListener(new RecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件
                    music_all.get(last_click_position).setIs_playing(false);
                    music_all.get(position).setIs_playing(true);
                    adapter.notifyItemChanged(last_click_position);//刷新单个数据
                    adapter.notifyItemChanged(position);
                    last_click_position = position;
                }
            });
            adapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    //item长按事件
                    ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                    music_edit_select.add(position);
                    PublicDate.public_music_edit_temp = music_all;
                    PublicDate.public_music_edit_temp_select = music_edit_select;
                    getActivity().startActivity(new Intent(getActivity(), MusicEditItemLongActivity.class).putExtra("is_all_music_01",true));
                }
            });
            show_all_music_list.setAdapter(adapter);

            //RecyclerView设置自适应高度，原理：用屏幕的高度-toolbar的高度-activity底部控件的高度-状态栏的高度
            int view_w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int view_h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            fragment_all_music_toolbar.measure(view_w, view_h);
            int view_height = fragment_all_music_toolbar.getMeasuredHeight();
            WindowManager wm = getActivity().getWindowManager();
            int mwidth = wm.getDefaultDisplay().getWidth();
            int mheight = wm.getDefaultDisplay().getHeight();
            ViewGroup.LayoutParams lp = show_all_music_list.getLayoutParams();
            int statusBarHeight1 = -1;
            //获取status_bar_height资源的ID
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
            }
            lp.height = (mheight - view_height - PublicDate.public_drawer_center_bottom_view_height-statusBarHeight1);//单位是像素，不是dp
            show_all_music_list.setLayoutParams(lp);



        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (is_update_image) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(0x123456);
                }
            }, 500);
            is_update_image = false;
        }
    }

    public void getMusicAlbumImage() {
        int music_album_num = 0;
        while (music_album_num < music_all.size()) {

            long songid = music_all.get(music_album_num).getMusic_id();
            long albumid = music_all.get(music_album_num).getMusic_album_id();
            Bitmap bitmap = MusicUtils.getArtwork(getActivity(), songid, albumid, true);
            if (bitmap != null) {
                setImageViewImage(all_music_show_album_image, bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        //获取专辑图片中的柔和亮色
                        image_color = palette.getMutedSwatch();
                        if (image_color != null) {
                            collapsingToolbarLayout.setContentScrimColor(image_color.getRgb());
                            fragment_all_music_toolbar.setBackgroundColor(image_color.getRgb());
                        }
                    }
                });
                break;
            }
            music_album_num--;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (music_all != null && music_all.size() > 0) {
            music_all.get(last_click_position).setIs_playing(false);
            adapter.notifyItemChanged(last_click_position);
        }
        getActivity().unregisterReceiver(mReceiver);
    }

    // 给一个ImageView设置高斯模糊的图片,并带有渐变
    public void setImageViewImage(ImageView image_view, Bitmap image_bitmap) {
        /*
         * 增大scaleRatio缩放比，使用一样更小的bitmap去虚化可以得到更好的模糊效果，而且有利于占用内存的减小；
		 * 增大blurRadius，可以得到更高程度的虚化，不过会导致CPU更加intensive
		 */
        int scaleRatio = PublicDate.scaleRatio;
        int blurRadius = PublicDate.blurRadius;
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(image_bitmap,
                image_bitmap.getWidth() / scaleRatio, image_bitmap.getHeight()
                        / scaleRatio, false);
        Bitmap blurBitmap = FastBlurUtil.doBlur(scaledBitmap, blurRadius, true);
        image_view.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Drawable start_drawable = getResources().getDrawable(R.color.fengexian_color);//渐变前的Drawable
        Drawable end_drawable = new BitmapDrawable(blurBitmap);//渐变后的Drawable，bitmap转drawable
        TransitionDrawable mTransitionDrawable = new TransitionDrawable(new Drawable[]{
                start_drawable,
                end_drawable
        });
        mTransitionDrawable.setCrossFadeEnabled(true);
        mTransitionDrawable.startTransition(800);//渐变过程持续时间
        image_view.setImageDrawable(mTransitionDrawable);


        /*ByteArrayOutputStream baos = new ByteArrayOutputStream();
        blurBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes =  baos.toByteArray();
        Glide.with(getActivity()).load(bytes).into(image_view);*/


//        image_view.setImageBitmap(blurBitmap);

    }

    class FragmentAllMusicReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    boolean is_update = intent.getBooleanExtra("is_update", false);
                    if (is_update) {
                        music_all = PublicDate.public_music_all;
                        adapter.notifyDataSetChanged();
                        show_all_list_size.setText((music_all.size()) + "首歌");
                        handler.sendEmptyMessage(0x123456);
                    }
                    break;
            }
        }
    }
    public ArrayList<HashMap<String, Object>> listToFolder(ArrayList<MusicInfo> list_temp) {
        listSortFolder(list_temp);
        ArrayList<HashMap<String, Object>> list_folder_all_temp = new ArrayList<>();
        ArrayList<HashMap<String, Object>> list_folder = new ArrayList<>();
        for (int i = 0; i < list_temp.size(); i++) {
            String temp = list_temp.get(i).getMusic_path();
            String temp002 = "";

            temp = getStringFolder(temp);
            if (i == 0) {
                HashMap<String, Object> folder_map = new HashMap<>();
                folder_map.put("folder_name", temp);
                folder_map.put("start_position", 0);
                list_folder.add(folder_map);
            } else {
                temp002 = getStringFolder(list_temp.get(i - 1).getMusic_path());
                if (!temp.equals(temp002)) {
                    HashMap<String, Object> folder_map = new HashMap<>();
                    folder_map.put("folder_name", temp);
                    folder_map.put("start_position", i);
                    list_folder.add(folder_map);
                }
            }
        }

        for (int i = 0; i < list_folder.size(); i++) {
            int start_position = (int) list_folder.get(i).get("start_position");
            int end_position = -1;
            if (i == list_folder.size() - 1) {
                end_position = list_temp.size();
            } else {
                end_position = (int) list_folder.get(i + 1).get("start_position");
            }
            HashMap<String, Object> map_temp = new HashMap<>();
            ArrayList<MusicInfo> folder_list_temp = new ArrayList<>();
            for (int j = start_position; j < end_position; j++) {
                folder_list_temp.add((MusicInfo) list_temp.get(j));
            }
            map_temp.put("folder_name", getInfoString(list_folder.get(i).get("folder_name").toString()));
            listSortPinYin(folder_list_temp);
            map_temp.put("folder_name_list", folder_list_temp);
            map_temp.put("folder_name_path", list_folder.get(i).get("folder_name").toString());
            list_folder_all_temp.add(map_temp);
        }

        return list_folder_all_temp;
    }

    // 自定义的排序
    public static void listSortPinYin(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getMusic_pinyin();
                String name2 = o2.getMusic_pinyin();
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);
            }
        });
    }

    public String getStringFolder(String str) {
        String string = str;
        if (str.indexOf("/") > -1) {
            string = str.substring(0, str.lastIndexOf("/"));
        }
        return string;
    }

    public String getInfoString(String string) {
        if (string.equals(sd_path)) {
            return "根目录";
        } else if (string.toLowerCase().indexOf("12530") > -1) {
            return "咪咕音乐";
        } else if (string.toLowerCase().indexOf("music") > -1 && string.toLowerCase().indexOf("baidu") > -1) {
            return "百度音乐";
        } else if (string.toLowerCase().indexOf("kgmusic") > -1) {
            return "酷狗音乐";
        } else if (string.toLowerCase().indexOf("kuwomusic") > -1) {
            return "酷我音乐";
        } else if (string.toLowerCase().indexOf("cloudmusic") > -1) {
            return "网易云音乐";
        } else if (string.toLowerCase().indexOf("qqmusic") > -1) {
            return "QQ音乐";
        } else if (string.toLowerCase().indexOf("xiami") > -1) {
            return "虾米音乐";
        } else {
            return string.substring(string.lastIndexOf("/") + "/".length());
        }

    }

    public void listSortFolder(ArrayList<MusicInfo> resultList) {
        Collections.sort(resultList, new Comparator<MusicInfo>() {
            public int compare(MusicInfo o1, MusicInfo o2) {
                String name1 = o1.getMusic_path() + "";
                String name2 = o2.getMusic_path() + "";
                Collator instance = Collator.getInstance(Locale.CHINA);
                return instance.compare(name1, name2);

            }
        });
    }
}
