package com.li6a209;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;


public class RefreshFootView extends LinearLayout implements IRefreshLayout{

	public RefreshFootView(Context context){
		this(context, null);
	}
	
	public RefreshFootView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void pullToRefresh() {
		
	}

	@Override
	public void release() {
		
	}

	@Override
	public void refreshing() {
		
	}
	
}