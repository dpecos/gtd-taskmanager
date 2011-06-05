package com.danielpecos.gtdtm.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.utils.ActivityUtils;

public class PreferencesActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);

		final SharedPreferences preferences = TaskManager.getPreferences();
		
		final Preference clearGooglePreferences = (Preference) findPreference("settings_clear_google");
		clearGooglePreferences.setEnabled(!preferences.getString(GoogleAccountActivity.GOOGLE_ACCOUNT_NAME, "").equalsIgnoreCase(""));
		
		clearGooglePreferences.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				ActivityUtils.createConfirmDialog(PreferencesActivity.this, R.string.confirm_clear_google).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor editor = preferences.edit();
						editor.remove(GoogleAccountActivity.GOOGLE_ACCOUNT_NAME);
						editor.remove(GoogleAccountActivity.GOOGLE_AUTH_TOKEN);
						editor.commit();
						
						clearGooglePreferences.setEnabled(false);
					}
				}).show();

				return true;
			}

		});
		
		// TODO: implement backup to file
		if (true || !TaskManager.isFullVersion(this)) {
			((Preference) findPreference("settings_backup_store")).setEnabled(false);
			((Preference) findPreference("settings_backup_restore")).setEnabled(false);
		}
	}
	
	@Override
	public void onBackPressed() {
		//Handle the back button
		this.setResult(RESULT_OK);
		this.finish(); 
	}
}
