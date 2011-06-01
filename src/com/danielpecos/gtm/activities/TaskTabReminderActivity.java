package com.danielpecos.gtm.activities;

import android.app.Activity;
import android.os.Bundle;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.views.TaskViewHolder;

public class TaskTabReminderActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout_task_tab_reminder);

		TaskActivity.taskReminderViewHolder = new TaskViewHolder(null, TaskActivity.task);
		TaskActivity.taskReminderViewHolder.setView(findViewById(android.R.id.content));
		TaskActivity.taskReminderViewHolder.updateView(this);

	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}
}
