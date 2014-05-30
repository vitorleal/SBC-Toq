package com.vleal.sbc.api;

import java.util.ArrayList;
import java.util.concurrent.Callable;

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
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.ListCard;
import com.qualcomm.toq.smartwatch.api.v1.deckofcards.card.SimpleTextCard;
import com.vleal.sbc.AssetActivity;
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
	
	//Make card list
	public void makeCardList(final ListCard listCard, final Boolean update) throws JSONException {
		SbcAPI.get(null, null, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject json) {
				try {
					if (json.has("data")) {
						JSONArray assetsList = (JSONArray) json.get("data");
						
						if (assetsList != null && assetsList.length() > 0) {
							for(int i = 0; i < assetsList.length(); i++) {
								
								JSONObject assetItem    = assetsList.getJSONObject(i);
						   		JSONObject asset        = assetItem.getJSONObject("asset");
						   		
						   		String name             = asset.getString("name");
						   		String status           = assetItem.getString("status");
						   		int number              = i + 1;
						   		ArrayList<String> sData = new ArrayList<String>();
						   		
						   		
						   		if (assetItem.has("sensorData")) {
						   			JSONArray sensors  = assetItem.getJSONArray("sensorData");
						   			
									for(int j = 0; j < sensors.length(); j++) {
										JSONObject sensorItem = sensors.getJSONObject(j);
								   		JSONObject sensor     = sensorItem.getJSONObject("ms");
								   		Object v              = sensor.opt("v");
								   		String p              = sensor.getString("p");
								   		String u              = sensor.getString("u");
								   		
								   		Log.e("sensor", v.toString());
								   		sData.add(p + ": " + v +" " + u.toString());
								  	}
								}
						   		
						   		SimpleTextCard assetCard = new SimpleTextCard("asset"+ number, status, System.currentTimeMillis(), name, null);
						   		if (sData.size() > 0) {
						   			assetCard.setMessageText(sData.toArray(new String[sData.size()]));
						   			
						   		} else {
						   			assetCard.setMessageText(new String[] { "Sem dados na SBC" });
						   		}
						   		assetCard.setReceivingEvents(true);
						   		assetCard.setShowDivider(true);
						        listCard.add(assetCard);
						        Log.e("card added", assetCard.toString());
						  	}
						}
					}
					
				} catch (Exception e) {
					Log.e("error", e.toString());
				} 	
			}
		});
	}
	
	//Make asset list
	public void makeList(final ListView list, final ProgressBar loader) throws JSONException {
		SbcAPI.get(null, null, new JsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(JSONObject json) {
				loader.setVisibility(View.GONE);
				
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
									
									Intent intent = new Intent(getContext(), AssetActivity.class);
										
									Bundle bundle = new Bundle();
									bundle.putString("name", asset);
										
									intent.putExtras(bundle);
									getContext().startActivity(intent);
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
	
	
	
	
	//Make asset item
	public void makeItem(final String asset, final ListView list, final ProgressBar loader) throws JSONException {
		loader.setVisibility(View.GONE);
		
		SbcAPI.get(asset, null, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject json) {
				try {
					JSONObject data = json.getJSONObject("data");
					
					ArrayList<String> assets = null;
					
					if (data.has("sensorData")) {
			   			JSONArray sensors  = data.getJSONArray("sensorData");
						assets = new ArrayList<String>();
						
						for(int i = 0; i < sensors.length(); i++) {
							JSONObject assetItem = sensors.getJSONObject(i);
					   		JSONObject asset     = assetItem.getJSONObject("ms");
					   		Object v             = asset.opt("v");
					   		String p             = asset.getString("p");
					   		String u             = asset.getString("u");
					   		
					   		assets.add(p + ": " + v +" " + u.toString());
					  	}
						
					} else {
						assets = new ArrayList<String>();
						assets.add(getContext().getString(R.string.no_asset_data));
					}
					
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), 
							R.layout.sensor_list_item, R.id.sensorName, assets);
					
					list.setAdapter(adapter);

				} catch (JSONException e) {}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				Log.e("Assets get one Failure", e.toString());
			}
		});
	}
	
	//Make asset item
	public void makeCardItem(final SimpleTextCard card) throws JSONException {
		String asset = card.getTitleText();
		
		SbcAPI.get(asset, null, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(JSONObject json) {
				Log.e("josn", json.toString());
		        card.setTimeMillis(System.currentTimeMillis());
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				Log.e("Assets get one Failure", e.toString());
			}
		});
	}
}
