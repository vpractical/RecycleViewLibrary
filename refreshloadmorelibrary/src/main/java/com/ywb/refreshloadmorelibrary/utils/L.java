package com.ywb.refreshloadmorelibrary.utils;

import android.util.Log;

/**
 * Created by ywb on 2018/3/7.
 */

public class L {
	public static void log(String str){
		Log.e("refreshloadmorelib:",str);
	}

	public static int key;
	public static void log(int index,String str){
		if(index == -1) index = key;
		if(key == index) return;
		key = index;
		Log.e(key + "  :  ",str);
	}
}
