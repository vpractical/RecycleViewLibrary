package com.ywb.refreshloadmorelibrary.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.ywb.refreshloadmorelibrary.listener.LoadMoreTrigger;
import com.ywb.refreshloadmorelibrary.listener.OnLoadMoreListener;
import com.ywb.refreshloadmorelibrary.listener.OnRefreshListener;
import com.ywb.refreshloadmorelibrary.listener.RefreshTrigger;
import com.ywb.refreshloadmorelibrary.utils.L;

/**
 * Created by ywb on 2018/3/16.
 */

public class SwipeLayout extends ViewGroup {

	public SwipeLayout(Context context) {
		this(context, null);
	}

	public SwipeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, -1);
	}

	public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mTouchSlop = ViewConfiguration.getTouchSlop();
		mScroller = new AutoScroller();
	}

	private View mHeaderView, mTargetView, mFooterView;
	private boolean mHasHeader, mHasFooter;
	private boolean mRefreshEnable = true, mLoadMoreEnable = true;
	private OnRefreshListener onRefreshListener;
	private OnLoadMoreListener onLoadMoreListener;
	private RefreshTrigger mRefreshTrigger;
	private LoadMoreTrigger mLoadMoreTrigger;

	private int mHeaderHeight, mFooterHeight;
	private int mHeaderOffset, mTargetOffset, mFooterOffset;

	/**
	 * 触摸参数
	 */
	private float downX, downY, lastX, lastY;
	private final int mTouchSlop;
	private static float mDragRatio = 0.5f;
	private int mActivePointerId;
	private static final int INVALID_POINTER = -1;

	private AutoScroller mScroller;

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		L.log("onFinishInflate 回调");
		int childNum = getChildCount();
		if(childNum != 1){
			throw new IllegalStateException("only contains one view or miss view");
		}

		mTargetView = getChildAt(0);

//		int childNum = getChildCount();
//		if(childNum != 3){
//			throw new IllegalStateException("only contains three views or miss view");
//		}
//
//		this.mHeaderView = getChildAt(0);
//		this.mHasHeader = true;
//		if (mHeaderView instanceof RefreshTrigger) {
//			this.mRefreshTrigger = (RefreshTrigger) mHeaderView;
//		} else {
//			throw new IllegalStateException("headerView must be implements RefreshTrigger");
//		}
//
//		mTargetView = getChildAt(1);
//
//		this.mFooterView = getChildAt(2);
//		if(mFooterView instanceof LoadMoreTrigger){
//			this.mHasFooter = true;
//			this.mLoadMoreTrigger = (LoadMoreTrigger) mFooterView;
//		}else{
//			throw new IllegalStateException("footerView must be implements LoadMoreTrigger");
//		}

	}

	private class AutoScroller implements Runnable {

		private Scroller scroller;
		private int scrollerLastY;
		private boolean isRunning, isAbort;

		public AutoScroller() {
			scroller = new Scroller(getContext());
		}

		@Override
		public void run() {
			boolean isFinish = !scroller.computeScrollOffset() || scroller.isFinished();
			int curY = scroller.getCurrY();
			int disY = curY - scrollerLastY;
			if (isFinish) {
				finish();
			} else {
				scrollerLastY = curY;
				doAutoScroll(disY);
				post(this);
			}
		}

		public void finish() {
			scrollerLastY = 0;
			isRunning = false;
			removeCallbacks(this);
			L.log("finish 滚动完成 " + isAbort);
			if (!isAbort) {
				autoUpdateFinished();
			}
		}

		public void abortIfRunning() {
			L.log("abortIfRunning " + isRunning);
			if (isRunning) {
				if (!scroller.isFinished()) {
					isAbort = true;
					scroller.forceFinished(true);
				}
				finish();
				isAbort = false;
			}
		}

		public void autoScroll(int yScrolled, int duration) {
			removeCallbacks(this);
			scrollerLastY = 0;
			if (!scroller.isFinished()) {
				scroller.forceFinished(true);
			}
			scroller.startScroll(0, 0, 0, yScrolled, 500);
			post(this);
			isRunning = true;
		}
	}

	/**
	 * 滚动完成后恢复状态，有刷新中和默认两种
	 */
	private void autoUpdateFinished() {
		L.log("autoUpdateFinished");
		if (State.isReleaseToRefresh()) {
			L.log("autoUpdateFinished refreshing");
			State.mState = State.STATE_REFRESHING;
			fixStateLayout();
			if (mRefreshTrigger != null) {
				mRefreshTrigger.onRefreshing();
			}
			if (onRefreshListener != null && mRefreshEnable) {
				onRefreshListener.onRefresh();
			}
		} else {
			L.log("autoUpdateFinished normal");
			State.mState = State.STATE_NORMAL;
			fixStateLayout();
		}
	}

	private void doAutoScroll(final float yScrolled) {

		updateScroll(yScrolled);
	}

	private void fingerScroll(final float yDiff) {
		float ratio = mDragRatio;
		float yScrolled = yDiff * ratio;

		float tmpTargetOffset = yScrolled + mTargetOffset;
		if ((tmpTargetOffset > 0 && mTargetOffset < 0)
				|| (tmpTargetOffset < 0 && mTargetOffset > 0)) {
			yScrolled = -mTargetOffset;
		}

		updateScroll(yScrolled);
	}

	private void updateScroll(float yScrolled) {
		if (yScrolled == 0) return;
		mTargetOffset += yScrolled;
		if (State.isRefresh()) {
			mHeaderOffset = mTargetOffset;
			mFooterOffset = 0;
		} else if (State.isLoadMore()) {
			mHeaderOffset = 0;
			mFooterOffset = mTargetOffset;
		}

		layoutChildren();
		invalidate();
	}


	private void scrollReleaseToRefresh2Refreshing() {
		mScroller.autoScroll(mHeaderHeight - mHeaderOffset, 0);
	}

	private void scrollPullToRefresh2Normal() {
		mScroller.autoScroll(-mHeaderOffset, 0);
	}

	private void scrollRefreshing2Normal() {
		mScroller.autoScroll(-mTargetOffset, 0);
	}

	private void scrollLoadingMore2Normal() {
		mScroller.autoScroll(-mTargetOffset, 0);
	}


	//measureChildWithMargins() : measure 时考虑把 margin 及  padding 也作为子视图大小的一部分
	//需要重载 generateDefaultLayoutParams() 返回一个继承自 ViewGroup.MarginLayoutParams 的 布局类
	public static class LayoutParams extends MarginLayoutParams {

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (mHasHeader) {
			final View headerView = mHeaderView;
			measureChildWithMargins(headerView, widthMeasureSpec, 0, heightMeasureSpec, 0);
			MarginLayoutParams lp = (MarginLayoutParams) headerView.getLayoutParams();
			mHeaderHeight = headerView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
		}

		measureChildWithMargins(mTargetView, widthMeasureSpec, 0, heightMeasureSpec, 0);

		if (mHasFooter) {
			final View footerView = mFooterView;
			measureChildWithMargins(footerView, widthMeasureSpec, 0, heightMeasureSpec, 0);
			MarginLayoutParams lp = (MarginLayoutParams) footerView.getLayoutParams();
			mFooterHeight = footerView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layoutChildren();
	}

	private void layoutChildren() {
		final int height = getMeasuredHeight();
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingBottom = getPaddingBottom();

		MarginLayoutParams tlp = (MarginLayoutParams) mTargetView.getLayoutParams();
		final int tLeft = paddingLeft + tlp.leftMargin;
		final int tRight = tLeft + mTargetView.getMeasuredWidth();
		final int tTop = paddingTop + tlp.topMargin + mTargetOffset;
		final int tBottom = tTop + mTargetView.getMeasuredHeight();
		mTargetView.layout(tLeft, tTop, tRight, tBottom);

		if (mHasHeader) {
			MarginLayoutParams hlp = (MarginLayoutParams) mHeaderView.getLayoutParams();
			final int hLeft = paddingLeft + hlp.leftMargin;
			final int hRight = hLeft + mHeaderView.getMeasuredWidth();
			final int hTop = paddingTop + hlp.topMargin + mHeaderOffset - mHeaderHeight;
			final int hBottom = hTop + mHeaderHeight;
			mHeaderView.layout(hLeft, hTop, hRight, hBottom);
			mHeaderView.bringToFront();
		}

		if (mHasFooter) {
			MarginLayoutParams flp = (MarginLayoutParams) mFooterView.getLayoutParams();
			final int fLeft = paddingLeft + flp.leftMargin;
			final int fTop = height - paddingBottom + mFooterOffset;
			final int fRight = fLeft + mTargetView.getMeasuredWidth();
			final int fBottom = fTop + mFooterHeight;
			mFooterView.layout(fLeft, fTop, fRight, fBottom);
			mFooterView.bringToFront();
		}
	}

	private void fixStateLayout() {
		if (State.isRefreshing()) {
			mTargetOffset = mHeaderHeight;
			mHeaderOffset = mTargetOffset;
			mFooterOffset = 0;
		} else if (State.isNormal()) {
			mTargetOffset = 0;
			mHeaderOffset = 0;
			mFooterOffset = 0;
		}
		layoutChildren();
		invalidate();
	}

	/**
	 * 滑动状态
	 */
	public static final class State {
		public static final int STATE_NORMAL = 0;
		public static final int STATE_PULL_TO_REFRESH = -1;
		public static final int STATE_RELEASE_TO_REFRESH = -2;
		public static final int STATE_REFRESHING = -3;
		public static final int STATE_LOADINGMORE = 1;

		public static int mState = STATE_NORMAL;//当前页面状态

		public static boolean isNormal() {
			return STATE_NORMAL == mState;
		}

		public static boolean isPullToRefresh() {
			return STATE_PULL_TO_REFRESH == mState;
		}

		public static boolean isReleaseToRefresh() {
			return STATE_RELEASE_TO_REFRESH == mState;
		}

		public static boolean isRefreshing() {
			return STATE_REFRESHING == mState;
		}

		public static boolean isLoadingMore() {
			return STATE_LOADINGMORE == mState;
		}

		public static boolean isRefresh() {
			return STATE_NORMAL > mState;
		}

		public static boolean isLoadMore() {
			return STATE_NORMAL < mState;
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getActionMasked()) {
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				L.log(18,"dispatch up");
				if (State.isReleaseToRefresh()) {
					L.log("dispatch up releaseToRefresh");
					scrollReleaseToRefresh2Refreshing();
				} else if (State.isPullToRefresh()) {
					L.log("dispatch up pullToRefresh");
					scrollPullToRefresh2Normal();
				}
				break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		int action = ev.getActionMasked();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = ev.getPointerId(0);
				lastX = downX = getMotionEventX(ev, mActivePointerId);
				lastY = downY = getMotionEventY(ev, mActivePointerId);

				L.log(19,"Intercept down 按下 = " + State.mState);

				if (State.isPullToRefresh() || State.isReleaseToRefresh() || State.isLoadMore()) {
					L.log(11, "状态为还原时下拉");
					mScroller.abortIfRunning();
				}

				if (State.isPullToRefresh() || State.isReleaseToRefresh() || State.isLoadMore()) {
					L.log(12, "Intercept down return true");
					return true;
				}

				break;
			case MotionEvent.ACTION_MOVE:
				L.log(13, "Intercept move");
				if (mActivePointerId == INVALID_POINTER) {
					return false;
				}
				final float x = getMotionEventX(ev, mActivePointerId);
				final float y = getMotionEventY(ev, mActivePointerId);
				final float disX = x - downX;
				final float disY = y - downY;
				lastX = x;
				lastY = y;

				boolean moved = Math.abs(disY) > Math.abs(disX) && Math.abs(disY) > mTouchSlop;
				boolean refreshTriggerCondition = moved && disY > 0 && canRefreshCheck();
				boolean loadMoreTriggerCondition = moved && disY < 0 && canLoadMoreCheck();

				Log.e("-------","loadMoreTriggerCondition : " + loadMoreTriggerCondition + "  can : " + canLoadMoreCheck());

				if (refreshTriggerCondition || loadMoreTriggerCondition) {
					return true;
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				final int pointerIndex = ev.getActionIndex();
				final int pointerId = ev.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					mActivePointerId = ev.getPointerId(newPointerIndex);
				}
				lastX = downX = getMotionEventX(ev, mActivePointerId);
				lastY = downY = getMotionEventY(ev, mActivePointerId);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mActivePointerId = INVALID_POINTER;
				L.log(14, "Intercept up");
				break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getActionMasked();

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = event.getPointerId(0);
				L.log(15, "touch down");
				return true;
			case MotionEvent.ACTION_MOVE:
				final float x = getMotionEventX(event, mActivePointerId);
				final float y = getMotionEventY(event, mActivePointerId);
				final float disX = x - lastX;
				float disY = y - lastY;
				lastX = x;
				lastY = y;
				L.log(-1, "touch move---------------------- " + State.mState);
				if (Math.abs(disY) < Math.abs(disX) && Math.abs(disX) > mTouchSlop) {
					return false;
				}

				if (State.isRefresh() && mTargetOffset <= 0) {
					State.mState = State.STATE_NORMAL;
					fixStateLayout();
					return false;
				}

				if (State.isNormal()) {
					L.log(1, "1 如果state = normal - disY = " + disY + "  mTargetOffset = " + mTargetOffset + "   ");
					if (disY > 0 && canRefreshCheck()) {
						State.mState = State.STATE_PULL_TO_REFRESH;
						L.log(2, "2 state = pull");
						if (mRefreshTrigger != null) {
							mRefreshTrigger.onPrepare();
						}
					} else if (disY < 0 && canLoadMoreCheck()) {
						if (State.mState != State.STATE_LOADINGMORE) {
							State.mState = State.STATE_LOADINGMORE;
							L.log(3, "3 state = loading");
							if (mLoadMoreTrigger != null) {
								mLoadMoreTrigger.onLoadingMore();
							}
							if (onLoadMoreListener != null && mLoadMoreEnable) {
								mFooterView.setOnClickListener(null);
								onLoadMoreListener.onLoadMore();
							}
						}
					}
				}

				if (State.isPullToRefresh()) {
					L.log(4, "4 如果state = pull - disY = " + disY + "  mTargetOffset = " + mTargetOffset + "   ");
					if (mTargetOffset > mHeaderHeight) {
						State.mState = State.STATE_RELEASE_TO_REFRESH;
						L.log(5, "5 state = release");
						if (mRefreshTrigger != null) {
							mRefreshTrigger.onRelease();
						}
					} else if (mTargetOffset <= 0) {
						State.mState = State.STATE_NORMAL;
						L.log(6, "6 state = normal");
					}
					fingerScroll(disY);
				} else if (State.isReleaseToRefresh()) {
					L.log(7, "7 如果state = release - disY = " + disY + "  mTargetOffset = " + mTargetOffset + "   ");
					if (mTargetOffset <= mHeaderHeight) {
						State.mState = State.STATE_PULL_TO_REFRESH;
						L.log(8, "8 state = pull");
						if (mRefreshTrigger != null) {
							mRefreshTrigger.onPull();
						}
					}
					fingerScroll(disY);
				} else if (State.isLoadingMore()) {
					L.log(9, "9 如果state = loading - disY = " + disY + "  mTargetOffset = " + mTargetOffset + "   ");
					if (mTargetOffset >= 0) {
						State.mState = State.STATE_NORMAL;
						L.log(10, "10 state = normal");
					} else if (mTargetOffset < -mFooterHeight) {
						disY = -mFooterHeight - mTargetOffset;
					}
					fingerScroll(disY);
				}

				return true;
			case MotionEvent.ACTION_POINTER_DOWN: {
				final int pointerIndex = event.getActionIndex();
				final int pointerId = event.getPointerId(pointerIndex);
				if (pointerId != INVALID_POINTER) {
					mActivePointerId = pointerId;
				}
				downX = lastX = getMotionEventX(event, mActivePointerId);
				downY = lastY = getMotionEventY(event, mActivePointerId);
				break;
			}
			case MotionEvent.ACTION_POINTER_UP: {
				final int pointerIndex = event.getActionIndex();
				final int pointerId = event.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					mActivePointerId = event.getPointerId(newPointerIndex);
				}
				downX = lastX = getMotionEventX(event, mActivePointerId);
				downY = lastY = getMotionEventY(event, mActivePointerId);
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				L.log(17, "touch up");
				if (mActivePointerId == INVALID_POINTER) {
					return false;
				}
				mActivePointerId = INVALID_POINTER;
				break;
		}
		return super.onTouchEvent(event);
	}

	private float getMotionEventY(MotionEvent event, int activePointerId) {
		final int index = MotionEventCompat.findPointerIndex(event, activePointerId);
		if (index < 0) {
			return -1;
		}
		return MotionEventCompat.getY(event, index);
	}

	private float getMotionEventX(MotionEvent event, int activePointId) {
		final int index = MotionEventCompat.findPointerIndex(event, activePointId);
		if (index < 0) {
			return -1;
		}
		return MotionEventCompat.getX(event, index);
	}

	private boolean canRefreshCheck() {
		boolean can = mRefreshEnable && mHasHeader && !mTargetView.canScrollVertically(-1);
		return can;
	}

	private boolean canLoadMoreCheck() {
		boolean can = mLoadMoreEnable && mHasFooter && !mTargetView.canScrollVertically(1);
		return can;
	}

	/**
	 * 设置
	 */

	public SwipeLayout headerView(View headerView) {
		this.mHeaderView = headerView;
		this.mHasHeader = true;
		if (headerView instanceof RefreshTrigger) {
			this.mRefreshTrigger = (RefreshTrigger) headerView;
		} else {
			throw new IllegalStateException("headerView must be implements RefreshTrigger");
		}

//		mHeaderView.setLayoutParams(generateDefaultLayoutParams());

		addView(mHeaderView, 0);
		return this;
	}

	public SwipeLayout footerView(View footerView) {
		this.mFooterView = footerView;
		this.mHasFooter = true;
		if (footerView instanceof LoadMoreTrigger) {
			this.mLoadMoreTrigger = (LoadMoreTrigger) footerView;
		} else {
			throw new IllegalStateException("footerView must be implements LoadMoreTrigger");
		}

//		mHeaderView.setLayoutParams(generateDefaultLayoutParams());

		addView(mFooterView, getChildCount());
		return this;
	}

	public SwipeLayout refreshEnable(boolean enable) {
		this.mRefreshEnable = enable;
		return this;
	}

	public SwipeLayout loadMoreEnable(boolean enable) {
		this.mLoadMoreEnable = enable;
		return this;
	}

	public SwipeLayout refreshListener(OnRefreshListener l) {
		this.onRefreshListener = l;
		return this;
	}

	public SwipeLayout loadMoreListener(OnLoadMoreListener l) {
		this.onLoadMoreListener = l;
		return this;
	}

	public void complete() {
		L.log("complete");
		if (State.isRefresh()) {
			L.log("complete  scrollRefreshing2Normal");
			scrollRefreshing2Normal();
		}

		if (State.isLoadingMore()) {
			L.log("complete  scrollLoadingMore2Normal");
			scrollLoadingMore2Normal();
		}
	}

	public void loadMoreFailed() {
		L.log("loadMoreFailed");
		if (mLoadMoreTrigger != null) {
			mLoadMoreTrigger.onLoadFailed();
			if (onLoadMoreListener != null && mLoadMoreEnable) {
				mFooterView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						mFooterView.setOnClickListener(null);
						onLoadMoreListener.onLoadMore();
					}
				});
			}
		}
	}

	public void loadNoMore() {
		L.log("loadNoMore");
		if (mLoadMoreTrigger != null) {
			mLoadMoreTrigger.onLoadNoMore();
		}
	}
}
