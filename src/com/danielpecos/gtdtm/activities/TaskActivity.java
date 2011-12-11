package com.danielpecos.gtdtm.activities;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.model.beans.Project;
import com.danielpecos.gtdtm.model.beans.Task;
import com.danielpecos.gtdtm.utils.ActivityUtils;
import com.danielpecos.gtdtm.views.TaskViewHolder;
import com.google.android.maps.GeoPoint;

public class TaskActivity extends Activity {
	public static final String FULL_RELOAD = "full_reload";
	public static final String FILE_NAME = "file_name";

	private TaskManager taskManager;
	private Context context;
	private Project project;

	private Task task;
	private Task originalTask;

	private TaskViewHolder taskViewHolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Long context_id = (Long)getIntent().getSerializableExtra("context_id");
		Long project_id = (Long)getIntent().getSerializableExtra("project_id");
		Long task_id = (Long)getIntent().getSerializableExtra("task_id");

		taskManager = TaskManager.getInstance(this);
		context = taskManager.getContext(context_id);
		if (project_id != null) {
			project = context.getProject(project_id);
			task = project.getTask(task_id);
		} else {
			project = null;
			task = context.getTask(task_id);
		}

		originalTask = (Task)task.clone();

		this.initializeUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu_task_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		if (item.getItemId() == R.id.task_optionsMenu_save) {
			closeSavingChanges();
			return true;
		}
		/*case R.id.menu_delete: {
			closeAndDiscardChanges();
			return true;
		}*/
		else if (item.getItemId() == R.id.task_optionsMenu_revert) {
			closeAndDiscardChanges();
			return true;
		}
		return false;
	}

	private void initializeUI() {
		setContentView(R.layout.activity_layout_task);

		this.setTitle(task.getName());

		this.taskViewHolder = new TaskViewHolder(findViewById(R.id.task_layout), task);
		this.taskViewHolder.updateView(this);

		Button buttonSave = (Button)findViewById(R.id.button_save);
		buttonSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeSavingChanges();
			}
		});

		Button buttonCancel = (Button)findViewById(R.id.button_cancel);
		buttonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeAndDiscardChanges();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ActivityUtils.MAP_ACTIVITY: 
			Log.d(TaskManager.TAG, "TaskActivity: Returning from the map activity");
			if (resultCode == Activity.RESULT_OK) {
				GeoPoint point = new GeoPoint(data.getIntExtra(TaskMapActivity.LATITUDE, 0), data.getIntExtra(TaskMapActivity.LONGITUDE, 0));
				task.setLocation(point);
				if (taskViewHolder != null) {
					taskViewHolder.updateView(this);
				}
			}
			break;
		case ActivityUtils.CAMERA_ACTIVITY: 
			Log.d(TaskManager.TAG, "TaskActivity: Returning from the camera app");
			if (resultCode == Activity.RESULT_OK) {

				Bitmap bitmap = (Bitmap) data.getExtras().get("data");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
				byte [] fileContent = baos.toByteArray();

				task.setPicture(fileContent);
				if (taskViewHolder != null) {
					taskViewHolder.updateView(this);
				}
				Log.d(TaskManager.TAG, "Picture read and viewHolder refreshed");

			}
			break;
		}
	}

	@Override
	public void onBackPressed() {
		//Handle the back button
		this.close();
	}

	private void close() {
		if (task.hashCode() != originalTask.hashCode()) {
			Log.d(TaskManager.TAG, "TaskActivity: task was modified");
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.task_quit_title)
			.setMessage(R.string.task_quit_message)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					closeSavingChanges();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					closeAndDiscardChanges();
				}
			})
			.show();
		} else {
			Log.d(TaskManager.TAG, "TaskActivity: task wasn't modified");
			TaskActivity.this.setResult(RESULT_CANCELED);
			this.finish();
		}
	}

	private void closeSavingChanges() {
		Log.d(TaskManager.TAG, "TaskActivity: close activity saving changes");
		task.store(TaskActivity.this);
		ActivityUtils.createAlarm(this, context, project, task);
		Intent resultIntent = new Intent();
		if (task.getName().equalsIgnoreCase(originalTask.getName()) 
				&& (task.getDescription() == null || task.getDescription().equalsIgnoreCase(originalTask.getDescription())) 
				&& task.getPriority().equals(originalTask.getPriority())) {

			Log.d(TaskManager.TAG, "TaskActivity: taskViewHolder refresh required");
			resultIntent.putExtra(TaskActivity.FULL_RELOAD, false);
		} else {
			Log.d(TaskManager.TAG, "TaskActivity: full reload required");
			resultIntent.putExtra(TaskActivity.FULL_RELOAD, true);
		}
		this.setResult(RESULT_OK, resultIntent);
		this.finish();  
	}

	private void closeAndDiscardChanges() {
		Log.d(TaskManager.TAG, "TaskActivity: close activity discarding changes");
		task.reload(TaskActivity.this);
		TaskActivity.this.setResult(RESULT_CANCELED);
		TaskActivity.this.finish();   
	}

}
