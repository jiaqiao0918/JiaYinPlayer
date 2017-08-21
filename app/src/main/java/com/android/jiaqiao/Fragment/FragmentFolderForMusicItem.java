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

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class FragmentFolderForMusicItem extends Fragment {
    private String folder_name_str = "";
    private int folder_list_position = 0;
    private String folder_name_path = "";

    private ArrayList<MusicInfo> folder_list = new ArrayList<MusicInfo>();

    private int last_click_position = 0;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar fragment_folder_music_item_toolbar;
    private TextView folder_name_tittle;
    private ImageView folder_music_item_show_album_image;

    private RecyclerViewAdapter adapter;
    private RecyclerView show_folder_list;
    private TextView show_folder_size;

    private Palette.Swatch image_color;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x123456) {
                getMusicAlbumImage();
            }
        }
    };
    private boolean is_update_image = true;

    private FragmentFolderForMusicItemReceiver mReceiver;
    private IntentFilter mFilter;

    public void setValues(String folder_name_str, int folder_list_position, String folder_name_time) {
        this.folder_name_str = folder_name_str;
        this.folder_list_position = folder_list_position;
        this.folder_name_path = folder_name_time;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_for_music_item_layout, null);
        TextView folder_name = (TextView) view.findViewById(R.id.folder_name);
        show_folder_size = (TextView) view.findViewById(R.id.show_folder_size);
        ImageView back_last_fragment01 = (ImageView) view.findViewById(R.id.folder_music_item_back_last_fragment01);
        ImageView back_last_fragment02 = (ImageView) view.findViewById(R.id.folder_music_item_back_last_fragment02);
        ImageView fragment_folder_for_music_item_modify = (ImageView) view.findViewById(R.id.fragment_folder_for_music_item_modify);
        show_folder_list = (RecyclerView) view.findViewById(R.id.show_folder_list);
        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar_layout);
        fragment_folder_music_item_toolbar = (Toolbar) view.findViewById(R.id.fragment_folder_music_item_toolbar);
        AppBarLayout appBarLayout = (AppBarLayout) view.findViewById(R.id.appbarlayout);
        folder_name_tittle = (TextView) view.findViewById(R.id.folder_name_tittle);
        folder_music_item_show_album_image = (ImageView) view.findViewById(R.id.folder_music_item_show_album_image);

        folder_list = (ArrayList<MusicInfo>) PublicDate.list_folder_all.get(folder_list_position).get("folder_name_list");
        show_folder_size.setText(folder_list.size() + "首歌");
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
        fragment_folder_for_music_item_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PublicDate.public_music_edit_temp = folder_list;
                startActivity(new Intent(getActivity(), MusicEditNeedListActivity.class).putExtra("is_all_music_01", true).putExtra("edit_name_intent", folder_name_tittle.getText().toString()));
            }
        });
        if (folder_name_str.length() > 0) {
            folder_name.setText(folder_name_str);
            folder_name_tittle.setText(folder_name_str);
        }

        ((AppCompatActivity) getActivity()).setSupportActionBar(fragment_folder_music_item_toolbar);
        collapsingToolbarLayout.setTitle(" ");
        //collapsingToolbarLayout.setContentScrimResource  设置过滤颜色
        collapsingToolbarLayout.setContentScrimResource(R.color.back_ground);
        fragment_folder_music_item_toolbar.setBackgroundResource(R.color.back_ground);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int scrollRangle = appBarLayout.getTotalScrollRange();
                //初始verticalOffset为0，不能参与计算。
                if (verticalOffset == 0) {
                    folder_name_tittle.setAlpha(0.0f);
                } else {
                    //保留一位小数
                    float alpha = Math.abs(Math.round(1.0f * verticalOffset / scrollRangle) * 10) / 10;
                    folder_name_tittle.setAlpha(alpha);
                    fragment_folder_music_item_toolbar.setAlpha(alpha);
                }
            }
        });

        //动态注册广播
        mReceiver = new FragmentFolderForMusicItemReceiver();
        mFilter = new IntentFilter();
        mFilter.addAction("com.android.jiaqiao.SelectMusicService");
        getActivity().registerReceiver(mReceiver, mFilter);

        update_date();

        return view;
    }

    public void update_date() {
        if (folder_list != null && folder_list.size() > 0) {
            // 创建默认的线性LayoutManager
            show_folder_list.setLayoutManager(new LinearLayoutManager(getActivity()));
            // 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
            show_folder_list.setHasFixedSize(true);
            // 创建并设置Adapter
            adapter = new RecyclerViewAdapter(folder_list);
            adapter.setOnItemClickListener(new RecyclerViewAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    //item单击事件
                    folder_list.get(last_click_position).setIs_playing(false);
                    folder_list.get(position).setIs_playing(true);
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
                    PublicDate.public_music_edit_temp = folder_list;
                    PublicDate.public_music_edit_temp_select = music_edit_select;
                    getActivity().startActivity(new Intent(getActivity(), MusicEditItemLongActivity.class).putExtra("is_all_music_01", true));
                }
            });
            show_folder_list.setAdapter(adapter);

            //RecyclerView设置自适应高度，原理：用屏幕的高度-toolbar的高度-activity底部控件的高度-状态栏的高度
            int view_w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            int view_h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            fragment_folder_music_item_toolbar.measure(view_w, view_h);
            int view_height = fragment_folder_music_item_toolbar.getMeasuredHeight();
            WindowManager wm = getActivity().getWindowManager();
            int mwidth = wm.getDefaultDisplay().getWidth();
            int mheight = wm.getDefaultDisplay().getHeight();
            ViewGroup.LayoutParams lp = show_folder_list.getLayoutParams();
            int statusBarHeight1 = -1;
            //获取status_bar_height资源的ID
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                //根据资源ID获取响应的尺寸值
                statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
            }
            lp.height = (mheight - view_height - PublicDate.public_drawer_center_bottom_view_height - statusBarHeight1);//单位是像素，不是dp
            show_folder_list.setLayoutParams(lp);


        } else {
            getFragmentManager().popBackStack();
        }
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (folder_list != null && folder_list.size() > 0) {
            folder_list.get(last_click_position).setIs_playing(false);
            adapter.notifyItemChanged(last_click_position);//刷新单个数据
        }
        getActivity().unregisterReceiver(mReceiver);
    }

    public void getMusicAlbumImage() {
        int music_album_num = 0;
        while (music_album_num < folder_list.size()) {

            long songid = folder_list.get(music_album_num).getMusic_id();
            long albumid = folder_list.get(music_album_num).getMusic_album_id();
            Bitmap bitmap = MusicUtils.getArtwork(getActivity(), songid, albumid, true);
            if (bitmap != null) {
                setImageViewImage(folder_music_item_show_album_image, bitmap);
//                folder_music_item_show_album_image.setImageBitmap(bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        //获取专辑图片中的亮色
                        image_color = palette.getMutedSwatch();
                        if (image_color != null) {
                            collapsingToolbarLayout.setContentScrimColor(image_color.getRgb());
                            fragment_folder_music_item_toolbar.setBackgroundColor(image_color.getRgb());
                        }
                    }
                });
                break;
            }
            music_album_num--;
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

        Drawable start_drawable = getResources().getDrawable(R.color.fengexian_color);//渐变前的Drawable
        Drawable end_drawable = new BitmapDrawable(blurBitmap);//渐变后的Drawable，bitmap转drawable
        TransitionDrawable mTransitionDrawable = new TransitionDrawable(new Drawable[]{
                start_drawable,
                end_drawable
        });
        mTransitionDrawable.setCrossFadeEnabled(true);
        mTransitionDrawable.startTransition(800);//渐变过程持续时间
        image_view.setImageDrawable(mTransitionDrawable);
    }

    class FragmentFolderForMusicItemReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra("type", -1);
            switch (type) {
                case MainActivity.ALL_MUSIC_UPDATE:
                    boolean is_update = intent.getBooleanExtra("is_update", false);
                    if (is_update) {
                        boolean is_have_folder_name = false;
                        for (int i = 0; i < PublicDate.list_folder_all.size(); i++) {
                            if (PublicDate.list_folder_all.get(i).get("folder_name_path").equals(folder_name_path)) {
                                is_have_folder_name = true;
                                break;
                            } else {
                                is_have_folder_name = false;
                            }
                        }
                        if (is_have_folder_name) {
                            if (PublicDate.list_folder_all.get(folder_list_position).get("folder_name_path").equals(folder_name_path)) {
                                folder_list = (ArrayList<MusicInfo>) PublicDate.list_folder_all.get(folder_list_position).get("folder_name_list");
                                show_folder_size.setText(folder_list.size() + "首歌");
                                update_date();
                                handler.sendEmptyMessage(0x123456);
                            } else {
                                getFragmentManager().popBackStack();
                            }
                        }else{
                            getFragmentManager().popBackStack();
                        }
                    }
                    break;
            }
        }

    }
}
