package com.android.jiaqiao.UiFragment;

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

import com.android.jiaqiao.Activity.AllMusciAddToSheetActivity;
import com.android.jiaqiao.Activity.DeleteMusicSheetActivity;
import com.android.jiaqiao.Activity.MusicEditItemLongActivity;
import com.android.jiaqiao.Activity.MusicEditNeedListActivity;
import com.android.jiaqiao.Adapter.RecyclerViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.Utils.FastBlurUtil;
import com.android.jiaqiao.Utils.MusicPlayUtil;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;

/**
 * Created by jiaqiao on 2017/7/3/0003.
 */

public class FragmentMusicSheet extends Fragment {
    private SheetInfo show_sheet_info = null;

    private TextView show_sheet_name, show_sheet_list_size, music_sheet_title;
    private ImageView music_sheet_show_album_image;

    private String path = "";
    private ArrayList<MusicInfo> music_sheet_list = new ArrayList<>();

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar fragment_music_sheet_toolbar;
    private AppBarLayout appBarLayout;
    private RecyclerView show_music_sheet_recycler_view;
    private RecyclerViewAdapter adapter;

    private int last_click_position = 0;
    private boolean is_update_image = true;

    private Palette.Swatch image_color;

    private FragmentMusicSheetReceiver mReceiver;
    private IntentFilter mFilter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123456) {
                getMusicAlbumImage();
            }
        }
    };

    public void setShow_sheet_info(SheetInfo show_sheet_info) {
        this.show_sheet_info = show_sheet_info;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_music_sheet, null);
        show_sheet_name = (TextView) view.findViewById(R.id.music_sheet_name);
        music_sheet_title = (TextView) view.findViewById(R.id.music_sheet_title);
        show_sheet_list_size = (TextView) view.findViewById(R.id.show_all_list_size);
        show_music_sheet_recycler_view = (RecyclerView) view.findViewById(R.id.music_sheet_music);
        music_sheet_show_album_image = (ImageView) view.findViewById(R.id.music_sheet_show_album_image);

        ImageView back_last_fragment01 = (ImageView) view.findViewById(R.id.music_sheet_back_last_fragment01);
        ImageView back_last_fragment02 = (ImageView) view.findViewById(R.id.music_sheet_back_last_fragment02);
        ImageView music_sheet_delete = (ImageView) view.findViewById(R.id.fragment_music_sheet_delete);
        ImageView music_sheet_modify = (ImageView) view.findViewById(R.id.fragment_music_sheet_modify);
        ImageView music_sheet_add = (ImageView) view.findViewById(R.id.fragment_music_sheet_add);


        show_sheet_name.setText(show_sheet_info.getSheet_name().toString());
        music_sheet_title.setText(show_sheet_info.getSheet_name().toString());
        path = getPath(show_sheet_info.getSheet_id().toString());
        getMusicSheetToArrayList(path);

        show_sheet_list_size.setText(music_sheet_list.size() + "首歌");
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

        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
        fragment_music_sheet_toolbar = (Toolbar) view.findViewById(R.id.fragment_music_sheet_toolbar);
        appBarLayout = (AppBarLayout) view.findViewById(R.id.appbarlayout);
//        list_view = (ListView) findViewById(R.id.list_view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(fragment_music_sheet_toolbar);
        collapsingToolbarLayout.setTitle(" ");
        //collapsingToolbarLayout.setContentScrimResource  设置过滤颜色
        collapsingToolbarLayout.setContentScrimResource(R.color.back_ground);
        fragment_music_sheet_toolbar.setBackgroundResource(R.color.back_ground);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int scrollRangle = appBarLayout.getTotalScrollRange();
                //初始verticalOffset为0，不能参与计算。
                if (verticalOffset == 0) {
                    music_sheet_title.setAlpha(0.0f);
                } else {
                    //保留一位小数
                    float alpha = Math.abs(Math.round(1.0f * verticalOffset / scrollRangle) * 10) / 10;
                    music_sheet_title.setAlpha(alpha);
                    fragment_music_sheet_toolbar.setAlpha(alpha);
                }
            }
        });

        //动态注册广播
        mReceiver = new FragmentMusicSheetReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao");
        getActivity().registerReceiver(mReceiver, mFilter);

        // 创建默认的线性LayoutManager
        show_music_sheet_recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        show_music_sheet_recycler_view.setHasFixedSize(true);
        // 创建并设置Adapter
        adapter = new RecyclerViewAdapter(music_sheet_list);
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //item单击事件
                music_sheet_list.get(last_click_position).setIs_playing(false);
                music_sheet_list.get(position).setIs_playing(true);
                adapter.notifyItemChanged(last_click_position);//刷新单个数据
                adapter.notifyItemChanged(position);
                last_click_position = position;

                PublicDate.music_play_now = music_sheet_list.get(position);
                if(PublicDate.music_play_list_str==null||PublicDate.music_play_list_str.equals("")){
                    PublicDate.music_play_list_str=music_sheet_list.toString();
                    PublicDate.music_play = music_sheet_list;
                    MusicPlayUtil.saveMusicPlayList();
                    getActivity().getSharedPreferences(MainActivity.SHARED, 0).edit().putString("music_play_list_str",PublicDate.music_play_list_str).commit();

                }else{
                    if(!PublicDate.music_play_list_str.equals(music_sheet_list.toString())){
                        PublicDate.music_play_list_str=music_sheet_list.toString();
                        PublicDate.music_play = music_sheet_list;
                        MusicPlayUtil.saveMusicPlayList();
                        getActivity().getSharedPreferences(MainActivity.SHARED, 0).edit().putString("music_play_list_str",PublicDate.music_play_list_str).commit();

                    }
                }
                PublicDate.music_play_list_position = position;
                getActivity().getSharedPreferences(MainActivity.SHARED, 0).edit().putInt("music_play_list_position",PublicDate.music_play_list_position).commit();

                //发送广播
                Intent temp_intent = new Intent();
                temp_intent.setAction("com.android.jiaqiao");
                temp_intent.putExtra("type", MainActivity.UPDATE_MUSIC_PLAY);
                temp_intent.putExtra("is_update_music_play", true);
                getActivity().sendBroadcast(temp_intent);
            }
        });
        adapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnRecyclerItemLongListener() {
            @Override
            public void onItemLongClick(View view, int position) {
                //item长按事件
                ArrayList<Integer> music_edit_select = new ArrayList<Integer>();
                music_edit_select.add(position);
                PublicDate.public_music_edit_temp = music_sheet_list;
                PublicDate.public_music_edit_temp_select = music_edit_select;
                getActivity().startActivity(new Intent(getActivity(), MusicEditItemLongActivity.class).putExtra("music_sheet_id_01", show_sheet_info.getSheet_id().toString()));
            }
        });
        show_music_sheet_recycler_view.setAdapter(adapter);

        //RecyclerView设置自适应高度，原理：用屏幕的高度-toolbar的高度-activity底部控件的高度-状态栏的高度
        int view_w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int view_h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        fragment_music_sheet_toolbar.measure(view_w, view_h);
        int view_height = fragment_music_sheet_toolbar.getMeasuredHeight();
        WindowManager wm = getActivity().getWindowManager();
        int mwidth = wm.getDefaultDisplay().getWidth();
        int mheight = wm.getDefaultDisplay().getHeight();
        ViewGroup.LayoutParams lp = show_music_sheet_recycler_view.getLayoutParams();
        int statusBarHeight1 = -1;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
        lp.height = (mheight - view_height - PublicDate.public_drawer_center_bottom_view_height-statusBarHeight1);//单位是像素，不是dp
        show_music_sheet_recycler_view.setLayoutParams(lp);


        music_sheet_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DeleteMusicSheetActivity.class).putExtra("sheet_id", show_sheet_info.getSheet_id().toString()));
            }
        });
        music_sheet_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublicDate.public_music_edit_temp = music_sheet_list;
                startActivity(new Intent(getActivity(), MusicEditNeedListActivity.class).putExtra("edit_name_intent", music_sheet_title.getText().toString()).putExtra("music_sheet_id_01", show_sheet_info.getSheet_id().toString()));
            }
        });
        music_sheet_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublicDate.temp_sheet_info = show_sheet_info;
                startActivity(new Intent(getActivity(), AllMusciAddToSheetActivity.class));
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (PublicDate.delete_sheet_over) {
            getFragmentManager().popBackStack();
        }
    }

    public MusicInfo getFromListGetMusicInfo(String music_id, String music_title) {
        ArrayList<MusicInfo> list_temp = PublicDate.public_music_all;

        for (int i = 0; i < list_temp.size(); i++) {
            if ((list_temp.get(i).getMusic_id() + "").equals(music_id) && list_temp.get(i).getMusic_title().equals(music_title)) {
                return list_temp.get(i);
            }
        }
        return null;
    }

    public void getMusicSheetToArrayList(String path_name) {
        try {
            FileReader fr = new FileReader(path_name);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (temp.length() > 0 && temp.indexOf(separate_str) > -1) {
                    String[] music_temp = temp.split(PublicDate.separate_str);
                    if (music_temp.length >= 2) {
                        MusicInfo music_info_temp = getFromListGetMusicInfo(music_temp[0], music_temp[1]);
                        if (music_info_temp != null) {
                            music_sheet_list.add(music_info_temp);
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

    }

    public String getPath(String sheet_id) {
        File file = new File(getActivity().getFilesDir() + "/music_sheet_list");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (music_sheet_list != null && music_sheet_list.size() > 0) {
            music_sheet_list.get(last_click_position).setIs_playing(false);
            adapter.notifyItemChanged(last_click_position);//刷新单个数据
        }
        getActivity().unregisterReceiver(mReceiver);
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
        while (music_album_num < music_sheet_list.size()) {

            long songid = music_sheet_list.get(music_album_num).getMusic_id();
            long albumid = music_sheet_list.get(music_album_num).getMusic_album_id();
            Bitmap bitmap = MusicUtils.getArtwork(getActivity(), songid, albumid, true);
            if (bitmap != null) {
                setImageViewImage(music_sheet_show_album_image, bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        //获取专辑图片中的柔和亮色
                        image_color = palette.getMutedSwatch();
                        if (image_color != null) {
                            collapsingToolbarLayout.setContentScrimColor(image_color.getRgb());
                            fragment_music_sheet_toolbar.setBackgroundColor(image_color.getRgb());
                        }
                    }
                });
                break;
            }
            music_album_num++;
        }

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

        image_view.setDrawingCacheEnabled(true);
        Bitmap image_view_bitmap = image_view.getDrawingCache();
        Drawable start_drawable = new BitmapDrawable(image_view_bitmap);//渐变前的Drawable
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

    class FragmentMusicSheetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    boolean is_update = intent.getBooleanExtra("is_update", false);
                    if (is_update) {
                        music_sheet_list.clear();
                        getMusicSheetToArrayList(path);
                        adapter.notifyDataSetChanged();
                        show_sheet_list_size.setText(music_sheet_list.size() + "首歌");
                        handler.sendEmptyMessage(0x123456);
                    }
                    break;
                case MainActivity.UPDATE_SHEET:
                    boolean is_update_sheet = intent.getBooleanExtra("is_update_sheet", false);
                    if (is_update_sheet) {
                        music_sheet_list.clear();
                        getMusicSheetToArrayList(path);
                        adapter.notifyDataSetChanged();
                        show_sheet_list_size.setText(music_sheet_list.size() + "首歌");
                        handler.sendEmptyMessage(0x123456);
                    }
                    break;
            }
        }

    }
}
