package jiayinplayer.example.jiaqiao.Fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import jiayinplayer.example.jiaqiao.Adapter.MusicListViewAdapter;
import jiayinplayer.example.jiaqiao.JavaBean.MusicInfo;
import jiayinplayer.example.jiaqiao.jiayinplayer.R;

/**
 * Created by jiaqiao on 2017/6/28/0028.
 */

public class FragmentFolderForMusicItem extends Fragment {
    private Context mContext;
    private String folder_name_str = "";
    private ArrayList<MusicInfo> folder_list = new ArrayList<MusicInfo>();

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public void setValues(String folder_name_str, ArrayList<MusicInfo> folder_list) {
        this.folder_name_str = folder_name_str;
        this.folder_list = folder_list;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder_for_music_item_layout, null);
        TextView folder_name = (TextView) view.findViewById(R.id.folder_name);
        ImageButton back_last_fragment = (ImageButton) view.findViewById(R.id.back_last_fragment);
        ListView show_folder_list = (ListView) view.findViewById(R.id.show_folder_list);
        back_last_fragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        if (folder_name_str.length() > 0) {
            folder_name.setText(folder_name_str);
        }
        if (folder_list != null && folder_list.size() > 0) {
            MusicListViewAdapter adapter = new MusicListViewAdapter(mContext,folder_list);
            show_folder_list.setAdapter(adapter);
        }


        return view;
    }
}
