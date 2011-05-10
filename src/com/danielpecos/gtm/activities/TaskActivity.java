package com.danielpecos.gtm.activities;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.views.TaskViewHolder;

public class TaskActivity extends TabActivity {
	private TaskManager taskManager;
	private Context context;
	private Project project;
	private Task task;
	
	private static TaskViewHolder taskViewHolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    String context_name = (String) getIntent().getSerializableExtra("context_name");
		Long project_id = (Long) getIntent().getSerializableExtra("project_id");
		Long task_id = (Long) getIntent().getSerializableExtra("task_id");
	    
	    taskManager = TaskManager.getInstance();
	    context = taskManager.getContext(context_name);
	    if (project_id != null) {
	    	project = context.getProject(project_id);
	    	task = project.getTask(task_id);
	    } else {
	    	project = null;
	    	task = context.getTask(task_id);
	    }

	    this.initializeUI();
	    
		setResult(RESULT_OK, getIntent());
	}
	
	private void initializeUI() {
		setContentView(R.layout.task_layout);
		
		this.taskViewHolder = new TaskViewHolder(null, task);
		
		Resources res = getResources(); 
	    TabHost tabHost = getTabHost(); 
	    TabHost.TabSpec spec;  

	    Intent intent = new Intent().setClass(this, TaskTabInfoActivity.class);
	    spec = tabHost.newTabSpec("details").setIndicator(getString(R.string.task_tab_details),
                res.getDrawable(android.R.drawable.ic_menu_info_details))
                .setContent(intent);
	    tabHost.addTab(spec);

	    spec = tabHost.newTabSpec("map").setIndicator(getString(R.string.task_tab_map),
                res.getDrawable(android.R.drawable.ic_menu_mapmode))
                .setContent(intent);
	    tabHost.addTab(spec);
	    
	    spec = tabHost.newTabSpec("reminder").setIndicator(getString(R.string.task_tab_reminder),
                res.getDrawable(android.R.drawable.ic_popup_reminder))
                .setContent(intent);
	    tabHost.addTab(spec);
	    
	}
	
	public static class TaskTabInfoActivity extends Activity {
		@Override
		public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.task_tab_info);
		    
		    taskViewHolder.setView(findViewById(android.R.id.content));
		    taskViewHolder.updateView();
		    
		}
	}
}
