package com.danielpecos.gtm.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;

public class SettingsActivity extends PreferenceActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TaskManager.TAG, "Settings: Cargo el fichero de prerencias");
		addPreferencesFromResource(R.xml.preferences);
		
		//Log.d(TaskManager.TAG, "Settings: Fijo los valores por defecto");
//		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);
	}
}
