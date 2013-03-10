package com.li6a209;

public interface ILoadingLayout{
	public void pullToRefresh();
	public void releaseToRefresh();
	public void refreshing();
	public void normal();
}