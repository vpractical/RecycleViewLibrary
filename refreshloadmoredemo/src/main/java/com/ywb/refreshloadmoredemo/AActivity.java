package com.ywb.refreshloadmoredemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ywb.refreshloadmoredemo.bean.User;
import com.ywb.refreshloadmorelibrary.adapter.RecyclerViewAdapter;
import com.ywb.refreshloadmorelibrary.adapter.viewholder.ItemViewHolder;
import com.ywb.refreshloadmorelibrary.listener.OnLoadMoreListener;
import com.ywb.refreshloadmorelibrary.listener.OnRefreshListener;
import com.ywb.refreshloadmorelibrary.utils.L;
import com.ywb.refreshloadmorelibrary.views.SwipeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywb on 2018/3/16.
 */

public class AActivity extends AppCompatActivity implements OnRefreshListener,OnLoadMoreListener{

	SwipeLayout swipeLayout;
	View headerView,footerView;
	RecyclerView rv;
	RecyclerViewAdapter adapter;
	private List<User> list = new ArrayList<>();
	private int curPg;
	private int numRequest;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_a);

		swipeLayout = findViewById(R.id.swipe);
		headerView = new HeaderView(this);
		footerView = new FooterView(this);
		swipeLayout
				.headerView(headerView)
				.footerView(footerView)
				.refreshListener(this)
				.loadMoreListener(this);

		swipeLayout
				.refreshListener(this)
				.loadMoreListener(this);

		rv = findViewById(R.id.rv);
		rv.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
		rv.setAdapter(adapter = new RecyclerViewAdapter<User>(AActivity.this, R.layout.item, list) {
			@Override
			public void convert(ItemViewHolder holder, User bean, int position) {
				bean.pos = position;
				holder.setText(R.id.tv_item_a, bean.name + "   :   id = " + bean.age + "   :   pg = " + bean.pg + "   :   pos = " + position);
			}

			@Override
			public boolean areContentsSame(User oldItem, User newItem) {
				return oldItem.pos == newItem.pos;
			}

			@Override
			public boolean areItemSame(User oldItem, User newItem) {
				return oldItem.pos == newItem.pos;
			}
		});


		getData(true);

	}

	@Override
	public void onRefresh() {
		getData(true);
		L.log("触发刷新");
	}

	@Override
	public void onLoadMore() {
		getData(false);
		L.log("触发加载更多");
	}

	private void setData(int pg, List<User> data) {
		if(pg == 1) list.clear();
		curPg = pg;
		list.addAll(data);
		adapter.notifyChanged();

		if(data.size() < 10){
			swipeLayout.loadNoMore();
		}else{
			swipeLayout.complete();
		}

	}

	private void getData(boolean isRefresh) {
		final int pg;
		if (isRefresh) {
			pg = 1;
			numRequest = 0;
		} else {
			if(curPg == 3) return;
			pg = curPg + 1;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);

					if (pg == 2 && numRequest < 1) {
						numRequest++;
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								swipeLayout.loadMoreFailed();
							}
						});
						return;
					}

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							List<User> data = new ArrayList<>();
							for (int i = 0; i < 10; i++) {
								User user = new User();
								user.name = String.valueOf((char) (0x4e00 + (int) (Math.random() * (0x9fa5 - 0x4e00 + 1))));
								user.age = list.size() + data.size();
								user.pg = pg;
								data.add(user);
								if (pg == 3 && i == 5) break;
							}

							setData(pg, data);
						}
					});
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
