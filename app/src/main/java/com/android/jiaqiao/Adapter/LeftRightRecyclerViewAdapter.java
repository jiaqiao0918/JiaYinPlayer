package com.android.jiaqiao.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jiaqiao.JavaBean.MusicInfo;
import com.android.jiaqiao.jiayinplayer.R;

import java.util.ArrayList;

public class LeftRightRecyclerViewAdapter extends RecyclerView.Adapter<LeftRightRecyclerViewAdapter.ViewHolder> {
	private ArrayList<MusicInfo> list = new ArrayList<MusicInfo>();

	private OnRecyclerViewItemClickListener mOnItemClickListener = null;
	private OnRecyclerItemLongListener mOnItemLong = null;

	public LeftRightRecyclerViewAdapter(ArrayList<MusicInfo> list) {
		super();
		this.list = list;
	}

	// 创建新View，被LayoutManager所调用
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		View view = LayoutInflater.from(viewGroup.getContext()).inflate(
				R.layout.left_right_music_play_list, viewGroup, false);
		ViewHolder vh = new ViewHolder(view, mOnItemClickListener,mOnItemLong);
		return vh;
	}

	// 将数据与界面进行绑定的操作
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, int position) {
		MusicInfo music_temp = list.get(position);
		viewHolder.music_title.setText(music_temp.getMusic_title());
		String artist_album_str = "";
		if (music_temp.getMusic_artist() != "" && music_temp.getMusic_artist().length() > 0) {
			artist_album_str += music_temp.getMusic_artist();
		}
		viewHolder.music_artist_album.setText(artist_album_str);
		if(music_temp.is_playing()){
			viewHolder.music_is_playing.setVisibility(View.VISIBLE);
		}else{
			viewHolder.music_is_playing.setVisibility(View.INVISIBLE);
		}
		if(position==list.size()-1){
			viewHolder.view_fgx.setVisibility(View.GONE);
		}
	}

	// 获取数据的数量
	@Override
	public int getItemCount() {
		return list==null?0:list.size();
	}

	public interface OnRecyclerViewItemClickListener {
		void onItemClick(View view, int position);

	}
	public interface OnRecyclerItemLongListener{
		void onItemLongClick(View view, int position);
	}

	public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}
	public void setOnItemLongClickListener(OnRecyclerItemLongListener listener){
		this.mOnItemLong =  listener;
	}

	// 自定义的ViewHolder，持有每个Item的的所有界面元素
	public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
		private TextView music_title, music_artist_album;
		private ImageView music_is_playing;
		private View view_fgx;

		private OnRecyclerViewItemClickListener mOnItemClickListener = null;
		private OnRecyclerItemLongListener mOnItemLong = null;

		public ViewHolder(View view , OnRecyclerViewItemClickListener mListener,OnRecyclerItemLongListener longListener) {
			super(view);
			this.mOnItemClickListener = mListener;
			this.mOnItemLong = longListener;
			music_title = (TextView) view.findViewById(R.id.music_title);
			music_artist_album = (TextView) view.findViewById(R.id.music_artist_album);
			music_is_playing =(ImageView) view.findViewById(R.id.music_is_playing);
			view_fgx =(View) view.findViewById(R.id.view_fgx);
			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
		}
		@Override
		public void onClick(View v) {
			if (mOnItemClickListener != null) {
				//注意这里使用getTag方法获取数据
				mOnItemClickListener.onItemClick(v, getAdapterPosition());
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if(mOnItemLong != null){
				mOnItemLong.onItemLongClick(v,getPosition());
			}
			return true;
		}
	}

}