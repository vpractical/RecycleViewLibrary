package com.ywb.refreshloadmoredemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ywb.refreshloadmorelibrary.listener.RefreshTrigger;

/**
 * Created by ywb on 2018/3/16.
 */

public class HeaderView extends FrameLayout implements RefreshTrigger {
	public HeaderView(Context context) {
		super(context);
		init();
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init(){
		view = inflate(getContext(),R.layout.header,this);
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
	public void onPull() {
		setTip("onPull");
	}

	@Override
	public void onRelease() {
		setTip("onRelease");
	}

	@Override
	public void onMove(float val, float total) {
		setTip("onMove");
	}

	@Override
	public void onRefreshing() {
		setTip("onRefreshing");
	}

	@Override
	public void onSuccess() {
		setTip("onSuccess");
	}

	@Override
	public void onFailed() {
		setTip("onFailed" );
	}
}
