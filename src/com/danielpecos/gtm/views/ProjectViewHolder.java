package com.danielpecos.gtm.views;

import java.util.HashMap;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Project;

public class ProjectViewHolder extends ViewHolder {
	private Project project;

	public ProjectViewHolder(View view, Project project) {
		super(view);
		this.project = project;
	}
	
	public Project getProject() {
		return project;
	}

	@Override
	public HashMap<String, Object> getListFields() {
		HashMap<String, Object> projectData = new HashMap<String, Object>();
		
		projectData.put("_BASE_", project);
		projectData.put("id", project.getId());
		projectData.put("name", project.getName());
		projectData.put("description", project.getDescription());
		projectData.put("status_text", project.getCompletedTasksCount() + "/" + (project.getTasksCount() - project.getDiscardedTasksCount()));
		projectData.put("status_icon", this.getProjectStatusIcon(project.getTasksCount(), project.getCompletedTasksCount()));
		
		return projectData;
	}

	@Override
	public void updateView() {
		int completedTasks = project.getCompletedTasksCount();
		int totalTasks = project.getTasksCount() - project.getDiscardedTasksCount();

		((TextView)getView(R.id.project_name)).setText(project.getName());
		((TextView)getView(R.id.project_description)).setText(project.getDescription());
		((TextView)getView(R.id.project_status_text)).setText(completedTasks + "/" + totalTasks);

		((ImageView)getView(R.id.project_status_icon)).setImageResource(this.getProjectStatusIcon(totalTasks, completedTasks));
	}
	
	private int getProjectStatusIcon(int totalTasks, int completedTasks) {
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
