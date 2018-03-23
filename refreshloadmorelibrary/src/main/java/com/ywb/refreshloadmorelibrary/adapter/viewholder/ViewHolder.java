package com.ywb.refreshloadmorelibrary.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ywb on 2018/3/13.
 */

public abstract class ViewHolder<Child> extends RecyclerView.ViewHolder{

	private View itemView;
	private SparseArray<View> views = new SparseArray<>();

	public ViewHolder(View itemView) {
		super(itemView);
		this.itemView = itemView;
	}

	public View getConvertView(){
		return itemView;
	}

	public <T extends View>T getView(int viewId){
		View view = views.get(viewId);
		if(view == null){
			view = itemView.findViewById(viewId);
		}
		return (T) view;
	}

	public Child setText(int viewId, String str){
		TextView tv = getView(viewId);
		tv.setText(str);
		return (Child) this;
	}
}
