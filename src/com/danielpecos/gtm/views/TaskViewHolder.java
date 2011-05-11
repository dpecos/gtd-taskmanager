package com.danielpecos.gtm.views;

import java.util.Calendar;
import java.util.HashMap;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

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
		final Resources res = this.view.getResources();

		((TextView)getView(R.id.task_name)).setText(task.getName());
		((TextView)getView(R.id.task_description)).setText(task.getDescription());

		CheckBox check = (CheckBox)getView(R.id.task_status_check);
		check.setChecked(task.getStatus() == Task.Status.Completed || task.getStatus() == Task.Status.Discarded_Completed);
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

		final RatingBar ratingBar = (RatingBar)getView(R.id.task_priority_bar);
		if (ratingBar != null) {
			switch (task.getPriority()) {
			case Low:
				ratingBar.setRating(1.0f);
				break;
			case Normal:
				ratingBar.setRating(2.0f);
				break;
			case Important:
				ratingBar.setRating(3.0f);
				break;
			case Critical:
				ratingBar.setRating(4.0f);
				break;
			}
			ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
				private Toast toast;
				public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
					if (fromUser) {
						
						if (this.toast != null) {
							this.toast.cancel();
						} else {
							this.toast = Toast.makeText(view.getContext(), "", Toast.LENGTH_SHORT);
						}
						
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
						
						String msg = view.getResources().getString(R.string.task_priority_label) + " " + task.getPriority();
						this.toast.setText(msg);
						this.toast.show();
						
						updateView();
					}
				}
			});
		}


		final ToggleButton task_status_discarded = (ToggleButton)getView(R.id.task_status_discarded);
		if (task_status_discarded != null) {
			task_status_discarded.setChecked(task.getStatus() != Task.Status.Discarded && task.getStatus() != Task.Status.Discarded_Completed);
			task_status_discarded.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (!isChecked) {
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

		TextView dueDate = (TextView)getView(R.id.task_duedate);
		if (dueDate != null) { 

			if (task.getDueDate() != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(task.getDueDate());
				int mYear = c.get(Calendar.YEAR);
				int mMonth = c.get(Calendar.MONTH);
				int mDay = c.get(Calendar.DAY_OF_MONTH);

				dueDate.setText(
						new StringBuilder()
						.append(mYear + 1).append("-")
						.append(mMonth).append("-")
						.append(mDay).append(" "));
			}

		}
		
		if (task.getStatus() == Task.Status.Discarded || task.getStatus() == Task.Status.Discarded_Completed) {
			getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityDiscarded);
			((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityDiscarded));

			if (check != null) 
				check.setEnabled(false);
			if (ratingBar != null)
				ratingBar.setEnabled(false);
		} else {
			switch(task.getPriority()) {
			case Low: 
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityLow);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityLow));
				break;
			case Normal:
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityNormal);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityNormal));
				break;
			case Important: 
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityImportant);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityImportant));
				break;
			case Critical: 
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityCritical);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityCritical));
				break;
			}

			if (check != null) 
				check.setEnabled(true);
			if (ratingBar != null)
				ratingBar.setEnabled(true);
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