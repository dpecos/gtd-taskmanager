package com.danielpecos.gtm.utils;

import android.app.Activity;
import android.content.Intent;

import com.danielpecos.gtm.activities.ProjectActivity;
import com.danielpecos.gtm.activities.TaskActivity;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;

public class ActivityUtils {
	public static final int PROJECT_ACTIVITY = 0;
	public static final int TASK_ACTIVITY = 1;
	
	public static void showProjectActivity(Activity activity, Context context, Project project) {
		Intent intent = new Intent(activity.getBaseContext(), ProjectActivity.class);
		intent.putExtra("context_name", context.getName());
		intent.putExtra("project_id", project.getId());
		activity.startActivityForResult(intent, PROJECT_ACTIVITY);
	}

	public static void showTaskActivity(Activity activity, Task task) {
		Intent intent = new Intent(activity.getBaseContext(), TaskActivity.class);
		//intent.putExtra("context_name", context.getName());
		//intent.putExtra("project_id", project.getId());
		activity.startActivityForResult(intent, TASK_ACTIVITY);
		
	}
}
