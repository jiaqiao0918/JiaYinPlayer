package com.android.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.jiaqiao.Activity.DeleteMusicSheetActivity;
import com.android.jiaqiao.Adapter.RecyclerViewAdapter;
import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.Utils.FastBlurUtil;
import com.android.jiaqiao.Utils.MusicUtils;
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

import static com.android.jiaqiao.jiayinplayer.PublicDate.music_all;
import static com.android.jiaqiao.jiayinplayer.PublicDate.separate_str;
import static com.android.jiaqiao.jiayinplayer.R.id.all_music_show_album_image;

/**
 * Created by jiaqiao on 2017/7/3/0003.
 */

public class FragmentMusicSheet extends Fragment {
    private SheetInfo show_sheet_info = null;

    private TextView show_sheet_name, show_sheet_list_size,music_sheet_title;
    private ImageView music_sheet_show_album_image;

    private String path = "";
    private ArrayList<MusicInfo> music_sheet_list = new ArrayList<>();

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar fragment_music_sheet_toolbar;
    private AppBarLayout appBarLayout;
    private RecyclerView show_music_sheet_recycler_view;
    private RecyclerViewAdapter adapter;

    private int last_click_position = 0;

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

        ImageButton back_last_fragment01 = (ImageButton) view.findViewById(R.id.music_sheet_back_last_fragment01);
        ImageButton back_last_fragment02 = (ImageButton) view.findViewById(R.id.music_sheet_back_last_fragment02);
        LinearLayout music_sheet_delete = (LinearLayout) view.findViewById(R.id.music_sheet_delete);
        LinearLayout music_sheet_modify = (LinearLayout) view.findViewById(R.id.music_sheet_modify);
        LinearLayout music_sheet_add = (LinearLayout) view.findViewById(R.id.music_sheet_add);

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


        if (music_sheet_list != null && music_sheet_list.size() > 0) {
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
                }
            });
            adapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnRecyclerItemLongListener() {
                @Override
                public void onItemLongClick(View view, int position) {
                    //item长按事件
                    music_sheet_list.remove(position);
//                  adapter.notifyItemRemoved(position);//不会刷新RecyclerView的高度
                    adapter.notifyDataSetChanged();
                    show_music_sheet_recycler_view.startLayoutAnimation();//重新开始LayoutAnimation

                }
            });
            show_music_sheet_recycler_view.setAdapter(adapter);


        }

        music_sheet_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), DeleteMusicSheetActivity.class).putExtra("sheet_id", show_sheet_info.getSheet_id().toString()));
            }
        });
        music_sheet_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "编辑按钮", Toast.LENGTH_SHORT).show();
            }
        });
        music_sheet_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "添加按钮", Toast.LENGTH_SHORT).show();
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
        ArrayList<MusicInfo> list_temp = music_all;

        for (int i = 0; i < list_temp.size(); i++) {
            if ((list_temp.get(i).getMusic_id()+"").equals(music_id) && list_temp.get(i).getMusic_title().equals(music_title)) {
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
    }
    @Override
    public void onStart() {
        super.onStart();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123456);
            }
        }, 500);
    }

    public void getMusicAlbumImage() {
        int music_album_num = 0;
        while (music_album_num < music_sheet_list.size()) {

            long songid = music_sheet_list.get(music_album_num).getMusic_id();
            long albumid = music_sheet_list.get(music_album_num).getMusic_album_id();
            Bitmap bitmap = MusicUtils.getArtwork(getActivity(), songid, albumid, true);
            if (bitmap != null) {
                setImageViewImage(music_sheet_show_album_image,bitmap);
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        //获取专辑图片中的柔和亮色
                        image_color=palette.getMutedSwatch();
                        if(image_color!=null){
                            collapsingToolbarLayout.setContentScrimColor(image_color.getRgb());
                            fragment_music_sheet_toolbar.setBackgroundColor(image_color.getRgb());
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
        int scaleRatio = 20;
        int blurRadius = 3;
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
}
