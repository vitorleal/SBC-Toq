package com.vleal.sbctoq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ToqAppletInstallBroadcastReciever extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("On Receive", "ToqAppletInstallationBroadcastReceiver.onReceive - context: " + context + ", intent: " + intent);

        Intent launchIntent= new Intent(context, ToqActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 

        context.startActivity(launchIntent);
	}
}