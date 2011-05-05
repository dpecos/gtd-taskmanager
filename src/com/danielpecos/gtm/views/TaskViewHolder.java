package com.danielpecos.gtm.views;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Task;

public class TaskViewHolder extends View {
	private View mRow;
	private TextView task_name = null;
	private TextView task_description = null;
	private CheckBox task_status;

	public TaskViewHolder(Context context, View row) {
		super(context);
		mRow = row;
	}
	public TextView getName() {
		if (task_name == null){
			task_name = (TextView) mRow.findViewById(R.id.task_name);
		}
		return task_name;
	} 
	public TextView getDescription() {
		if (task_description == null){
			task_description = (TextView) mRow.findViewById(R.id.task_description);
		}
		return task_description;
	} 
	public CheckBox getStatus() {
		if (task_status == null) {
			task_status = (CheckBox) mRow.findViewById(R.id.task_status);
		}
		return task_status;
	}
}
