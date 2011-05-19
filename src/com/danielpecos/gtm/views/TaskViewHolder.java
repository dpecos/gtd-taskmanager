package com.danielpecos.gtm.views;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.activities.CameraActivity;
import com.danielpecos.gtm.model.beans.Task;

public class TaskViewHolder extends ViewHolder {
	private Task task;

	private TextView textView_taskName;
	private EditText editText_taskName;
	private TextView textView_taskDescription;
	private EditText editText_taskDescription;
	private Button button_taskDescriptionClear;
	private CheckBox checkbox_taskStatus;
	private Spinner spinner_taskPriority;
	private ToggleButton toggleButton_taskDiscarded;
	private TextView textView_taskDueDate;
	private Button button_changeDueDate;
	private TextView textView_taskDueTime;
	private Button button_changeDueTime;
	private Button button_takePicture;

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
		if (getView(R.id.task_priority_label) != null) { 
			this.textViews_labels.add((TextView)getView(R.id.task_priority_label));
		}
		if (getView(R.id.task_description_label) != null) {
			this.textViews_labels.add((TextView)getView(R.id.task_description_label));
		}
		if (getView(R.id.task_status_label) != null) {
			this.textViews_labels.add((TextView)getView(R.id.task_status_label));
		}
		if (getView(R.id.task_duedate_label) != null) {
			this.textViews_labels.add((TextView)getView(R.id.task_duedate_label));
		}
		if (getView(R.id.task_duetime_label) != null) {
			this.textViews_labels.add((TextView)getView(R.id.task_duetime_label));
		}
		if (getView(R.id.task_picture_label) != null) {
			this.textViews_labels.add((TextView)getView(R.id.task_picture_label));
		}

		if (getView(R.id.task_name) != null) {
			this.textView_taskName = (TextView)getView(R.id.task_name);
		} else {
			this.editText_taskName = (EditText)getView(R.id.task_name_text);
			if (this.editText_taskName != null) {
				this.editText_taskName.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						task.setName(editText_taskName.getText().toString());
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					}
					@Override
					public void afterTextChanged(Editable s) {
					}
				});
			}
		}
		if (getView(R.id.task_description) != null) {
			this.textView_taskDescription = (TextView)getView(R.id.task_description);
		} else {
			this.editText_taskDescription = (EditText)getView(R.id.task_description_text);
			if (this.editText_taskDescription != null) {
				this.editText_taskDescription.addTextChangedListener(new TextWatcher() {
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						task.setDescription(editText_taskDescription.getText().toString());
					}
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
					}
					@Override
					public void afterTextChanged(Editable s) {
					}
				});
				this.button_taskDescriptionClear = (Button)getView(R.id.task_description_clear);
				this.button_taskDescriptionClear.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						editText_taskDescription.setText("");
					}
				});
			}
		}

		this.checkbox_taskStatus = (CheckBox)getView(R.id.task_status_check);
		if (this.checkbox_taskStatus != null) {
			this.checkbox_taskStatus.setOnCheckedChangeListener(new OnCheckedChangeListener() {
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

					if (viewListeners != null && viewListeners.get("status_check") != null) {
						((OnCheckedChangeListener)viewListeners.get("status_check")).onCheckedChanged(buttonView, isChecked);
					}

					updateView();
				}
			});
		}

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
							task.setPriority(Task.Priority.Low);
						}
						break;
					case 1:
						if (task.getPriority() != Task.Priority.Normal) {
							task.setPriority(Task.Priority.Normal);
						}
						break;
					case 2:
						if (task.getPriority() != Task.Priority.Important) {
							task.setPriority(Task.Priority.Important);
						}
						break;
					case 3:
						if (task.getPriority() != Task.Priority.Critical) {
							task.setPriority(Task.Priority.Critical);
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
							task.setStatus(Task.Status.Discarded_Completed);
						} else if (task.getStatus() == Task.Status.Active) {
							task.setStatus(Task.Status.Discarded);
						}
					} else {
						if (task.getStatus() == Task.Status.Discarded_Completed) {
							task.setStatus(Task.Status.Completed);
						} else if (task.getStatus() == Task.Status.Discarded) {
							task.setStatus(Task.Status.Active);
						}
					}
					updateView();
				}
			});
		}

		this.textView_taskDueDate = (TextView)getView(R.id.task_duedate);
		this.button_changeDueDate = (Button)getView(R.id.button_changeDueDate);
		if (this.button_changeDueDate != null) {
			this.button_changeDueDate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Calendar c = Calendar.getInstance();
					if (task.getDueDate() != null) 
						c.setTime(task.getDueDate());
					int mYear = c.get(Calendar.YEAR);
					int mMonth = c.get(Calendar.MONTH);
					int mDay = c.get(Calendar.DAY_OF_MONTH);
					new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {

						public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							Calendar c = Calendar.getInstance();
							if (task.getDueDate() != null) {
								c.setTime(task.getDueDate());
							} else {
								c.set(Calendar.HOUR_OF_DAY, 0);
								c.set(Calendar.MINUTE, 0);
							}
							c.set(Calendar.YEAR, year);
							c.set(Calendar.MONTH, monthOfYear);
							c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							task.setDueDate(c.getTime());

							updateView();
						}
					}, mYear, mMonth, mDay).show();

				}
			});
		}

		this.textView_taskDueTime = (TextView)getView(R.id.task_duetime);
		this.button_changeDueTime = (Button)getView(R.id.button_changeDueTime);
		if (this.button_changeDueTime != null) {
			this.button_changeDueTime.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Calendar c = Calendar.getInstance();
					if (task.getDueDate() != null) {
						c.setTime(task.getDueDate());
					} else {
						c.set(Calendar.MINUTE, 0);
					}
					int mMinute = c.get(Calendar.MINUTE);
					int mHour = c.get(Calendar.HOUR_OF_DAY);
					new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
						@Override
						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
							Calendar c = Calendar.getInstance();
							if (task.getDueDate() != null) {
								c.setTime(task.getDueDate());
							} else {
								c.set(Calendar.YEAR, c.get(Calendar.YEAR));
								c.set(Calendar.MONTH, c.get(Calendar.MONTH));
								c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
							}
							c.set(Calendar.HOUR_OF_DAY, hourOfDay);
							c.set(Calendar.MINUTE, minute);
							task.setDueDate(c.getTime());

							updateView();
						}
					}, mHour, mMinute, true).show();

				}
			});
		}

		this.button_takePicture = (Button)getView(R.id.task_take_picture);
		if (this.button_takePicture != null) {
			this.button_takePicture.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(v.getContext(), CameraActivity.class);    	    	
					v.getContext().startActivity(i);
				}
			});
		}
	}

	@Override
	public void updateView() {

		if (this.textView_taskName == null && this.editText_taskName == null) {
			this.setUpView();
		}

		final Resources res = this.view.getResources();

		if (this.textView_taskName != null || this.editText_taskName != null) {
			if (this.editText_taskName != null) {
				this.editText_taskName.setText(task.getName());
			} else if (this.textView_taskName != null){
				this.textView_taskName.setText(task.getName());
			}
			if (task.getDescription() != null && !task.getDescription().equalsIgnoreCase("")) {
				if (this.editText_taskDescription != null) {
					this.editText_taskDescription.setText(task.getDescription());
				} else if (this.textView_taskDescription != null) {
					this.textView_taskDescription.setText(task.getDescription());
				}
			}

			if (this.textView_taskName != null && this.textView_taskDescription != null) {
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				lp.addRule(RelativeLayout.RIGHT_OF, R.id.task_status_check);
				if (task.getDescription() == null || task.getDescription().equalsIgnoreCase("")) {
					this.textView_taskDescription.setVisibility(View.GONE);
					lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
				} else {
					this.textView_taskDescription.setVisibility(View.VISIBLE);
					this.textView_taskName.setPadding(0, 7, 0, 0);
				}
				this.textView_taskName.setLayoutParams(lp);
			}
		}

		if (this.checkbox_taskStatus != null) {
			this.checkbox_taskStatus.setChecked(task.getStatus() == Task.Status.Completed || task.getStatus() == Task.Status.Discarded_Completed);

			this.checkbox_taskStatus.requestLayout();
		}

		if (this.spinner_taskPriority != null) {
			switch (task.getPriority()) {
			case Low:
				this.spinner_taskPriority.setSelection(0);
				break;
			case Normal:
				this.spinner_taskPriority.setSelection(1);
				break;
			case Important:
				this.spinner_taskPriority.setSelection(2);
				break;
			case Critical:
				this.spinner_taskPriority.setSelection(3);
				break;
			}

		}

		if (this.toggleButton_taskDiscarded != null) {
			this.toggleButton_taskDiscarded.setChecked(task.getStatus() != Task.Status.Discarded && task.getStatus() != Task.Status.Discarded_Completed);
		}

		if (this.textView_taskDueDate != null && this.textView_taskDueTime != null) { 

			if (task.getDueDate() != null) {
				Calendar c = Calendar.getInstance();
				c.setTime(task.getDueDate());

				int mYear = c.get(Calendar.YEAR);
				int mMonth = c.get(Calendar.MONTH);
				int mDay = c.get(Calendar.DAY_OF_MONTH);
				int mHour = c.get(Calendar.HOUR_OF_DAY);
				int mMinute = c.get(Calendar.MINUTE);

				this.textView_taskDueDate.setText(
						new StringBuilder()
						.append(mYear + 1).append("/")
						.append(mMonth <= 9 ? "0" + mMonth : mMonth).append("/")
						.append(mDay <= 9 ? "0" + mDay : mDay));

				this.textView_taskDueTime.setText(
						new StringBuilder()
						.append(mHour <= 9 ? "0" + mHour : mHour).append(":")
						.append(mMinute <= 9 ? "0" + mMinute : mMinute));
			}

		}

		if (task.getStatus() == Task.Status.Discarded || task.getStatus() == Task.Status.Discarded_Completed) {
			if (getView(R.id.task_priority) != null) {
				getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityDiscarded);
			}
			if (getView(R.id.task_priority_big) != null) {
				getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityDiscarded);
			}
			if (getView(R.id.task_name) != null) {
				((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityDiscarded));
			}
			for (TextView label: this.textViews_labels) {
				label.setTextColor(res.getColor(R.color.Task_PriorityDiscarded));
			}

			if (this.checkbox_taskStatus != null) 
				this.checkbox_taskStatus.setEnabled(false);
			if (this.spinner_taskPriority != null)
				this.spinner_taskPriority.setEnabled(false);
			if (this.button_changeDueDate != null)
				this.button_changeDueDate.setEnabled(false);
			if (this.button_changeDueTime != null)
				this.button_changeDueTime.setEnabled(false);
		} else {
			switch(task.getPriority()) {
			case Low: 
				if (getView(R.id.task_priority) != null) {
					getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityLow);
				}
				if (getView(R.id.task_priority_big) != null) {
					getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityLow);
				}
				if (getView(R.id.task_name) != null) {
					((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityLow));
				}
				for (TextView label: this.textViews_labels) {
					label.setTextColor(res.getColor(R.color.Task_PriorityLow));
				}
				break;
			case Normal:
				if (getView(R.id.task_priority) != null) {
					getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityNormal);
				}
				if (getView(R.id.task_priority_big) != null) {
					getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityNormal);
				}
				if (getView(R.id.task_name) != null) {
					((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityNormal));
				}
				for (TextView label: this.textViews_labels) {
					label.setTextColor(res.getColor(R.color.Task_PriorityNormal));
				}
				break;
			case Important: 
				if (getView(R.id.task_priority) != null) {
					getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityImportant);
				}
				if (getView(R.id.task_priority_big) != null) {
					getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityImportant);
				}
				if (getView(R.id.task_name) != null) {
					((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityImportant));
				}
				for (TextView label: this.textViews_labels) {
					label.setTextColor(res.getColor(R.color.Task_PriorityImportant));
				}
				break;
			case Critical: 
				if (getView(R.id.task_priority) != null) {
					getView(R.id.task_priority).setBackgroundResource(R.color.Task_PriorityCritical);
				}
				if (getView(R.id.task_priority_big) != null) {
					getView(R.id.task_priority_big).setBackgroundResource(R.color.Task_PriorityCritical);
				}
				if (getView(R.id.task_name) != null) {
					((TextView)getView(R.id.task_name)).setTextColor(res.getColor(R.color.Task_PriorityCritical));
				}
				for (TextView label: this.textViews_labels) {
					label.setTextColor(res.getColor(R.color.Task_PriorityCritical));
				}
				break;
			}

			if (this.checkbox_taskStatus != null) 
				this.checkbox_taskStatus.setEnabled(true);
			if (this.spinner_taskPriority != null)
				this.spinner_taskPriority.setEnabled(true);
			if (this.button_changeDueDate != null)
				this.button_changeDueDate.setEnabled(true);
			if (this.button_changeDueTime != null)
				this.button_changeDueTime.setEnabled(true);
			if (this.button_takePicture != null)
				this.button_takePicture.setEnabled(true);
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