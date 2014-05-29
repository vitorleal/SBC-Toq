package com.vleal.sbc;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vleal.sbctoq.R;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class AssetActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);
		
		setContentView(R.layout.activity_asset);
		
		Bundle intent     = getIntent().getExtras();
		String jsonString = intent.getString("json");
		String name       = intent.getString("name");
		
		actionBar.setTitle(name);
		
		try {
			JSONObject json      = new JSONObject(jsonString);
			JSONObject data      = json.getJSONObject("data");
			JSONArray sensorData = data.getJSONArray("sensorData");
			
			ArrayList<String> assets = null;
			
			if (sensorData != null && sensorData.length() > 0) {
				assets = new ArrayList<String>();
				
				for(int i = 0; i < sensorData.length(); i++) {
					JSONObject assetItem = sensorData.getJSONObject(i);
			   		JSONObject asset     = assetItem.getJSONObject("ms");
			   		int v                = asset.getInt("v");
			   		String p             = asset.getString("p");
			   		String u             = asset.getString("u");
			   		
			   		if (u == "Unknown") {
			   			u = null;
			   		}
			   		
			   		assets.add(p + ": " + v +" " + u);
			  	}
				
			} else {
				assets = new ArrayList<String>();
				assets.add(getString(R.string.no_asset_data));
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), 
					R.layout.sensor_list_item, R.id.sensorName, assets);
			
			ListView list = (ListView) findViewById(R.id.sensorList);
			list.setAdapter(adapter);

			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}


