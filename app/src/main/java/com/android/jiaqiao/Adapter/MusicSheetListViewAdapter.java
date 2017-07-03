package com.android.jiaqiao.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;


/**
 * Created by jiaqiao on 2017/6/22/0022.
 */

public class MusicSheetListViewAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<MusicInfo> list;

    public MusicSheetListViewAdapter(Context mContext, ArrayList<MusicInfo> list) {
        this.mContext = mContext;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.show_music_sheet_list, null);
            holder = new ViewHolder();
            holder.music_title = (TextView) view.findViewById(R.id.music_title);
            holder.music_artist_album = (TextView) view.findViewById(R.id.music_artist_album);
            holder.music_is_selected =(ImageView) view.findViewById(R.id.music_is_selected);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        MusicInfo music_temp = list.get(position);
        holder.music_title.setText(music_temp.getMusic_title());
        String artist_album_str = "";
        if (music_temp.getMusic_artist() != "" && music_temp.getMusic_artist().length() > 0) {
            artist_album_str += music_temp.getMusic_artist();
        }
        if (music_temp.getMusic_album() != "" && music_temp.getMusic_album().length() > 0) {
            artist_album_str += " - " + music_temp.getMusic_album();
        }
        holder.music_artist_album.setText(artist_album_str);

        if(music_temp.is_selected()){
            holder.music_is_selected.setImageResource(R.drawable.is_selected);
        }else{
            holder.music_is_selected.setImageResource(R.drawable.not_selected);
        }

        return view;
    }
    public class ViewHolder
    {
        public TextView music_title;
        public TextView music_artist_album;
        public ImageView music_is_selected;
    }

    //更新ListView中单个Item
    public void updataView(int position, ListView listView) {
        int visibleFirstPosi = listView.getFirstVisiblePosition();
        int visibleLastPosi = listView.getLastVisiblePosition();
        if (position >= visibleFirstPosi && position <= visibleLastPosi) {
            View view = listView.getChildAt(position - visibleFirstPosi);
            ViewHolder holder = (ViewHolder) view.getTag();

            MusicInfo music_temp = list.get(position);
            holder.music_title.setText(music_temp.getMusic_title());
            String artist_album_str = "";
            if (music_temp.getMusic_artist() != "" && music_temp.getMusic_artist().length() > 0) {
                artist_album_str += music_temp.getMusic_artist();
            }
            if (music_temp.getMusic_album() != "" && music_temp.getMusic_album().length() > 0) {
                artist_album_str += " - " + music_temp.getMusic_album();
            }
            holder.music_artist_album.setText(artist_album_str);

            if(music_temp.is_selected()){
                holder.music_is_selected.setImageResource(R.drawable.is_selected);
            }else{
                holder.music_is_selected.setImageResource(R.drawable.not_selected);
            }

        }
    }
}
