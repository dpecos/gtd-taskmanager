package com.danielpecos.gtm.utils;

import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.danielpecos.gtm.R;
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
	
	public static void onTaskItemDisplay(Activity activity, View rowView,	HashMap<String, Object> data) {
		Resources r = activity.getResources();
		switch((Task.Priority)data.get("priority")) {
		case Low: 
			rowView.findViewById(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityLow_Background);
			((TextView)rowView.findViewById(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityLow_Foreground));
			break;
		case Normal:
			rowView.findViewById(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityNormal_Background);
			((TextView)rowView.findViewById(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityNormal_Foreground));
			break;
		case Important: 
			rowView.findViewById(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityImportant_Background);
			((TextView)rowView.findViewById(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityImportant_Foreground));
			break;
		case Critical: 
			rowView.findViewById(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityCritical_Background);
			((TextView)rowView.findViewById(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityCritical_Foreground));
			break;
		}
		rowView.requestLayout();
	}
}
