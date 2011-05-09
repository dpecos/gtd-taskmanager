package com.danielpecos.gtm.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Project;

public class ProjectViewHolder extends View {
	private View mRow;
	private TextView project_name = null;
	private TextView project_description = null;
	private ImageView project_status_icon = null;
	private TextView project_status_text = null;

	public ProjectViewHolder(Context context, View row) {
		super(context);
		mRow = row;
	}
	public TextView getName() {
		if (project_name == null){
			project_name = (TextView) mRow.findViewById(R.id.project_name);
		}
		return project_name;
	} 
	public TextView getDescription() {
		if (project_description == null){
			project_description = (TextView) mRow.findViewById(R.id.project_description);
		}
		return project_description;
	} 
	public ImageView getStatusIcon() {
		if (project_status_icon == null){
			project_status_icon = (ImageView) mRow.findViewById(R.id.project_status_icon);
		}
		return project_status_icon;
	} 
	public TextView getStatusText() {
		if (project_status_text == null){
			project_status_text = (TextView) mRow.findViewById(R.id.project_status_text);
		}
		return project_status_text;
	} 

	public void updateView(Project project) {
		if (project != null) {
			int completedTasks = project.getCompletedTasksCount();
			int totalTasks = project.getTasksCount();

			this.getName().setText(project.getName());
			this.getDescription().setText(project.getDescription());
			this.getStatusText().setText(completedTasks + "/" + totalTasks);

			this.getStatusIcon().setImageResource(getProjectStatusIcon(totalTasks, completedTasks));
		}
	}
	
	public static int getProjectStatusIcon(int totalTasks, int completedTasks) {
		float percent = completedTasks / (float)totalTasks * 100;
		
		int imageResource = R.drawable.stat_sys_signal_0;
		if (percent == 0) {
			imageResource = R.drawable.stat_sys_signal_0;
		} else if (percent > 0 && percent < 37.5) {
			imageResource = R.drawable.stat_sys_signal_1;
		} else if (percent >= 37.5 && percent < 62.5) {
			imageResource = R.drawable.stat_sys_signal_2;
		} else if (percent >= 62.5 && percent < 87.5) {
			imageResource = R.drawable.stat_sys_signal_3;
		} else if (percent >= 87.5 && percent <= 100) {
			imageResource = R.drawable.stat_sys_signal_4;
		}

		return imageResource;
	}
}
