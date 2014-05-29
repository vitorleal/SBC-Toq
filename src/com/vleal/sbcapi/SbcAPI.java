package com.vleal.sbcapi;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SbcAPI {
	private static final String SERVICE  = "brasilTest";
	private static final String BASE_URL = "http://195.235.93.67:8080/m2m/v2/services/"+ SERVICE +"/assets/";
	private static final Integer TIMEOUT = 30000;

	private static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(String asset, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		final String URL;
		
		client.setTimeout(TIMEOUT);
		
		if (asset == null) {
			URL = BASE_URL;
		} else {
			URL = getAssetUrl(asset);
		}
		
		client.get(URL, params, responseHandler);
	}

	private static String getAssetUrl(String asset) {
		return BASE_URL + asset;
	}
}