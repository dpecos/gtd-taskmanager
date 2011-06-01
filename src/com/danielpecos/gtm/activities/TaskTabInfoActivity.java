package com.danielpecos.gtm.activities;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.views.TaskViewHolder;

public class TaskTabInfoActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout_task_tab_info);

		TaskActivity.taskInfoViewHolder = new TaskViewHolder(null, TaskActivity.task);
		TaskActivity.taskInfoViewHolder.setView(findViewById(android.R.id.content));

		/*taskInfoViewHolder.registerChainedFieldEvents(R.id.task_status_check, new Object[] {
				new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
						originalTask.setStatus(task.getStatus());
					}
				}
		});*/

		TaskActivity.taskInfoViewHolder.updateView(this);

	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ActivityUtils.CAMERA_ACTIVITY:
			if (resultCode == Activity.RESULT_OK) {

				Bitmap bitmap = (Bitmap) data.getExtras().get("data");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
				byte [] fileContent = baos.toByteArray();

				TaskActivity.task.setPicture(fileContent);
				TaskActivity.taskInfoViewHolder.updateView(this);
				Log.d(TaskManager.TAG, "Picture read and viewHolder refreshed");

			}
		}
	}
}