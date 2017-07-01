package jiayinplayer.example.jiaqiao.Fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jiayinplayer.example.jiaqiao.Adapter.TestAdapter;
import jiayinplayer.example.jiaqiao.Utils.FragmentTransactionExtended;
import jiayinplayer.example.jiaqiao.jiayinplayer.PublicDate;
import jiayinplayer.example.jiaqiao.jiayinplayer.R;


public class FragmentMain extends Fragment {
    private Context mContext;
    private ArrayList<String> list = new ArrayList<String>();


    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_layout, null);
        ScrollView scroll_view = (ScrollView) view.findViewById(R.id.scroll_view);
        ListView music_list_list = (ListView) view.findViewById(R.id.show_music_list);
        LinearLayout fragment_to_all_music = (LinearLayout) view.findViewById(R.id.fragment_to_all_music);
        LinearLayout fragment_to_date_for_music = (LinearLayout) view.findViewById(R.id.fragment_to_date_for_music);
        LinearLayout fragment_to_folder_for_music = (LinearLayout) view.findViewById(R.id.fragment_to_folder_for_music);
        TextView music_all_count = (TextView) view.findViewById(R.id.music_all_count);
        music_all_count.setText((PublicDate.music_all.size()) + "首");
        TestAdapter adapter = new TestAdapter(mContext, list);
        music_list_list.setAdapter(adapter);

        setListViewHeightBasedOnChildren(music_list_list);
        //设置scrollview初始化后滑动到顶部，必须在ListView填充数据之后，否则无法实现预期效果
//        scroll_view.smoothScrollTo(0,0);//滑动到顶部，两种方法都可行
        scroll_view.fullScroll(ScrollView.FOCUS_UP);//滑动到顶部

        music_list_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, list.get(position).toString(), Toast.LENGTH_SHORT).show();
            }
        });

        fragment_to_all_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentAllMusic fragment_all_music = new FragmentAllMusic();
                fragment_all_music.setContext(mContext);
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        mContext, fragmentTransaction, new FragmentMain(),
                        fragment_all_music, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });
        fragment_to_date_for_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentDateForMusic fragment_date_for_music = new FragmentDateForMusic();
                fragment_date_for_music.setContext(mContext);
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        mContext, fragmentTransaction, new FragmentMain(),
                        fragment_date_for_music, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });
        fragment_to_folder_for_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentFolderForMusic fragment_folder_for_music = new FragmentFolderForMusic();
                fragment_folder_for_music.setContext(mContext);
                FragmentTransaction fragmentTransaction = getFragmentManager()
                        .beginTransaction();
                FragmentTransactionExtended fragmentTransactionExtended = new FragmentTransactionExtended(
                        mContext, fragmentTransaction, new FragmentMain(),
                        fragment_folder_for_music, R.id.fragment_show);
                fragmentTransactionExtended.setTransition();
                fragmentTransactionExtended.commit();
            }
        });


        return view;
    }



    public void setListViewHeightBasedOnChildren(ListView listView) {
        /*
        * 代码设置ListView的高度，使ListView的高度固定，可以在ScrollView中完全显示，而不是显示ListView中一个Item
        *
        * */
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
//        listView.setAdapter(listAdapter);

        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

}
