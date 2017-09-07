package com.android.jiaqiao.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class SetFolderListViewAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<HashMap<String, Object>> list_folder_all = new ArrayList<HashMap<String, Object>>();

    public SetFolderListViewAdapter(Context mContext, ArrayList<HashMap<String, Object>> list_folder_all) {
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
            view = LayoutInflater.from(mContext).inflate(R.layout.set_folder_for_music_floder_item, null);

            holder = new ViewHolder();
            holder.folder_name = (TextView) view.findViewById(R.id.folder_name);
            holder.is_click_this = (ImageView) view.findViewById(R.id.is_click_this);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.folder_name.setText(list_folder_all.get(position).get("folder_name").toString());
        if((boolean)list_folder_all.get(position).get("is_click")){
            holder.is_click_this.setImageResource(R.drawable.is_selected);
        }else{
            holder.is_click_this.setImageResource(R.drawable.not_selected);
        }
        return view;
    }
    public class ViewHolder
    {
        public TextView folder_name;
        public ImageView is_click_this;
    }

    //更新ListView中单个Item
    public void updataView(int position, ListView listView) {
        int visibleFirstPosi = listView.getFirstVisiblePosition();
        int visibleLastPosi = listView.getLastVisiblePosition();
        if (position >= visibleFirstPosi && position <= visibleLastPosi) {
            View view = listView.getChildAt(position - visibleFirstPosi);
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.folder_name.setText(list_folder_all.get(position).get("folder_name").toString());
            if((boolean)list_folder_all.get(position).get("is_click")){
                holder.is_click_this.setImageResource(R.drawable.is_selected);
            }else{
                holder.is_click_this.setImageResource(R.drawable.not_selected);
            }
        }
    }


}
