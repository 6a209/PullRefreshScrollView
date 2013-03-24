package com.li6a209;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.li6a209.PullRefreshScrollView.OnRefereshListener;
import com.li6a209.PullRefreshScrollView.OnReqMoreListener;

/**
 * 
 * @author 6a209
 * Mar 16, 2013
 */
public class DemoAct extends Activity{
	PullRefreshScrollView mScrollView;
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		mScrollView = new PullRefreshScrollView(this);
		setContentView(mScrollView);
		TextView tv = new TextView(this);
		tv.setText(" i am content");
		tv.setTextSize(80);
		tv.setBackgroundColor(Color.LTGRAY);
		tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1500));
		mScrollView.setContentView(tv);
		mScrollView.setOnRefereshListener(new OnRefereshListener() {
			@Override
			public void onReferesh() {
				new GetDataTask(true).execute();
			}
		});
		mScrollView.setOnReqMoreListener(new OnReqMoreListener() {
			@Override
			public void onReqMore() {
				new GetDataTask(false).execute();
			}
		});
	}
	
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {
		
		private boolean mIsRefresh;
		
		public GetDataTask(boolean isRefresh){
			mIsRefresh = isRefresh;
		}
			
		@Override
		protected String[] doInBackground(Void... params) {
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			if(mIsRefresh){
				mScrollView.refreshOver();
			}else{
				mScrollView.getMoreOver();
			}
			super.onPostExecute(result);
		}
	}
}