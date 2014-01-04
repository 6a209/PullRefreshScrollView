package com.pullrefresh.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;


/**
 * 可以下拉刷新的scrollView 
 * @author 6a209
 *
 * 2011-11-20 下午9:15:47
 */
public class PullRefreshScrollView extends ScrollView{
	
	private static final String TAG = "PullRefreshScrollView";
	private static final boolean DEBUG = true;

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
	private float mLastY = 0;
	private FrameLayout mHeadViewLy;
	protected FrameLayout mFootViewLy;
	private float mNeedRefreshDeltaY;
	private float mNeedGetMoreDeltaY;

	private ILoadingLayout mHeadLoadingView;
	private ILoadingLayout mFootLoadingView;
	private LinearLayout mNoMoreView;
	private LinearLayout mEmptyView;
	
	private boolean mIsAnimation;
	private final float mDefautlTopPadding;
	private boolean mCanPullUpGetMore = true;
	private boolean mCanPullDownRefresh = true;
	
	private int mTouchSlop;
	private View mCustomFootLoadingView;

//    private Scroller mPullScroller;
    private int mToStatus;
    private ILoadingLayout mActionLoadingLayout;

    private int mAnimationDuration = 300;


	public interface OnReqMoreListener{
		public void onReqMore();
	}
	
	public interface OnRefereshListener{
		public void onReferesh();
	}
	
	public interface OnStopListener{
		public void onStop(int scrollY);
	}

    private interface  OnPullAnimationOverListener{
        public void onPullAnimationOver();
    }
	
	
	private OnStopListener mStopListener;
	protected OnReqMoreListener mReqMoreListener;
	private OnRefereshListener mOnRefereshListener;
    private OnPullAnimationOverListener mPullAnimListener;


	public PullRefreshScrollView(Context context){
		this(context, null);
	}
	
	public PullRefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = LayoutInflater.from(context);
		ViewConfiguration vc = ViewConfiguration.get(context);
		mTouchSlop = vc.getScaledTouchSlop();
		inflater.inflate(R.layout.pull_refresh_scroll_view, this, true);
		mContentLy = (FrameLayout)findViewById(R.id.content_ly);
		mHeadViewLy = (FrameLayout)findViewById(R.id.head_ly);
		mFootViewLy = (FrameLayout)findViewById(R.id.foot_ly);
		mHeadLoadingView = new HeadLoadingView(context);
		mFootLoadingView = new FootLoadingView(context);
		mNoMoreView = (LinearLayout) inflater.inflate(R.layout.no_more_layout, null);
		mEmptyView = (LinearLayout) inflater.inflate(R.layout.empty_layout, this, false);
//		mFootViewLy.addView(mNoMoreView);
		mHeadViewLy.addView((View)mHeadLoadingView);
		mFootViewLy.addView((View)mFootLoadingView);
		mHeadLoadingView.normal();
		mFootLoadingView.normal();
		mNeedRefreshDeltaY = getResources().getDimension(R.dimen.need_refresh_delta);
		mNeedGetMoreDeltaY = getResources().getDimension(R.dimen.need_refresh_delta);
		mDefautlTopPadding = getResources().getDimension(R.dimen.head_view_height);
		setFadingEdgeLength(0);
	}
	
	public void setOnReqMoreListener(OnReqMoreListener listener){
		mReqMoreListener = listener;
	}

	public void setOnStopListener(OnStopListener listener){
		mStopListener = listener;
	}
	
	public void setOnRefereshListener(OnRefereshListener listener){

		mOnRefereshListener = listener;
	}

    void setOnPullAnimOverListener(OnPullAnimationOverListener listener){
        mPullAnimListener = listener;
    }
	
	public void setContentView(View view){
		mContentLy.addView(view);
	}
	
	public void refreshOver(){
        mToStatus = NORMAL_STATUS;
        debug("the contentPaddingTop is =>" + getContentPaddingTop());
        PaddingAnimation paddingAnimation = new PaddingAnimation(getContentPaddingTop(), 0, mAnimationDuration);
        paddingAnimation.setOnAnimationOverListener(new OnAnimationOverListener() {
            @Override
            public void onOver() {
                updateStatus(NORMAL_STATUS, mHeadLoadingView);
            }
        });
        paddingAnimation.startAnimation(mHeadViewLy, true);
	}

	public void getMoreOver(){
		updateStatus(NORMAL_STATUS, mFootLoadingView);
		requestLayout();
	}
	
	public void hideFootLoading(){
		mCanPullUpGetMore = false;
		mFootViewLy.removeAllViews();
		mFootViewLy.addView(mNoMoreView,new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	public void showEmptyView(){
		mCanPullUpGetMore = false;
		mFootViewLy.removeAllViews();
		mFootViewLy.addView(mEmptyView);
	}
	
	public void resetFoot(){
		mCanPullUpGetMore = true;
		mFootViewLy.removeAllViews();
		if(null == mCustomFootLoadingView){
			mFootViewLy.addView((View)mFootLoadingView);
			mFootLoadingView.normal();
		}else{
			mFootViewLy.addView(mCustomFootLoadingView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	public void setNonePullUp(View view){
		mCustomFootLoadingView = view;
		mCanPullUpGetMore = false;
		mFootViewLy.removeAllViews();
		mFootViewLy.addView(mCustomFootLoadingView, new LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	public void setCanPullDown(boolean is){
		mCanPullDownRefresh = is;
	}
	
	public void showFootLoading(){
		mCanPullUpGetMore = true;
		mFootViewLy.setVisibility(View.VISIBLE);
	}

	private boolean isRefreshOver = true;

	public void setToRefreshing(){

		if (isRefreshOver) {
			isRefreshOver = false;
//            mPullScroller.startScroll(0, 0, 0, getContentPaddingTop(), 300);
            setOnPullAnimOverListener(new OnPullAnimationOverListener() {
                @Override
                public void onPullAnimationOver() {
                    isRefreshOver = true;
                    if(null != mOnRefereshListener){
                        mOnRefereshListener.onReferesh();
                    }
                }
            });
		}
	}
	/**
	 * it the content top padding
	 * @param top
	 */
	public void setContentTopPadding(int top){
		mHeadViewLy.setPadding(0, top, 0, 0);
	}

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        debug("on intercept touch");
        try{
            ViewGroup parent = (ViewGroup) getParent();
            if(null != parent){
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }catch(ClassCastException e){

        }
        if(MotionEvent.ACTION_DOWN == ev.getAction()){
            mLastY = ev.getY();
        }
        return super.onInterceptTouchEvent(ev);
    }

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
        debug("on touch event");
		if(mIsAnimation){
			return super.onTouchEvent(ev);
		}
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
            debug("is in ACTION_DOWN");
			mLastY = ev.getY();
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			release(mStatus);
			break;
		case MotionEvent.ACTION_MOVE:
			final float lastY = mLastY;
			float nowY = ev.getY();
            debug("lastY is " + lastY + "  nowY is" + nowY);
			int deltaY = (int) (lastY - nowY);
			mLastY = nowY;

			if(deltaY < 0){
                debug("getScrollY is =>" + getScrollY());
				//down
				if(getScrollY() <= 0 && mStatus != REFRESHING_STATUS){
					// head
//					mMode = HEAD_MODE;
					if (!mCanPullDownRefresh) {
						return super.onTouchEvent(ev);
					}
					updateMode(HEAD_MODE);
					updateHeadPadding(deltaY / 2);
					if(getContentPaddingTop() >= mNeedRefreshDeltaY){
						updateStatus(RELEASE_TO_REFRESH_STATUS, mHeadLoadingView);
					}else{
						updateStatus(PULL_TO_REFRESH_STATUS, mHeadLoadingView);
					}
                    return true;
				}
				
				if(mStatus != REFRESHING_STATUS && mStatus != NORMAL_STATUS){
					// foot
					if(!mCanPullUpGetMore){
						return super.onTouchEvent(ev);
					}
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
                    if (!mCanPullDownRefresh) {
                        return super.onTouchEvent(ev);
					}
					updateMode(HEAD_MODE);
					updateHeadPadding(deltaY / 2);
					if(getContentPaddingTop() > mDefautlTopPadding){
						updateStatus(RELEASE_TO_REFRESH_STATUS, mHeadLoadingView);
					}else if(getContentPaddingTop() == 0){
						updateStatus(NORMAL_STATUS, mHeadLoadingView);
					}
					return super.onTouchEvent(ev);
				}
				if(getScrollY() + getHeight() >= mContentLy.getHeight() + mFootViewLy.getHeight() 
					&& mStatus != REFRESHING_STATUS){
					if(!mCanPullUpGetMore){
						return super.onTouchEvent(ev);
					}
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
	
	private void updateHeadPadding(int deltaY){
        int topPadding = getContentPaddingTop() - deltaY;
        debug("the deltay is=> " + deltaY);
        debug("the current padding is =>" + topPadding);
        mHeadViewLy.setPadding(getPaddingLeft(), topPadding, getPaddingRight(), getPaddingBottom());
	}

    private int getContentPaddingTop(){
       return mHeadViewLy.getPaddingTop();
    }
	
	private void updateFootPadding(int deltaY){
		int bottomPadding = getPaddingBottom() + deltaY;
		if(bottomPadding <= 0){
			bottomPadding = 0;
		}
		setPadding(getPaddingLeft(), getContentPaddingTop(), getPaddingRight(), bottomPadding);
	}
	
	private void updateStatus(int status, ILoadingLayout layout){
		if(mStatus == status){
			return;
		}
		mStatus = status;
		switch (status) {
		case PULL_TO_REFRESH_STATUS:
            debug("to pull to refresh status <------");
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

	private void release(int status) {
		if(HEAD_MODE == mMode){
			headReleas();
		}else if(FOOT_MODE == mMode){
			if(!mCanPullUpGetMore){
				return;
			}
			footReleas();
		}
		
	}
	
	private void headReleas(){
		int toPadding;
		if(RELEASE_TO_REFRESH_STATUS == mStatus){
            int headTopPadding = getContentPaddingTop();
			toPadding = (int) mDefautlTopPadding;
		}else if(PULL_TO_REFRESH_STATUS == mStatus){
			toPadding = 0;
		}else{
			return;
		}
        PaddingAnimation paddingAnimation = new PaddingAnimation(getContentPaddingTop(), toPadding, mAnimationDuration);
        paddingAnimation.setOnAnimationOverListener(new OnAnimationOverListener() {
            @Override
            public void onOver() {
                if(RELEASE_TO_REFRESH_STATUS == mStatus){
					if(null != mOnRefereshListener){
						updateStatus(REFRESHING_STATUS, mHeadLoadingView);
						mOnRefereshListener.onReferesh();
					}
				}else if(PULL_TO_REFRESH_STATUS == mStatus){
					updateStatus(NORMAL_STATUS, mHeadLoadingView);
				}
            }
        });
        paddingAnimation.startAnimation(mHeadViewLy, true);
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

    private void debug(String d){
        if(DEBUG){
            Log.d(TAG, d);
        }
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
