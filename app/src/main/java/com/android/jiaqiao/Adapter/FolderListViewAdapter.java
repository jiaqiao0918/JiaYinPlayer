package com.android.jiaqiao.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class FolderListViewAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();

    public FolderListViewAdapter(Context mContext, ArrayList<HashMap<String, Object>> list_folder_all) {
        this.mContext = mContext;
        this.list_folder_all = list_folder_all;
    }

    @Override
    public int getCount() {
        return list_folder_all==null?0:list_folder_all.size();
    }

    @Override
    public Object getItem(int position) {
        return list_folder_all.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = null;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.fragment_folder_for_music_list_item, null);

            holder = new ViewHolder();
            holder.folder_name = (TextView) view.findViewById(R.id.folder_name);
            holder.folder_count = (TextView) view.findViewById(R.id.folder_count);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.folder_name.setText(list_folder_all.get(position).get("folder_name").toString());
        holder.folder_count.setText(((ArrayList<MusicInfo>)list_folder_all.get(position).get("folder_name_list")).size()+"首");
        return view;
    }
    public class ViewHolder
    {
        public TextView folder_name;
        public TextView folder_count;
    }
}
