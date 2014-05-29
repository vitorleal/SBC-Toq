package com.vleal.sbcui;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class SbcUI {
	private static Context context;

	 public SbcUI(Context context) {
		 SbcUI.setContext(context);
	}
	
	// Get/set context
	public static Context getContext() {
		return context;
	}
	public static void setContext(Context context) {
		SbcUI.context = context;
	}

	// Toast
	public void showToast(String text) {
		Toast toast = Toast.makeText(getContext(), text, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.show();
	}
}