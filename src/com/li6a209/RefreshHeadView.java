package com.li6a209;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class RefreshHeadView extends LinearLayout implements IRefreshLayout{

	public RefreshHeadView(Context context){
		this(context, null);
	}
	
	public RefreshHeadView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void pullToRefresh() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshing() {
		// TODO Auto-generated method stub
		
	}
	
}