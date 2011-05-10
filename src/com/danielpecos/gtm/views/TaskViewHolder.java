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
		final Button dueDateButton = (Button)getView(R.id.task_duedate_button);;
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
						.append(mDay).append("-")
						.append(mYear).append(" "));
			}

			final DatePickerDialog.OnDateSetListener dateSetListener =
				new DatePickerDialog.OnDateSetListener() {

				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar c = Calendar.getInstance();
					c.set(Calendar.YEAR, year);
					c.set(Calendar.MONTH, monthOfYear);
					c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					task.setDueDate(c.getTime());

					updateView();
				}
			};

			dueDateButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Calendar c = Calendar.getInstance();
					if (task.getDueDate() != null) 
						c.setTime(task.getDueDate());
					int mYear = c.get(Calendar.YEAR);
					int mMonth = c.get(Calendar.MONTH);
					int mDay = c.get(Calendar.DAY_OF_MONTH);
					new DatePickerDialog(view.getContext(), dateSetListener, mYear, mMonth, mDay).show();
				}
			});
		}
		
		final Button changeNameButton = (Button)getView(R.id.task_changeName_button);
		if (changeNameButton != null) {
			changeNameButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ActivityUtils.showAddDialog(
							view.getContext(), 
							res.getString(R.string.textbox_title_name), 
							res.getString(R.string.textbox_label_name), 
							task.getName(),
							new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							task.setName(((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString());
							updateView();
						}
					});
				}
			});
		}
		
		final Button changeDescriptionButton = (Button)getView(R.id.task_changeDescription_button);;
		if (changeDescriptionButton != null) {
			changeDescriptionButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ActivityUtils.showAddDialog(
							view.getContext(), 
							res.getString(R.string.textbox_title_description), 
							res.getString(R.string.textbox_label_description), 
							task.getDescription(),
							new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							task.setDescription(((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString());
							updateView();
						}
					});
				}
			});
		}
		
		if (task.getStatus() == Task.Status.Discarded || task.getStatus() == Task.Status.Discarded_Completed) {
			getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityDiscarded_Background);
			((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityDiscarded_Foreground));

			if (check != null) 
				check.setEnabled(false);
			if (ratingBar != null)
				ratingBar.setEnabled(false);
			if (dueDateButton != null)
				dueDateButton.setEnabled(false);
			if (changeNameButton != null)
				changeNameButton.setEnabled(false);
			if (changeDescriptionButton != null)
				changeDescriptionButton.setEnabled(false);
		} else {
			switch(task.getPriority()) {
			case Low: 
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityLow_Background);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityLow_Foreground));
				break;
			case Normal:
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityNormal_Background);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityNormal_Foreground));
				break;
			case Important: 
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityImportant_Background);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityImportant_Foreground));
				break;
			case Critical: 
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityCritical_Background);
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityCritical_Foreground));
				break;
			}

			if (check != null) 
				check.setEnabled(true);
			if (ratingBar != null)
				ratingBar.setEnabled(true);
			if (dueDateButton != null)
				dueDateButton.setEnabled(true);
			if (changeNameButton != null)
				changeNameButton.setEnabled(true);
			if (changeDescriptionButton != null)
				changeDescriptionButton.setEnabled(true);
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