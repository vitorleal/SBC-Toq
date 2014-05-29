package com.vleal.sbcapi;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.vleal.sbctoq.AssetActivity;
import com.vleal.sbctoq.R;


public class Assets {
	private static Context context;
	
	public Assets(Context context) {
		Assets.setContext(context);
	}
	
	public static Context getContext() {
		return context;
	}
	public static void setContext(Context context) {
		Assets.context = context;
	}
	
	//Get asset list
	public void getList(final ListView list, final ProgressBar loader) throws JSONException {
		SbcAPI.get(null, null, new JsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(JSONObject json) {
				loader.setVisibility(View.GONE);
				list.setVisibility(View.VISIBLE);
				
				try {
					if (json.has("data")) {
						JSONArray assetsList = (JSONArray) json.get("data");
						
						if (assetsList != null && assetsList.length() > 0) {
							ArrayList<String> assets = new ArrayList<String>();
							
							for(int i = 0; i < assetsList.length(); i++) {
								JSONObject assetItem = assetsList.getJSONObject(i);
						   		JSONObject asset     = assetItem.getJSONObject("asset");
						   		String name          = asset.getString("name");
						   		
						   		assets.add(name);
						  	}
							
							ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), 
									R.layout.asset_list_item, R.id.assetName, assets);
							
							list.setAdapter(adapter);
							
							list.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> parent, View view, 
				                		int position, long id) {
				                    
									String asset = (String) list.getItemAtPosition(position);
									
									try {
										Assets one = new Assets(getContext());
										one.getAsset(asset);
										
									} catch (JSONException e) {}
								}
				            }); 
						}
					}
				} catch (Exception e) {}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				Log.e("Assets list get Failure", e.toString());
			}
		});
	}
	
	//Get asset
	public void getAsset(final String asset) throws JSONException {
		SbcAPI.get(asset, null, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject json) {
				Intent intent = new Intent(getContext(), AssetActivity.class);
				
				Bundle bundle = new Bundle();
				bundle.putString("name", asset);
				bundle.putString("json", json.toString());
				
				intent.putExtras(bundle);
				getContext().startActivity(intent);
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				Log.e("Assets get one Failure", e.toString());
			}
		});
	}
}
