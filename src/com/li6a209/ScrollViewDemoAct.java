package com.li6a209;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.li6a209.PullRefreshScrollView.OnRefereshListener;
import com.li6a209.pullrefreshscrollview.R;


public class ScrollViewDemoAct extends Activity{
	
	PullRefreshScrollView mScrollView;
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.main);
		mScrollView = (PullRefreshScrollView)findViewById(R.id.scrollvew);
		mScrollView.setOnRefereshListener(new OnRefereshListener() {
			@Override
			public void onReferesh() {
				new GetDataTask().execute();
			}
		});
	}
	
	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

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
			mScrollView.refreshOver();
			super.onPostExecute(result);
		}
	}
}