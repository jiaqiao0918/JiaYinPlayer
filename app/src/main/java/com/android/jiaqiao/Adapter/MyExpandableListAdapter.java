package com.android.jiaqiao.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;


/**
 * Created by jiaqiao on 2017/6/24/0024.
 */

public class MyExpandableListAdapter extends BaseExpandableListAdapter {
    private ArrayList<String> parent_list;
    private ArrayList<ArrayList<MusicInfo>> child_list;
    private Context mContext;

    public MyExpandableListAdapter(ArrayList<String> parent_list, ArrayList<ArrayList<MusicInfo>> child_list, Context mContext) {
        this.parent_list = parent_list;
        this.child_list = child_list;
        this.mContext = mContext;
    }

    //获取子元素对象
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return child_list.get(childPosition);
    }

    //获取子元素Id
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //加载子元素并显示
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View view = null;
        ChildHolder childholder = null;
        if (convertView != null) {
            view = convertView;
            childholder = (ChildHolder) view.getTag();
        } else {
            view = View.inflate(mContext, R.layout.expandable_child_layout, null);
            childholder = new ChildHolder();
            childholder.music_is_playing = (ImageView) view.findViewById(R.id.music_is_playing);
            childholder.music_title = (TextView) view.findViewById(R.id.music_title);
            childholder.music_artist_album = (TextView) view.findViewById(R.id.music_artist_album);
            view.setTag(childholder);
        }

        MusicInfo music_temp = child_list.get(groupPosition).get(childPosition);
        childholder.music_title.setText(music_temp.getMusic_title());
        String artist_album_str = "";
        if (music_temp.getMusic_artist() != "" && music_temp.getMusic_artist().length() > 0) {
            artist_album_str += music_temp.getMusic_artist();
        }
        if (music_temp.getMusic_album() != "" && music_temp.getMusic_album().length() > 0) {
            artist_album_str += " - " + music_temp.getMusic_album();
        }
        childholder.music_artist_album.setText(artist_album_str);

        if (music_temp.is_playing()) {
            childholder.music_is_playing.setVisibility(View.VISIBLE);
        } else {
            childholder.music_is_playing.setVisibility(View.INVISIBLE);
        }


        return view;
    }

    //获取子元素数目
    @Override
    public int getChildrenCount(int groupPosition) {
        return child_list.get(groupPosition).size();
    }

    //获取组元素对象
    @Override
    public Object getGroup(int groupPosition) {
        return parent_list.get(groupPosition);
    }

    //获取组元素数目
    @Override
    public int getGroupCount() {
        return parent_list == null ? 0 : child_list.size();
    }

    //获取组元素Id
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //加载并显示组元素
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View view = null;
        GroupHolder groupholder = null;
        if (convertView != null) {
            view = convertView;
            groupholder = (GroupHolder) view.getTag();
        } else {
            view = View.inflate(mContext, R.layout.expandable_parent_layout, null);
            groupholder = new GroupHolder();
            groupholder.expandable_parent_text = (TextView) view.findViewById(R.id.expandable_parent_text);
            view.setTag(groupholder);
        }
        groupholder.expandable_parent_text.setText(parent_list.get(groupPosition));

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        //返回false，则无法响应ExpandableListView的OnChildClickListener事件
        return true;
    }

    //更新ListView中单个Item
    public void updataView(ExpandableListView expandable_list_view, int groupPosition, int childPosition) {
        View view = getView(expandable_list_view,groupPosition,childPosition);
        if (view != null) {
/*
* 判断view是否可见
* 判断原理，getFirstVisiblePosition，getLastVisiblePosition，获取可见项的最小序号和最大序号
* 获取当前点击的子项的真实位置，（从0开始排序）
* 当前子项的父项前几个父项之和+当前子项在子项所在的父项的位置+父项的位置+1
* */
            int group_position_sum = 0;
            for (int i = 0; i < groupPosition; i++) {
                group_position_sum += expandable_list_view.getExpandableListAdapter().getChildrenCount(i);
            }
            group_position_sum = group_position_sum + groupPosition + childPosition + 1;

            if (expandable_list_view.getFirstVisiblePosition() <= group_position_sum && expandable_list_view.getLastVisiblePosition() >= group_position_sum) {

                ChildHolder child_holder = (ChildHolder) view.getTag();

                MusicInfo music_temp = child_list.get(groupPosition).get(childPosition);
                child_holder.music_title.setText(music_temp.getMusic_title());
                String artist_album_str = "";
                if (music_temp.getMusic_artist() != "" && music_temp.getMusic_artist().length() > 0) {
                    artist_album_str += music_temp.getMusic_artist();
                }
                if (music_temp.getMusic_album() != "" && music_temp.getMusic_album().length() > 0) {
                    artist_album_str += " - " + music_temp.getMusic_album();
                }
                child_holder.music_artist_album.setText(artist_album_str);

                if (music_temp.is_playing()) {
                    child_holder.music_is_playing.setVisibility(View.VISIBLE);
                } else {
                    child_holder.music_is_playing.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public View getView(ExpandableListView expandable_list_view, int groupPosition, int childPosition) {
        int group_position_sum = 0;
        for (int i = 0; i < groupPosition; i++) {
            group_position_sum += expandable_list_view.getExpandableListAdapter().getChildrenCount(i);
        }
        group_position_sum = group_position_sum + groupPosition + childPosition + 1 - expandable_list_view.getFirstVisiblePosition();
        return expandable_list_view.getChildAt(group_position_sum);
    }


    class GroupHolder {
        TextView expandable_parent_text;
    }

    class ChildHolder {
        ImageView music_is_playing;
        TextView music_title;
        TextView music_artist_album;
    }
}
