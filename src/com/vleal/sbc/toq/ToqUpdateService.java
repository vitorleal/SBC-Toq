package com.vleal.sbc.toq;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class ToqUpdateService extends Service {
	Handler handler = null;

	@Override
	public void onCreate() {
		Log.e("service", "on create");
		
		handler = new Handler();
		handler.postDelayed(updateCards, 0);
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Log.e("service", "on destroy");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private Runnable updateCards = new Runnable() {
		public void run() {
			Log.e("service", "--------- UPDATE --------");
			ToqActivity.updateDeckOfCardsFromUI(getApplicationContext());
			handler.postDelayed(this, 5000);
	    }
	};

}
