package com.android.jiaqiao.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.jiaqiao.JavaBean.SheetInfo;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;


/**
 * Created by jiaqiao on 2017/6/21/0021.
 */

public class MusicSheetAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<SheetInfo> list;

    public MusicSheetAdapter(Context mContext, ArrayList<SheetInfo> list) {
        this.mContext = mContext;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list==null?0:list.size();
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
        view = LayoutInflater.from(mContext).inflate(R.layout.music_list_item,null);
        TextView show_list_item_text = (TextView) view.findViewById(R.id.show_list_item_text);
        show_list_item_text.setText(list.get(position).getSheet_name().toString());
        return view;
    }
}
