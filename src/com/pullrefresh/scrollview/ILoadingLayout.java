package com.pullrefresh.scrollview;

public interface ILoadingLayout{
	public void pullToRefresh();
	public void releaseToRefresh();
	public void refreshing();
	public void normal();
}