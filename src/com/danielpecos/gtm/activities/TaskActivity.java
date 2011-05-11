package com.danielpecos.gtm.activities;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TabHost;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.views.TaskViewHolder;

public class TaskActivity extends TabActivity {
	public static final int DATE_DIALOG_ID = 0;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.task_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_changeName:
			ActivityUtils.showAddDialog(
					this, 
					this.getResources().getString(R.string.textbox_title_name), 
					this.getResources().getString(R.string.textbox_label_name), 
					task.getName(),
					new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							task.setName(((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString());
							taskViewHolder.updateView();
						}
					});
			return true;
		case R.id.menu_changeDescription:
			ActivityUtils.showAddDialog(
					this, 
					this.getResources().getString(R.string.textbox_title_description), 
					this.getResources().getString(R.string.textbox_label_description), 
					task.getDescription(),
					new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							task.setDescription(((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString());
							taskViewHolder.updateView();
						}
					});
			return true;
		case R.id.menu_changeDueDate:
			Calendar c = Calendar.getInstance();
			if (task.getDueDate() != null) 
				c.setTime(task.getDueDate());
			int mYear = c.get(Calendar.YEAR);
			int mMonth = c.get(Calendar.MONTH);
			int mDay = c.get(Calendar.DAY_OF_MONTH);
			new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.YEAR, year);
					c.set(Calendar.MONTH, monthOfYear);
					c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					task.setDueDate(c.getTime());

					taskViewHolder.updateView();
				}
			}, mYear, mMonth, mDay).show();
		}
		return false;
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

		/*spec = tabHost.newTabSpec("map").setIndicator(getString(R.string.task_tab_map),
                res.getDrawable(android.R.drawable.ic_menu_mapmode))
                .setContent(intent);
	    tabHost.addTab(spec);*/

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
