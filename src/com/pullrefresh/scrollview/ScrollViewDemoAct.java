package com.pullrefresh.scrollview;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class ScrollViewDemoAct extends Activity{

	PullRefreshScrollView mScrollView;

	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.main);
		mScrollView = (PullRefreshScrollView)findViewById(R.id.scrollvew);
		mScrollView.setOnRefereshListener(new PullRefreshScrollView.OnRefereshListener() {
			@Override
			public void onReferesh() {
				new GetDataTask().execute();
			}
		});
        TextView tv = new TextView(this);
        tv.setText("the F**k code -------------/n /r ------>>>>>>>>>>>>>> /n /r <<<<<<<<<<<<<<<<<<<___________----------------+++++++++++++++++");

        tv.setTextSize(200);
        mScrollView.setContentView(tv);
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