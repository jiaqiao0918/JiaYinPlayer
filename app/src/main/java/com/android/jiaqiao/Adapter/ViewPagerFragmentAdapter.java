package com.android.jiaqiao.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;

/**
 * Created by jiaqiao on 2017/8/21/0021.
 */

public class ViewPagerFragmentAdapter extends FragmentStatePagerAdapter {

    private FragmentManager fragmentmanager;  //创建FragmentManager
    private ArrayList<Fragment> list_fragment; //创建一个List<Fragment>
    // 定义构造带两个参数
    public ViewPagerFragmentAdapter(FragmentManager fm, ArrayList<Fragment> list) {
        super(fm);
        this.fragmentmanager =fm;
        this.list_fragment =list;
    }

    @Override
    public Fragment getItem(int arg0) {
        return list_fragment.get(arg0); //返回第几个fragment
    }

    @Override
    public int getCount() {
        return list_fragment.size(); //总共有多少个fragment
    }

    //更新ViewPager，需要继承于FragmentStatePagerAdapter，FragmentAdapter将数据直接写入内存会刷新不成功
    public void UpdateList(ArrayList<Fragment> arrayList) {
        this.list_fragment=arrayList;
        notifyDataSetChanged();
    }
    //解决notifyDataSetChanged，不刷新
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

}