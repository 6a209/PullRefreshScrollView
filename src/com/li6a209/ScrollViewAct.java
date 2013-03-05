package com.li6a209;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.li6a209.PullRefreshScrollView.OnRefereshListener;
import com.li6a209.pullrefreshscrollview.R;


public class ScrollViewAct extends Activity{
	
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
			// Simulates a background job.
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// Do some stuff here

			// Call onRefreshComplete when the list has been refreshed.
			mScrollView.refreshOver();

			super.onPostExecute(result);
		}
	}
}