package com.danielpecos.gtm.views;

import java.util.HashMap;

import android.content.res.Resources;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.utils.ActivityUtils;

public class TaskViewHolder extends ViewHolder {
	private Task task;

	public TaskViewHolder(View view, Task task) {
		super(view);
		this.task = task;
	}

	@Override
	public HashMap<String, Object> getListFields() {
		HashMap<String, Object> taskData = new HashMap<String, Object>();

		taskData.put("_BASE_", task);
		taskData.put("name", task.getName());
		taskData.put("description", task.getDescription());
		taskData.put("status_check", task.getStatus() == Task.Status.Completed || task.getStatus() == Task.Status.Discarded_Completed);
		taskData.put("status", Task.Status.Completed);
		taskData.put("priority", task.getPriority());

		return taskData;
	}

	@Override
	public void updateView() {
		Resources r = this.view.getResources();

		((TextView)getView(R.id.task_name)).setText(task.getName());
		((TextView)getView(R.id.task_description)).setText(task.getDescription());

		CheckBox check = (CheckBox)getView(R.id.task_status_check);
		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (task.getStatus() == Task.Status.Discarded) {
						task.setStatus(Task.Status.Discarded_Completed);
					} else {
						task.setStatus(Task.Status.Completed);
					}
				} else {
					if (task.getStatus() == Task.Status.Discarded_Completed) {
						task.setStatus(Task.Status.Discarded);
					} else {
						task.setStatus(Task.Status.Active);
					}
				}
				updateView();
			}
		});
		check.setChecked(task.getStatus() == Task.Status.Completed || task.getStatus() == Task.Status.Discarded_Completed);

		final RatingBar ratingbar = (RatingBar)getView(R.id.task_priority_bar);
		if (ratingbar != null) {
			ratingbar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
				public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
					if (fromUser) {
						switch((int)rating) {
						case 1:
							task.setPriority(Task.Priority.Low);
							break;
						case 2:
							task.setPriority(Task.Priority.Normal);
							break;
						case 3:
							task.setPriority(Task.Priority.Important);
							break;
						case 4:
							task.setPriority(Task.Priority.Critical);
							break;
						}
						updateView();
					}
				}
			});
			switch (task.getPriority()) {
			case Low:
				ratingbar.setRating(1.0f);
				break;
			case Normal:
				ratingbar.setRating(2.0f);
				break;
			case Important:
				ratingbar.setRating(3.0f);
				break;
			case Critical:
				ratingbar.setRating(4.0f);
				break;
			}
		}


		final ToggleButton task_status_discarded = (ToggleButton)getView(R.id.task_status_discarded);
		if (task_status_discarded != null) {
			task_status_discarded.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						if (task.getStatus() == Task.Status.Completed) {
							task.setStatus(Task.Status.Discarded_Completed);
						} else {
							task.setStatus(Task.Status.Discarded);
						}
					} else {
						if (task.getStatus() == Task.Status.Discarded_Completed) {
							task.setStatus(Task.Status.Completed);
						} else {
							task.setStatus(Task.Status.Active);
						}
					}
					updateView();
				}
			});
		}

		switch(task.getPriority()) {
		case Low: 
			getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityLow_Background);
			((TextView)getView(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityLow_Foreground));
			break;
		case Normal:
			getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityNormal_Background);
			((TextView)getView(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityNormal_Foreground));
			break;
		case Important: 
			getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityImportant_Background);
			((TextView)getView(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityImportant_Foreground));
			break;
		case Critical: 
			getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityCritical_Background);
			((TextView)getView(R.id.task_name)).setTextColor(r.getColor(R.color.Task_PriorityCritical_Foreground));
			break;
		}

		this.view.requestLayout();
	}

	public HashMap<String, Object> getListEvents(final ProjectViewHolder projectViewHolder) {
		HashMap<String, Object> taskEvents = new HashMap<String, Object>();

		taskEvents.put("status_check", new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				task.setStatus(isChecked ? Task.Status.Completed : Task.Status.Active);
				if (projectViewHolder != null) {
					projectViewHolder.updateView();
				}
				// this line is required to force the UI to update the checkbox view when using a real device
				buttonView.requestLayout();
			}
		});

		return taskEvents;
	}

}
