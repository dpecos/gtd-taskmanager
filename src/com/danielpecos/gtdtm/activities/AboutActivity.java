package com.danielpecos.gtdtm.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;

public class AboutActivity  extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_about);
		
		TextView textView_version = (TextView)findViewById(R.id.app_version);
		
		ComponentName comp = new ComponentName(this, AboutActivity.class);
		try {
			PackageInfo version = this.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
			textView_version.setText("ver. " + version.versionName + " (" + version.versionCode + ")");
		} catch (NameNotFoundException e) {
			Log.e(TaskManager.TAG, "Could not read app version", e);
		}
	}
}
