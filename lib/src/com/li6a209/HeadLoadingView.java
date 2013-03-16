package com.li6a209;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.li6a209.pullrefreshscrollview.R;

public class HeadLoadingView extends LinearLayout implements ILoadingLayout{

	
	private Animation mDownToUpAnim;
	private Animation mUpToDownAnim;
	private View mLeftProgress;
	private ImageView mLeftImage;
	private TextView mTitle;
	private boolean mImageIsUp;
	

	public HeadLoadingView(Context context){
		this(context, null);
		mDownToUpAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.down_to_up);
		mUpToDownAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.up_to_down);
		mDownToUpAnim.setFillAfter(true);
		mUpToDownAnim.setFillAfter(true);
		LayoutInflater.from(context).inflate(R.layout.head_layout, this);
		mLeftProgress = (ProgressBar)findViewById(R.id.head_layout_left_progressbar);
		mLeftImage = (ImageView)findViewById(R.id.head_layout_left_arrow);
		mTitle = (TextView)findViewById(R.id.head_layout_title);
	}
	
	public HeadLoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void pullToRefresh() {
		if(mImageIsUp){
			mLeftImage.startAnimation(mUpToDownAnim);
			mImageIsUp = false;
		}
		mTitle.setText(getResources().getString(R.string.pull_to_refresh));
	}

	@Override
	public void releaseToRefresh() {
		mLeftImage.startAnimation(mDownToUpAnim);
		mImageIsUp = true;
		mTitle.setText(getResources().getString(R.string.release_to_refresh));
	}

	@Override
	public void refreshing() {
		mImageIsUp = false;
		mLeftProgress.setVisibility(View.VISIBLE);
		mTitle.setText(getResources().getString(R.string.refreshing));
		mLeftImage.clearAnimation();
		mLeftImage.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void normal(){
		mImageIsUp = false;
		mLeftImage.setVisibility(View.VISIBLE);
		mLeftProgress.setVisibility(View.GONE);
		mTitle.setText(getResources().getString(R.string.pull_to_refresh));
	}
	
}