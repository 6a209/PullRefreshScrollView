package com.pullrefresh.scrollview;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import android.view.View;

public class PaddingAnimation{
	// bottom padding
	
    //  16ms  60 fps
	private int mPeriod = 16;
    private int mStepDistance;
	private int mToPadding;
	private boolean mIsOver = false;


	private OnAnimationOverListener mOverListener;
	public PaddingAnimation(int startPadding, int toPadding, float duration){
		int abs = Math.abs(startPadding - toPadding);
		mToPadding = toPadding;
        int count = (int)(duration / mPeriod);
        mStepDistance = abs / count;
	}

	public void setOnAnimationOverListener(OnAnimationOverListener l){
		mOverListener = l;
	}
	
	public void startAnimation(final View view, final boolean isTopPadding){
		mIsOver = false;
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				view.post(new Runnable() {
					@Override
					public void run() {
                        if(isTopPadding){
                            paddingTopAnim(view, timer);
                        }else{
                            paddingBottomAnim(view, timer);
                        }
					}
				});
			}
		}, 0, mPeriod);	
	}

    private void paddingTopAnim(View view, Timer timer){
        int paddingTop = view.getPaddingTop();
        paddingTop = targetPadding(paddingTop, timer);
        view.setPadding(view.getPaddingLeft(), paddingTop,
                view.getPaddingRight(), view.getPaddingBottom());
        if(mIsOver && null != mOverListener){
            mOverListener.onOver();
        }
    }

    private void paddingBottomAnim(View view, Timer timer){
        int paddingBottom = view.getPaddingBottom();
        paddingBottom = targetPadding(paddingBottom, timer);
        view.setPadding(view.getPaddingLeft(), view.getPaddingBottom(), view.getPaddingRight(), paddingBottom);
        if(mIsOver && null != mOverListener){
            mOverListener.onOver();
        }
    }

    private int targetPadding(int targetPadding, Timer timer){
       if(targetPadding > mToPadding){
           targetPadding -= mStepDistance;
           if(targetPadding <= mToPadding){
               targetPadding = mToPadding;
               finish(timer);
           }
       }else{
           targetPadding += mStepDistance;
           if(targetPadding >= mToPadding){
               targetPadding = mToPadding;
               finish(timer);
           }
       }
       return targetPadding;
    }

    private void finish(Timer timer){
        timer.cancel();
        mIsOver = true;
    }
}