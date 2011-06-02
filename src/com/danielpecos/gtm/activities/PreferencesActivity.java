package com.danielpecos.gtm.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.utils.ActivityUtils;

public class PreferencesActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);

		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		Preference clearGooglePreferences = (Preference) findPreference("settings_clear_google");
		clearGooglePreferences.setEnabled(!preferences.getString(GoogleAccountActivity.GOOGLE_ACCOUNT_NAME, "").equalsIgnoreCase(""));
		
		clearGooglePreferences.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ActivityUtils.createConfirmDialog(PreferencesActivity.this, R.string.confirm_clear_google).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences settings = preferences;
						SharedPreferences.Editor editor = settings.edit();
						editor.remove(GoogleAccountActivity.GOOGLE_ACCOUNT_NAME);
						editor.remove(GoogleAccountActivity.GOOGLE_AUTH_TOKEN);
						editor.commit();
					}
				}).show();

				return true;
			}

		});
	}
	
	@Override
	public void onBackPressed() {
		//Handle the back button
		this.setResult(RESULT_OK);
		this.finish(); 
	}
}
