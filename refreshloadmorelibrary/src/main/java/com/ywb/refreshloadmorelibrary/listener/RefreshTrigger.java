package com.ywb.refreshloadmorelibrary.listener;

/**
 * Created by ywb on 2018/3/16.
 */

public interface RefreshTrigger {
	void onPrepare();
	void onPull();
	void onRelease();
	void onMove(float val,float total);
	void onRefreshing();
	void onSuccess();
	void onFailed();
}
