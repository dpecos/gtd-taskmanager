package com.danielpecos.gtm.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.model.persistence.GTDSQLHelper;

public class TaskViewHolder extends ViewHolder {
	private Task task;

	private TextView textView_taskName;
	private TextView textView_taskDescription;
	private CheckBox checkbox_taskStatus;
	private Spinner spinner_taskPriority;
	private ToggleButton toggleButton_taskDiscarded;
	private TextView textView_taskDueDate;
	private List<TextView> textViews_labels;

	public TaskViewHolder(View view, Task task) {
		super(view);
		this.task = task;
	}

	public Task getTask() {
		return task;
	}

	@Override
	public HashMap<String, Object> getListFields() {
		HashMap<String, Object> taskData = new HashMap<String, Object>();

		taskData.put("_BASE_", task);
		taskData.put("id", task.getId());
		taskData.put("name", task.getName());
		taskData.put("description", task.getDescription());
		taskData.put("status_check", task.getStatus() == Task.Status.Completed || task.getStatus() == Task.Status.Discarded_Completed);
		taskData.put("status", Task.Status.Completed);
		taskData.put("priority", task.getPriority());

		return taskData;
	}

	private void setUpView() {
		this.textViews_labels = new ArrayList<TextView>();
		this.textViews_labels.add((TextView)getView(R.id.task_priority_label));
		this.textViews_labels.add((TextView)getView(R.id.task_duedate_label));
		this.textViews_labels.add((TextView)getView(R.id.task_status_label));

		this.textView_taskName = (TextView)getView(R.id.task_name);
		this.textView_taskDescription = (TextView)getView(R.id.task_description);
		this.checkbox_taskStatus = (CheckBox)getView(R.id.task_status_check);
		this.checkbox_taskStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (task.getStatus() == Task.Status.Discarded) {
						task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Discarded_Completed);
					} else {
						task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Completed);
					}
				} else {
					if (task.getStatus() == Task.Status.Discarded_Completed) {
						task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Discarded);
					} else {
						task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Active);
					}
				}

				if (viewListeners != null && viewListeners.get("status_check") != null) {
					((OnCheckedChangeListener)viewListeners.get("status_check")).onCheckedChanged(buttonView, isChecked);
				}

				updateView();
			}
		});

		this.spinner_taskPriority = (Spinner)getView(R.id.task_priority_spinner);
		if (this.spinner_taskPriority != null) {
			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.task_priorities, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			this.spinner_taskPriority.setAdapter(adapter);
			this.spinner_taskPriority.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

					switch (pos) {
					case 0:
						if (task.getPriority() != Task.Priority.Low) {
							task.setPriority(GTDSQLHelper.getInstance(view.getContext()), Task.Priority.Low);
						}
						break;
					case 1:
						if (task.getPriority() != Task.Priority.Normal) {
							task.setPriority(GTDSQLHelper.getInstance(view.getContext()), Task.Priority.Normal);
						}
						break;
					case 2:
						if (task.getPriority() != Task.Priority.Important) {
							task.setPriority(GTDSQLHelper.getInstance(view.getContext()), Task.Priority.Important);
						}
						break;
					case 3:
						if (task.getPriority() != Task.Priority.Critical) {
							task.setPriority(GTDSQLHelper.getInstance(view.getContext()), Task.Priority.Critical);
						}
						break;
					}

					updateView();
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
		}

		this.toggleButton_taskDiscarded = (ToggleButton)getView(R.id.task_status_discarded);
		if (this.toggleButton_taskDiscarded != null) {
			this.toggleButton_taskDiscarded.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (!isChecked) {
						if (task.getStatus() == Task.Status.Completed) {
							task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Discarded_Completed);
						} else {
							task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Discarded);
						}
					} else {
						if (task.getStatus() == Task.Status.Discarded_Completed) {
							task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Completed);
						} else {
							task.setStatus(GTDSQLHelper.getInstance(view.getContext()), Task.Status.Active);
						}
					}
					updateView();
				}
			});
		}

		this.textView_taskDueDate = (TextView)getView(R.id.task_duedate);
	}

	@Override
	public void updateView() {

		if (this.textView_taskName == null) {
			this.setUpView();
		}

		final Resources res = this.view.getResources();

		this.textView_taskName.setText(task.getName());
		this.textView_taskDescription.setText(task.getDescription());
		this.checkbox_taskStatus.setChecked(task.getStatus() == Task.Status.Completed || task.getStatus() == Task.Status.Discarded_Completed);

		// this line is required to force the UI to update the checkbox view when using a real device
		this.checkbox_taskStatus.requestLayout();

		if (this.spinner_taskPriority != null) {
			if (task.getStatus() == Task.Status.Discarded || task.getStatus() == Task.Status.Discarded_Completed) {
				for (TextView label: this.textViews_labels) {
					label.setTextColor(res.getColor(R.color.Task_PriorityDiscarded));
				}
			} else {
				switch (task.getPriority()) {
				case Low:
					this.spinner_taskPriority.setSelection(0);
					for (TextView label: this.textViews_labels) {
						label.setTextColor(res.getColor(R.color.Task_PriorityLow));
					}
					break;
				case Normal:
					this.spinner_taskPriority.setSelection(1);
					for (TextView label: this.textViews_labels) {
						label.setTextColor(res.getColor(R.color.Task_PriorityNormal));
					}
					break;
				case Important:
					this.spinner_taskPriority.setSelection(2);
					for (TextView label: this.textViews_labels) {
						label.setTextColor(res.getColor(R.color.Task_PriorityImportant));
					}
					break;
				case Critical:
					this.spinner_taskPriority.setSelection(3);
					for (TextView label: this.textViews_labels) {
						label.setTextColor(res.getColor(R.color.Task_PriorityCritical));
					}
					break;
				}
			}

		}

		if (this.toggleButton_taskDiscarded != null) {
			this.toggleButton_taskDiscarded.setChecked(task.getStatus() != Task.Status.Discarded && task.getStatus() != Task.Status.Discarded_Completed);
		}

		if (this.textView_taskDueDate != null) { 

			if (task.getDueDate() != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(task.getDueDate());
				int mYear = c.get(Calendar.YEAR);
				int mMonth = c.get(Calendar.MONTH);
				int mDay = c.get(Calendar.DAY_OF_MONTH);

				this.textView_taskDueDate.setText(
						new StringBuilder()
						.append(mYear + 1).append("-")
						.append(mMonth).append("-")
						.append(mDay).append(" "));
			}

		}

		if (task.getStatus() == Task.Status.Discarded || task.getStatus() == Task.Status.Discarded_Completed) {
			getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityDiscarded);
			((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityDiscarded));

			if (this.checkbox_taskStatus != null) 
				this.checkbox_taskStatus.setEnabled(false);
			if (this.spinner_taskPriority != null)
				this.spinner_taskPriority.setEnabled(false);
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

			if (this.checkbox_taskStatus != null) 
				this.checkbox_taskStatus.setEnabled(true);
			if (this.spinner_taskPriority != null)
				this.spinner_taskPriority.setEnabled(true);
		}

		this.view.requestLayout();
	}

	public HashMap<String, Object> getListEvents(Object ... params) {
		if (this.viewListeners == null) {
			final ProjectViewHolder projectViewHolder = (params != null && params.length > 0) ? (ProjectViewHolder) params[0] : null;
			this.viewListeners = new HashMap<String, Object>();

			this.viewListeners.put("status_check", new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					if (projectViewHolder != null) {
						projectViewHolder.updateView();
					}
				}
			});
		}
		return this.viewListeners;
	}

}