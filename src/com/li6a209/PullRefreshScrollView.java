package com.li6a209;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.li6a209.pullrefreshscrollview.R;


/**
 * 可以下拉刷新的scrollView 
 * @author 6a209
 *
 * 2011-11-20 下午9:15:47
 */
public class PullRefreshScrollView extends ScrollView{
	
//	private static final String TAG = "ElasticScrollView";
//	private static final boolean DEBUG = true;
	private static final int PULL_TO_REFRESH_STATUS = 0x00;
	private static final int RELEASE_TO_REFRESH_STATUS = 0x01;
	private static final int REFRESHING_STATUS = 0x02;
	private static final int NORMAL_STATUS = 0x03;
	
	//mode head or foot
	private static final int HEAD_MODE = 0x00;
	private static final int FOOT_MODE = 0x01;
	private int mStatus = NORMAL_STATUS;
	private int mMode = HEAD_MODE;
	protected FrameLayout mContentLy;
	private float mLastY = -1000;
	private FrameLayout mHeadViewLy;
	private FrameLayout mFootViewLy;
	private float mNeedRefreshDeltaY;
	private float mNeedGetMoreDeltaY;
	private float mDowY;
	
	private ILoadingLayout mHeadLoadingView;
	private ILoadingLayout mFootLoadingView;
	
	private boolean mIsAnimation;
	private final float mDefautlTopMargin;

	public interface OnScrollUpDownListener{
		public void onScrollUp(boolean isUp);
	}
	
	public interface OnReqMoreListener{
		public void onReqMore();
	}
	
	public interface OnRefereshListener{
		public void onReferesh();
	}
	
	/*滚动停止回调*/
	public interface OnStopListener{
		public void onStop(int scrollY);
	}
	
	private OnScrollUpDownListener mScrollUpDown;
	private OnStopListener mStopListener;
	private OnReqMoreListener mReqMoreListener;
	private OnRefereshListener mOnRefereshListener;
	
	public PullRefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.pull_refresh_scroll_view, this, true);
		mContentLy = (FrameLayout)findViewById(R.id.content_ly);
		mHeadViewLy = (FrameLayout)findViewById(R.id.head_ly);
		mFootViewLy = (FrameLayout)findViewById(R.id.foot_ly);
		mHeadLoadingView = new HeadLoadingView(context);
		mFootLoadingView = new FootLoadingView(context);
		mHeadViewLy.addView((View)mHeadLoadingView);
		mFootViewLy.addView((View)mFootLoadingView);
		mHeadLoadingView.normal();
		mFootLoadingView.normal();
		mNeedRefreshDeltaY = getResources().getDimension(R.dimen.need_refresh_delta);
		mNeedGetMoreDeltaY = getResources().getDimension(R.dimen.need_refresh_delta);
		mDefautlTopMargin = -getResources().getDimension(R.dimen.head_view_height);
		setFadingEdgeLength(0);
//		TextView tv = new TextView(getContext());
//		tv.setText("i am content");
//		tv.setGravity(Gravity.CENTER);
//		tv.setTextSize(100);
//		tv.setBackgroundColor(Color.GRAY);
//		tv.setDrawingCacheEnabled(false);
//		mContentLy.addView(tv, LayoutParams.FILL_PARENT, 3300);
	}
	
	public void setOnReqMoreListener(OnReqMoreListener listener){
		mReqMoreListener = listener;
	}

	public void setOnStopListener(OnStopListener listener){
		mStopListener = listener;
	}
	
	public void setOnScrollUpDownListener(OnScrollUpDownListener listener){
		mScrollUpDown = listener;
	}
	
	public void setOnRefereshListener(OnRefereshListener listener){
		mOnRefereshListener = listener;
	}
	
	public void setContentView(View view){
		mContentLy.addView(view);
	}
	
	public void refreshOver(){
		MaginAnimation maginAnim = new MaginAnimation(0, (int)mDefautlTopMargin, 300);
		maginAnim.startAnimation(mHeadViewLy);
		maginAnim.setOnAnimationOverListener(new OnAnimationOverListener() {
			@Override
			public void onOver() {
				updateStatus(NORMAL_STATUS, mHeadLoadingView);
			}
		});
	}
	
	public void getMoreOver(){
		updateStatus(NORMAL_STATUS, mFootLoadingView);
	}
	
//	public HeadLoadingView getHeadLoadingView(){
//		return mHeadLoadingView;
//	}
	
	public void setToRefreshing(){
		MaginAnimation maginAnim = new MaginAnimation(getHeadViewTopMargin(), 0, 300);
		maginAnim.startAnimation(mHeadViewLy);
		maginAnim.setOnAnimationOverListener(new OnAnimationOverListener() {
			@Override
			public void onOver() {
				updateStatus(REFRESHING_STATUS, mHeadLoadingView);
			}
		});
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(mIsAnimation){
			return super.onTouchEvent(ev);
		}
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastY = ev.getY();
			mDowY = ev.getY();
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(null != mScrollUpDown){
				if(ev.getY() - mDowY >= 20){
					mScrollUpDown.onScrollUp(true);
				}else if(ev.getY() - mDowY <= -30){
					mScrollUpDown.onScrollUp(false);
				}
			}
			release(mStatus);
			mLastY = -1000;
			break;
		case MotionEvent.ACTION_MOVE:
			//判断是不是 首次移动,
			if(-1000 == mLastY){
				mLastY = ev.getY();
				mDowY = ev.getY();
				return super.onTouchEvent(ev);
			}
			final float lastY = mLastY;
			float nowY = ev.getY();
			int deltaY = (int) (lastY - nowY);
			mLastY = nowY;
			if(deltaY < 0){
				//down
				if(getScrollY() == 0 && mStatus != REFRESHING_STATUS){
					// head
//					mMode = HEAD_MODE;
					updateMode(HEAD_MODE);
					updateHeadMargin(deltaY / 2);
					if(getHeadViewTopMargin() >= mNeedRefreshDeltaY){
						updateStatus(RELEASE_TO_REFRESH_STATUS, mHeadLoadingView);
					}else{
						updateStatus(PULL_TO_REFRESH_STATUS, mHeadLoadingView);
					}
					return super.onTouchEvent(ev);	
				}
				
				if(mStatus != REFRESHING_STATUS && mStatus != NORMAL_STATUS){
					// foot
//					mMode = FOOT_MODE;
					updateMode(FOOT_MODE);
					if(getPaddingBottom() > 0 && getPaddingBottom() < mNeedGetMoreDeltaY){
						updateStatus(PULL_TO_REFRESH_STATUS, mFootLoadingView);
					}else if(getPaddingBottom() >= mNeedGetMoreDeltaY){
						updateStatus(RELEASE_TO_REFRESH_STATUS, mFootLoadingView);
					}else if(getPaddingBottom() == 0){
						updateStatus(NORMAL_STATUS, mFootLoadingView);
					}
					updateFootPadding(deltaY / 2);
				}
				
			}else{
				//up
				if(mMode == HEAD_MODE && mStatus != REFRESHING_STATUS 
					&& mStatus != NORMAL_STATUS){
					// head
					updateMode(HEAD_MODE);
					updateHeadMargin(deltaY / 2);
					if(getHeadViewTopMargin() > mDefautlTopMargin 
						&& getHeadViewTopMargin() < mNeedRefreshDeltaY){
						updateStatus(PULL_TO_REFRESH_STATUS, mHeadLoadingView);
					}else if(getHeadViewTopMargin() == mDefautlTopMargin){
						updateStatus(NORMAL_STATUS, mHeadLoadingView);
					}
					return super.onTouchEvent(ev);
				}
				if(getScrollY() + getHeight() >= mContentLy.getHeight() + mFootViewLy.getHeight() 
					&& mStatus != REFRESHING_STATUS){
					// foot 
					updateMode(FOOT_MODE);
					updateFootPadding(deltaY / 2);
					if(getPaddingBottom() >= mNeedGetMoreDeltaY){
						updateStatus(RELEASE_TO_REFRESH_STATUS, mFootLoadingView);
					}else{
						updateStatus(PULL_TO_REFRESH_STATUS, mFootLoadingView);
					}
				}
			}
			
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	private void updateHeadMargin(int deltaY){
		LinearLayout.LayoutParams param = 
				(LinearLayout.LayoutParams)mHeadViewLy.getLayoutParams();
		param.topMargin -= deltaY;
		if(param.topMargin <= mDefautlTopMargin){
			param.topMargin = (int)mDefautlTopMargin;
		}
		mHeadViewLy.setLayoutParams(param);
	}
	
	private void updateFootPadding(int deltaY){
		int bottomPadding = getPaddingBottom() + deltaY;
		if(bottomPadding <= 0){
			bottomPadding = 0;
		}
		setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomPadding);
	}
	
	private void updateStatus(int status, ILoadingLayout layout){
		if(mStatus == status){
			return;
		}
		mStatus = status;
		debug();
		switch (status) {
		case PULL_TO_REFRESH_STATUS:
			layout.pullToRefresh();
			break;
		case RELEASE_TO_REFRESH_STATUS:
			layout.releaseToRefresh();
			break;
		case REFRESHING_STATUS:
			layout.refreshing();
			break;
		case NORMAL_STATUS:
			layout.normal();
			break;
		default:
			break;
		}
	}
	
	private void updateMode(int mode){
		mMode = mode;
	}
	
	private int getHeadViewTopMargin(){
		LinearLayout.LayoutParams param = 
				(LinearLayout.LayoutParams)mHeadViewLy.getLayoutParams();
		return param.topMargin;
	}
	
	private void release(int status) {
		if(HEAD_MODE == mMode){
			headReleas();
		}else if(FOOT_MODE == mMode){
			footReleas();
		}
		
	}
	
	private void headReleas(){
		int toMagin;
		if(RELEASE_TO_REFRESH_STATUS == mStatus){
			toMagin = 0;
		}else if(PULL_TO_REFRESH_STATUS == mStatus){
			toMagin = (int)mDefautlTopMargin;
		}else{
			return;
		}
		Log.d("headView margin", "" + getHeadViewTopMargin());
		Log.d("to magin", "" + toMagin);
		MaginAnimation maginAnim = new MaginAnimation(getHeadViewTopMargin(), toMagin, 300);
		maginAnim.startAnimation(mHeadViewLy);
		maginAnim.setOnAnimationOverListener(new OnAnimationOverListener() {
			@Override
			public void onOver() {
				if(mStatus == RELEASE_TO_REFRESH_STATUS){
					if(null != mOnRefereshListener){
						updateStatus(REFRESHING_STATUS, mHeadLoadingView);
						mOnRefereshListener.onReferesh();
					}
				}else if(mStatus == PULL_TO_REFRESH_STATUS){
					updateStatus(NORMAL_STATUS, mHeadLoadingView);
				}
			}
		});
	}
	
	private void footReleas(){
		if(getPaddingBottom() > mNeedGetMoreDeltaY){
			if(null != mReqMoreListener){
				updateStatus(REFRESHING_STATUS, mFootLoadingView);
				mReqMoreListener.onReqMore();
			}
		}else{
			updateStatus(NORMAL_STATUS, mFootLoadingView);
		}
		updateFootPadding(-getPaddingBottom());
	}
	
	private void debug(){
		String status = "";
		String mode = "";
		if(mStatus == PULL_TO_REFRESH_STATUS){
			status = "PULL_TO_REFRESH_STATUS";
		}else if(mStatus == RELEASE_TO_REFRESH_STATUS){
			status = "RELEASE_TO_REFRESH_STATUS";
		}else if(mStatus == NORMAL_STATUS){
			status = "NORMAL_STATUS";
		}else if(mStatus == REFRESHING_STATUS){
			status = "REFRESHING_STATUS";
		}
		Log.d("the status is ", status);
		
		if(mMode == HEAD_MODE){
			mode = "HEAD_MODE";
		}else if(mMode == FOOT_MODE){
			mode = "FOOT_MODE";
		}
		Log.d("the mode is ", mode);
	}
}
