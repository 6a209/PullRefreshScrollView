package com.li6a209;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import android.view.View;

public class PaddingAnimation{
	// bottom padding
	
	private int mStep = 5;
	private int mPeriod;
	private int mToPadding;
	private boolean mIsOver = false;
	private OnAnimationOverListener mOverListener;
	public PaddingAnimation(int startPadding, int toPadding, int duration){
		int abs = Math.abs(startPadding - toPadding);
		mToPadding = toPadding;
		mPeriod = duration / (abs / mStep); 
		Log.d("the period period is ", mPeriod + "");
	}

	public void setOnAnimationOverListener(OnAnimationOverListener l){
		mOverListener = l;
	}
	
	public void startAnimation(final View view){
		mIsOver = false;
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				view.post(new Runnable() {
					@Override
					public void run() {
						int paddingBottom = view.getPaddingBottom();
						paddingBottom -= mStep;
						if(paddingBottom <= mToPadding){
							timer.cancel();
							mIsOver = true;
						}
						view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), 
							view.getPaddingRight(), paddingBottom);
						if(mIsOver && null != mOverListener){
							mOverListener.onOver();
						}
					}
				});
			}
		}, 0, mPeriod);	
	}
}