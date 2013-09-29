package com.pullrefresh.scrollview;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.pullrefresh.scrollview.R;


public class FootLoadingView extends LinearLayout implements ILoadingLayout{

	private TextView mTitleTv;
	private ProgressBar mProgress;
	
	public FootLoadingView(Context context){
		this(context, null);
	}
	
	public FootLoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.foot_layout, this);
		mTitleTv = (TextView)findViewById(R.id.foot_layout_text);
		mProgress = (ProgressBar)findViewById(R.id.foot_layout_progress);
	}

	@Override
	public void pullToRefresh() {
		mTitleTv.setText(getResources().getString(R.string.pull_up_to_get_more));
	}

	@Override
	public void releaseToRefresh() {
		mTitleTv.setText(getResources().getString(R.string.release_to_get_more));
	}

	@Override
	public void refreshing() {
		mTitleTv.setText(getResources().getString(R.string.refreshing));
		mProgress.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void normal(){
		mTitleTv.setText(getResources().getString(R.string.pull_up_to_get_more));
		mProgress.setVisibility(View.GONE);
	}
	
}