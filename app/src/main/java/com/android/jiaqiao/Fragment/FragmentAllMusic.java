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
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jiaqiao.Adapter.RecyclerViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.Utils.FastBlurUtil;
import com.android.jiaqiao.Utils.MusicUtils;
import com.android.jiaqiao.jiayinplayer.MainActivity;
import com.android.jiaqiao.jiayinplayer.PublicDate;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123456) {
                if(music_all.size()>0) {
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

            }
        });
        music_all = PublicDate.music_all;
        show_all_list_size.setText((music_all.size()) + "首歌");

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
                    music_all.remove(position);
//                  adapter.notifyItemRemoved(position);//不会刷新RecycleView的高度
                    adapter.notifyDataSetChanged();
                    show_all_music_list.startLayoutAnimation();//重新开始LayoutAnimation

                }
            });
            show_all_music_list.setAdapter(adapter);
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
                        music_all = PublicDate.music_all;
                        adapter.notifyDataSetChanged();
                        show_all_list_size.setText((music_all.size()) + "首歌");
                        handler.sendEmptyMessage(0x123456);
                    }
                    break;
            }
        }

    }
}
