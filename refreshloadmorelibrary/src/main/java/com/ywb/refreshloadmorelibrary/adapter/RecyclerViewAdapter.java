package com.ywb.refreshloadmorelibrary.adapter;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ywb.refreshloadmorelibrary.adapter.viewholder.ItemViewHolder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ywb on 2018/3/7.
 */

public abstract class RecyclerViewAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private Context context;
	private int itemLayoutId;
	private List<T> mDates;

	public RecyclerViewAdapter(Context context, int itemLayoutId, List<T> mDates) {
		this.context = context;
		this.itemLayoutId = itemLayoutId;
		this.mDates = mDates;
	}

	public abstract void convert(ItemViewHolder holder, T bean, int position);

	@Override
	public int getItemCount() {
		return mDates.size();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(context).inflate(itemLayoutId, parent, false);
			return new ItemViewHolder(view);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof ItemViewHolder) {
			ItemViewHolder yHolder = (ItemViewHolder) holder;
			convert(yHolder, mDates.get(position),position);
		}
	}

	/**
	 * 内容变化的通知
	 * 需要重写这两个方法，才能使用
	 * areItemSame 需要判断两个bean是不是同一个，用唯一性属性如id去判断，同一个返回true
	 * areContentsSame 判断两个bean显示出来的属性是否一致，如显示了name，age，这两个属性都一样返回true
	 */

	private List<T> oldDatas = new ArrayList<>();

	public boolean areItemSame(T oldItem,T newItem){
		return false;
	}

	public boolean areContentsSame(T oldItem,T newItem){
		return false;
	}

	/**
	 * 使用这个方法要重写上面那两
	 */
	public void notifyChanged(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback,true);
//				((Activity)context).runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						diffResult.dispatchUpdatesTo(XAdapter.this);
//						oldDatas.clear();
//						oldDatas.addAll(mDatas);
//					}
//				});
//			}
//		}).start();
		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback,true);
		diffResult.dispatchUpdatesTo(RecyclerViewAdapter.this);
		oldDatas.clear();
		oldDatas.addAll(mDates);
	}

	private DiffUtil.Callback diffCallback = new DiffUtil.Callback() {
		@Override
		public int getOldListSize() {
			return oldDatas.size();
		}

		@Override
		public int getNewListSize() {
			return mDates.size();
		}

		@Override
		public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
			return areItemSame(oldDatas.get(oldItemPosition),mDates.get(newItemPosition));
		}

		@Override
		public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
			return areContentsSame(oldDatas.get(oldItemPosition),mDates.get(newItemPosition));
		}
	};

	@Override
	public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
		super.registerAdapterDataObserver(new NotifyObserver(observer));
	}

	private class NotifyObserver extends RecyclerView.AdapterDataObserver {

		RecyclerView.AdapterDataObserver mDataObserver;
		public NotifyObserver(RecyclerView.AdapterDataObserver dataObserver) {
			mDataObserver = dataObserver;
		}
		@Override
		public void onChanged() {
			mDataObserver.onChanged();
		}
		@Override
		public void onItemRangeChanged(int positionStart, int itemCount) {
			mDataObserver.onItemRangeChanged(positionStart , itemCount);
		}
		@Override
		public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
			mDataObserver.onItemRangeChanged(positionStart, itemCount, payload);
		}
		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			mDataObserver.onItemRangeInserted(positionStart, itemCount);
		}
		@Override
		public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			mDataObserver.onItemRangeMoved(fromPosition, toPosition, itemCount);
		}
		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			mDataObserver.onItemRangeRemoved(positionStart, itemCount);
		}
	}

}
