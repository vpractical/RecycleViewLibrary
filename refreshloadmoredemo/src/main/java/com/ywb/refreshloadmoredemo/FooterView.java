package com.ywb.refreshloadmoredemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ywb.refreshloadmorelibrary.listener.LoadMoreTrigger;
import com.ywb.refreshloadmorelibrary.utils.L;

/**
 * Created by ywb on 2018/3/16.
 */

public class FooterView extends FrameLayout implements LoadMoreTrigger{
	public FooterView(Context context) {
		super(context);
		init();
	}

	public FooterView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FooterView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init(){
		view = inflate(getContext(),R.layout.footer,this);
		tvTip = view.findViewById(R.id.tip);
	}

	private View view;
	private TextView tvTip;

	private void setTip(String str){
		tvTip.setText(str);
	}

	@Override
	public void onPrepare() {
		setTip("onPrepare");
	}

	@Override
	public void onPush() {
		setTip("onPush");
	}

	@Override
	public void onMove(float val, float total) {
		setTip("onMove");
	}

	@Override
	public void onLoadingMore() {
		L.log("加载中");
		setTip("onLoadingMore 正在加载");
	}

	@Override
	public void onLoaded() {
		L.log("加载完成");
		setTip("onLoaded 加载完成");
	}

	@Override
	public void onLoadNoMore() {
		L.log("没有更多");
		setTip("onLoadNoMore 没有更多了");
	}

	@Override
	public void onLoadFailed() {
		L.log("加载失败");
		setTip("onLoadFailed 点击重试");
	}

}
