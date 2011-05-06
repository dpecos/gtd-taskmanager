package com.danielpecos.gtm.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.danielpecos.gtm.R;

public class TaskActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.task_layout);

	    Resources res = getResources(); 
	    TabHost tabHost = getTabHost(); 
	    TabHost.TabSpec spec;  
	    Intent intent;  

	    // Create an Intent to launch an Activity for the tab (to be reused)

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    /*intent = new Intent().setClass(this, ContextActivity.class);
	    spec = tabHost.newTabSpec("artists").setIndicator("Artists",
	                      res.getDrawable(R.drawable.stat_sys_signal_0))
	                  .setContent(intent);
	    tabHost.addTab(spec);*/
	    
	    intent = new Intent().setClass(this, TaskTabInfoActivity.class);
	    spec = tabHost.newTabSpec("details").setIndicator("Details",
                res.getDrawable(android.R.drawable.ic_menu_info_details))
                .setContent(intent);
	    tabHost.addTab(spec);

	    spec = tabHost.newTabSpec("map").setIndicator("Map",
                res.getDrawable(android.R.drawable.ic_menu_mapmode))
                .setContent(intent);
	    tabHost.addTab(spec);
	    
	    spec = tabHost.newTabSpec("reminder").setIndicator("Reminder",
                res.getDrawable(android.R.drawable.ic_popup_reminder))
                .setContent(intent);
	    tabHost.addTab(spec);
	}
}
