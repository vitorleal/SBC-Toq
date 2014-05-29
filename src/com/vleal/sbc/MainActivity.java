package com.vleal.sbc;

import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.vleal.sbc.api.Assets;
import com.vleal.sbctoq.R;


public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		overridePendingTransition(R.anim.activity_open_scale, R.anim.activity_close_translate);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
				.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_settings:
				Intent intent = new Intent(this, ToqActivity.class);
				startActivityForResult(intent, 0);
				
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			//Get list and loader from the view
			ListView assetsList = (ListView) rootView.findViewById(R.id.assetsList);
			ProgressBar loader  = (ProgressBar) rootView.findViewById(R.id.loader);
			
			try {
				//Get assets list
				Assets assets = new Assets(rootView.getContext());
				assets.makeList(assetsList, loader);
				
			} catch (JSONException e) {}
			
			return rootView;
		}
	}

}
