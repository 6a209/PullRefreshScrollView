package com.li6a209;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
	
	private static final String TAG = "ElasticScrollView";
	private static final boolean DEBUG = true;
	private static final int PULL_TO_REFRESH_STATUS = 0x00;
	private static final int RELEASE_TO_REFRESH_STATUS = 0x01;
	private static final int REFRESHING_STATUS = 0x02;
	private View mLayout;
	private FrameLayout mContentLy;
	private float mLastY = -1000;
	private Rect mNormal = new Rect();
	private Rect mNormalBak = new Rect();
	private View mHeadView;
	private View mHeadLeftProgress;
	private boolean mIsMoveLayout = false;
	private float mNeedRefreshDeltaY;
	private boolean mNeedRefresh;
	private boolean mIsCanRefresh = false;
	private int mTop;
	private float mDowY;
	
	/*滚动的时候监听*/
//	private int mLastTop;
	
	private Animation mDownToUpAnim;
	private Animation mUpToDownAnim;
	private TextView mHeadTitle; 
	private ImageView mHeadLeftImage;
	
	private boolean mIsAnimation;
	private boolean mIsRefreshing;
	
	private Timer mTimer;
//	private boolean mFirstLayout = true;
	private float mDefautlTopMargin;
	

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
		inflater.inflate(R.layout.elastic_scroll_view, this, true);
		mLayout = findViewById(R.id.layout);
		mContentLy = (FrameLayout)findViewById(R.id.content_ly);
		mHeadView = findViewById(R.id.headView);
		mHeadLeftImage = (ImageView)mHeadView.findViewById(R.id.leftLogo);
		mHeadTitle = (TextView)mHeadView.findViewById(R.id.textTitle);
		mDownToUpAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.down_to_up);
		mUpToDownAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.up_to_down);
		mDownToUpAnim.setFillAfter(true);
		mUpToDownAnim.setFillAfter(true);
		mNeedRefreshDeltaY = getResources().getDimension(R.dimen.need_refresh_delta);
		mDefautlTopMargin = -getResources().getDimension(R.dimen.head_view_height);
		mHeadLeftProgress = (ProgressBar)findViewById(R.id.left_progressbar);
		setFadingEdgeLength(0);
		TextView tv = new TextView(getContext());
		tv.setText("i am content");
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(100);
		tv.setBackgroundColor(Color.GRAY);
		tv.setDrawingCacheEnabled(false);
		mContentLy.addView(tv, LayoutParams.FILL_PARENT, 1300);
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
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int newHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(mLayout.getMeasuredHeight()), 
				MeasureSpec.getMode(heightMeasureSpec));
		mLayout.measure(widthMeasureSpec, newHeightSpec);
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
		case MotionEvent.ACTION_UP:
			if(null != mScrollUpDown){
				if(ev.getY() - mDowY >= 20){
					mScrollUpDown.onScrollUp(true);
				}else if(ev.getY() - mDowY <= -30){
					mScrollUpDown.onScrollUp(false);
				}
			}
			if(mIsRefreshing){
				return super.onTouchEvent(ev);
			}
			if(isNeedAnimation()) {
				mIsAnimation = true;
				release();
				mIsMoveLayout = false;
			}
			mLastY = -1000;
			break;
		case MotionEvent.ACTION_MOVE:
			//判断是不是 首次移动,
			if(-1000 == mLastY){
				mLastY = ev.getY();
				mDowY = ev.getY();
				return super.onTouchEvent(ev);
			}
			final float preY = mLastY;
			float nowY = ev.getY();
			int deltaY = (int) (preY - nowY);
			
			mLastY = nowY;
			//正在载入数据
			if(mIsRefreshing){
				if(deltaY < 0){
					return super.onTouchEvent(ev);
				}
			}
			
			//下滑
			if(getHeadViewTopMargin() > mNeedRefreshDeltaY){
				needRefresh(true);
			}else{
				needRefresh(false);
			}
			deltaY /= 2;
			LinearLayout.LayoutParams param = 
					(LinearLayout.LayoutParams)mHeadView.getLayoutParams();
			if(!mIsMoveLayout){
				if(getScrollY() == 0 && deltaY < 0){
					//是否已经移动了布局(没有)
					mIsMoveLayout = true;
					param.topMargin -= deltaY;
					mHeadView.setLayoutParams(param);
				}
			}else{
				param.topMargin -= deltaY;
				mHeadView.setLayoutParams(param);
				if(mLayout.getTop() <= mTop){
					mIsMoveLayout = false;
				}
				return true;
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	public void refreshOver(){
		if(mIsRefreshing){
			mIsRefreshing = false;
			mHeadLeftImage.setVisibility(View.VISIBLE);
			mHeadLeftProgress.setVisibility(View.INVISIBLE);
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					post(new Runnable() {
						@Override
						public void run() {
							LinearLayout.LayoutParams param = 
								(LinearLayout.LayoutParams)mHeadView.getLayoutParams();
							int headMargin = param.topMargin - 1;
							if(param.topMargin <= -mHeadView.getHeight()){
								setHeadViewMargin(-mHeadView.getHeight());
								timer.cancel();
							}else{
								setHeadViewMargin(headMargin);
							}
							
						}
					});
				}
			}, 2, 2);
		}
	}
	
	private void needRefresh(boolean needRefresh){
		mNeedRefresh = needRefresh;
		if(needRefresh){
			if(!mIsCanRefresh){
				mIsCanRefresh = true;
				mHeadLeftImage.startAnimation(mDownToUpAnim);
				mHeadTitle.setText("松手刷新...");
			}
		}else{
			if(mIsCanRefresh){
				mIsCanRefresh = false;
				mHeadLeftImage.startAnimation(mUpToDownAnim);
				mHeadTitle.setText("下拉刷新...");
			}
		}
	}
	
	private void updateStatus(int status){
		switch (status) {
		case PULL_TO_REFRESH_STATUS:
			mHeadTitle.setText(getResources().getString(R.string.pull_to_refresh));
			mHeadLeftImage.startAnimation(mUpToDownAnim);
			break;
		case RELEASE_TO_REFRESH_STATUS:
			mHeadTitle.setText(getResources().getString(R.string.release_to_refresh));
			mHeadLeftImage.startAnimation(mUpToDownAnim);
			break;
		case REFRESHING_STATUS:
			break;
		default:
			break;
		}
	}
	
	private int getHeadViewTopMargin(){
		LinearLayout.LayoutParams param = 
				(LinearLayout.LayoutParams)mHeadView.getLayoutParams();
		return param.topMargin;
	}
	
	private void setHeadViewMargin(float topMargin){
		
		LinearLayout.LayoutParams param = 
				(LinearLayout.LayoutParams)mHeadView.getLayoutParams();
		log("header height is" + mHeadView.getHeight());
		log("header margin is" + ((LinearLayout.LayoutParams)mHeadView.getLayoutParams()).topMargin);
		param.topMargin = (int)topMargin;
		log("top margin" +param.topMargin);
		mHeadView.setLayoutParams(param);
	}
	
	@Override
	public void onDetachedFromWindow(){
		super.onDetachedFromWindow();
		if(mTimer != null){
			mTimer.cancel();
		}
	}
	
//	private int mScrollTotal;
//	
//	/**
//	 * 滚动停止事件
//	 */
//	private void scrollStop(){
//		// scroll 的总高度
//		mScrollTotal = mLayout.getHeight() - getHeight();
//		mScrollTotal -= 250;
//		int scrollY = getScrollY();
//		if(scrollY >= mScrollTotal){
//			//是否要加载更多
//			if(null != mReqMoreListener){
//				mReqMoreListener.onReqMore();
//			}
//		}
//		//将scrollY　传到ctrl　调度图片
//		if(mStopListener != null){
//			mStopListener.onStop(scrollY);
//		}
//		
//	}
	
	private void release() {
		int animationTop; 
		if(mNeedRefresh){
			animationTop = -getHeadViewTopMargin();
		}else{
			animationTop = (int) (mDefautlTopMargin - getHeadViewTopMargin());
		}
		
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0,
				animationTop);
		ta.setDuration(300);
		mLayout.startAnimation(ta);
		ta.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				mLayout.clearAnimation();
				mNormal.set(mNormalBak);
				mIsCanRefresh = false;
				mHeadLeftImage.clearAnimation();
				mHeadTitle.setText("下拉刷新");
				mIsAnimation = false;
				if(mNeedRefresh){
					setHeadViewMargin(0);
					if(null != mOnRefereshListener){
						mIsRefreshing = true;
						mHeadLeftProgress.setVisibility(View.VISIBLE);
						mHeadLeftImage.setVisibility(View.INVISIBLE);
						mOnRefereshListener.onReferesh();
					}
				}else{
					setHeadViewMargin(mDefautlTopMargin);
				}
			}
		});
		
	}

	/**
	 * 隐藏滚动条
	 * @param hide
	 */
	public void hideProgress(boolean hide){
		View v = findViewById(R.id.wall_progress_ly);
		if(hide){
			v.setVisibility(View.INVISIBLE);
		}else{
			v.setVisibility(View.VISIBLE);
		}
	}
	
	private boolean isNeedAnimation() {
		if(getHeadViewTopMargin() > mDefautlTopMargin){
			return true;
		}
		return false;
	}
	
	private void log(String msg){
		Log.d(TAG, msg);
	}
	
}
