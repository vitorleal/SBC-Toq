package com.vleal.sbctoq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ToqAppletInstallBroadcastReciever extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
        Intent launchIntent = new Intent(context, ToqActivity.class);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 

        context.startActivity(launchIntent);
	}
}