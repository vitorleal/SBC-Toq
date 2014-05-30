package com.vleal.sbc;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.vleal.sbc.api.Assets;
import com.vleal.sbctoq.R;


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
		String name       = intent.getString("name");
		
		actionBar.setTitle(name);
		
		ListView list      = (ListView) findViewById(R.id.sensorList);
		ProgressBar loader = (ProgressBar) findViewById(R.id.loader_sensor);
		
		try {
			Assets one = new Assets(getApplicationContext());
			one.makeItem(name, list, loader);
			
		} catch (JSONException e) {}
	}
}


