package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.utils.ExpandableNestedMixedListAdapter.RowDisplayListener;
import com.danielpecos.gtm.utils.SimpleListAdapter;
import com.danielpecos.gtm.views.ProjectViewHolder;
import com.danielpecos.gtm.views.TaskViewHolder;
import com.danielpecos.gtm.views.ViewHolder;

public class ProjectActivity extends ListActivity {

	private TaskManager taskManager;
	private Context context;
	private Project project;

	private ViewHolder triggerViewHolder;

	private ProjectViewHolder projectViewHolder;
	private HashMap<Long, TaskViewHolder> taskViewHolders;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.taskManager = TaskManager.getInstance(this);

		Long context_id = (Long) getIntent().getSerializableExtra("context_id");
		Long project_id = (Long) getIntent().getSerializableExtra("project_id");

		this.context = this.taskManager.getContext(context_id);
		this.project = this.context.getProject(project_id);

		this.initializeUI();

		setResult(RESULT_OK, getIntent());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.project_activity_menu, menu);
		return true;
	}


	private void initializeUI() {
		setContentView(R.layout.project_layout);

		this.setTitle(project.getName());

		// PROJECT 
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		View projectItemView = mInflater.inflate(R.layout.project_item, null);
		projectItemView.setMinimumHeight(projectItemView.getMeasuredHeight() + 8);
		projectItemView.setPadding(0, 6, 0, 0);

		LinearLayout header = (LinearLayout)findViewById(R.id.header);
		header.addView(projectItemView);

		this.projectViewHolder = new ProjectViewHolder(projectItemView, project);
//		((TextView)projectViewHolder.getView(R.id.project_name)).setTextSize(((TextView)projectViewHolder.getView(R.id.project_name)).getTextSize() + 4);
//		((TextView)projectViewHolder.getView(R.id.project_description)).setTextSize(((TextView)projectViewHolder.getView(R.id.project_description)).getTextSize() + 4);

		findViewById(R.id.project_details).setVisibility(View.INVISIBLE);
		//findViewById(R.id.project_details).setVisibility(View.GONE);

		projectViewHolder.updateView(this);

		//projectItemView.setTag(projectViewHolder);

		// TASKS LIST
		this.taskViewHolders = new HashMap<Long, TaskViewHolder>();

		ArrayList<HashMap<String, Object>> itemsData = new ArrayList<HashMap<String, Object>>();
		for (final Task task : project) {

			TaskViewHolder tvh = new TaskViewHolder(null, task);

			tvh.registerChainedFieldEvents(R.id.task_status_check, new Object[] {
					new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
							if (projectViewHolder != null) {
								projectViewHolder.updateView(ProjectActivity.this);
							}
						}
					}
			});

			taskViewHolders.put(task.getId(), tvh);

			HashMap<String, Object> taskData = tvh.getListFields();

			itemsData.add(taskData);
		}

		SimpleListAdapter adapter = new SimpleListAdapter(
				this,
				itemsData, 
				R.layout.task_item, 
				new String[] {"name", "description", "status_check"},
				new int[] {R.id.task_name, R.id.task_description, R.id.task_status_check},
				new RowDisplayListener() {
					@Override
					public void onViewSetUp(View view, HashMap<String, Object> data) {
						Task task = (Task)data.get("_BASE_");
						ViewHolder tvh = taskViewHolders.get(task.getId());
						tvh.setView(view);
						tvh.updateView(ProjectActivity.this);
					}
				}
		);

		this.setListAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.project_optionsMenu_renameProject: {
			ActivityUtils.showTextBoxDialog(
					this, 
					this.getResources().getString(R.string.textbox_renameProject_title), 
					this.getResources().getString(R.string.textbox_renameProject_label), 
					project.getName(),
					new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							String projectName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
							project.setName(projectName);
							project.store(ProjectActivity.this);
							projectViewHolder.updateView(ProjectActivity.this);
						}
					});
			break;
		}
		case R.id.project_optionsMenu_changeDescription: {
			ActivityUtils.showTextBoxDialog(
					this, 
					this.getResources().getString(R.string.textbox_changeProjectDescription_title), 
					this.getResources().getString(R.string.textbox_changeProjectDescription_label), 
					project.getDescription(),
					new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							String projectDescription = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
							project.setDescription(projectDescription);
							project.store(ProjectActivity.this);
							projectViewHolder.updateView(ProjectActivity.this);
						}
					});
			break;
		}
		}
		return true;
	}

	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		super.onListItemClick(parent, view, position, id);

		Task task = project.elementAt(position);
		this.triggerViewHolder = this.taskViewHolders.get(task.getId());
		ActivityUtils.showTaskActivity(this, this.context, this.project, task);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ActivityUtils.TASK_ACTIVITY) {
			if (resultCode == RESULT_OK) {
				// task saved
				if (data.getBooleanExtra(TaskActivity.FULL_RELOAD, false)) {
					this.initializeUI();
				} else {
					TaskViewHolder taskViewHolder = (TaskViewHolder) this.triggerViewHolder;
					taskViewHolder.updateView(this);
				}
			} else if (this.triggerViewHolder != null) {
				// always refresh view, its status may change 
				TaskViewHolder taskViewHolder = (TaskViewHolder) this.triggerViewHolder;
				taskViewHolder.updateView(this);
			}
		}

		this.triggerViewHolder = null;
	}
}