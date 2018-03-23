package com.ywb.refreshloadmorelibrary.listener;

/**
 * Created by ywb on 2018/3/16.
 */

public interface LoadMoreTrigger {
	void onPrepare();
	void onPush();
	void onMove(float val,float total);
	void onLoadingMore();
	void onLoaded();
	void onLoadNoMore();
	void onLoadFailed();
}
