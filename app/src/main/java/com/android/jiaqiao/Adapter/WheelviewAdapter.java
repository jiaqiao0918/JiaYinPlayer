package com.android.jiaqiao.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.android.jiaqiao.View.WheelView.AbstractWheelTextAdapter;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/8/29/0029.
 */

public class WheelviewAdapter extends AbstractWheelTextAdapter {
    ArrayList<String> list;

    public WheelviewAdapter(Context context, ArrayList<String> list) {
        super(context, R.layout.wheel_view_item, NO_RESOURCE, 0,14,14);
        this.list = list;
        setItemTextResource(R.id.tempValue);
    }

    @Override
    public View getItem(int index, View cachedView, ViewGroup parent) {
        View view = super.getItem(index, cachedView, parent);
        return view;
    }

    @Override
    public int getItemsCount() {
        return list.size();
    }

    @Override
    protected CharSequence getItemText(int index) {
        return list.get(index) + "";
    }
}
