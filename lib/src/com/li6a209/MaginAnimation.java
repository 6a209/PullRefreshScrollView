package com.li6a209;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MaginAnimation{
	// top magin

	
	private int mStep = 5;
	private int mPeriod = 10;
	private int mToMaing;
	private boolean mIsOver = false;
	private OnAnimationOverListener mOverListener;
	public MaginAnimation(int startMagin, int toMagin, int duration){
		float abs = Math.abs(startMagin - toMagin);
		float a = abs / (float) mStep;
		mToMaing = toMagin;
		Log.d("abs / mStep > ","start " + startMagin + " to margin" + toMagin);
		mPeriod = (int) ((float)duration / a); 
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
						LinearLayout.LayoutParams param = 
							(LinearLayout.LayoutParams)view.getLayoutParams();
						param.topMargin -= mStep;
						if(param.topMargin <= mToMaing){
							param.topMargin = mToMaing;
							timer.cancel();
							mIsOver = true;
						}
						view.setLayoutParams(param);
						if(mIsOver && null != mOverListener){
							mOverListener.onOver();
						}
					}
				});
			}
		}, 0, mPeriod);	
	}
}