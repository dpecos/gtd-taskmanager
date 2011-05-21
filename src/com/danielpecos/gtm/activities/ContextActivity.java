package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.Toast;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.utils.ActivityUtils;
import com.danielpecos.gtm.utils.ExpandableNestedMixedListAdapter;
import com.danielpecos.gtm.utils.ExpandableNestedMixedListAdapter.RowDisplayListener;
import com.danielpecos.gtm.views.ProjectViewHolder;
import com.danielpecos.gtm.views.TaskViewHolder;
import com.danielpecos.gtm.views.ViewHolder;

public class ContextActivity extends ExpandableListActivity implements ExpandableListView.OnChildClickListener {
	private TaskManager taskManager;

	private ViewHolder triggerViewHolder;

	private HashMap<Long, ProjectViewHolder> projectViewHolders;
	private HashMap<Long, TaskViewHolder> taskViewHolders;
	
	private static final String LIST_STATE_KEY = "listState";
	private static final String LIST_POSITION_KEY = "listPosition";
	private static final String ITEM_POSITION_KEY = "itemPosition";

	private Parcelable mListState = null;
	private int mListPosition = 0;
	private int mItemPosition = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//		Log.d(TaskManager.TAG, "Settings: Fijo los valores por defecto");
		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);

		this.taskManager = TaskManager.getInstance(this);

		if (this.taskManager.getContexts().size() == 0) {
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_info)
			.setTitle(R.string.context_loadDemo_title)
			.setMessage(R.string.context_loadDemo_message)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					loadDemoData();
					initializeUI();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					initializeUI();
				}
			})
			.show();

		} else {
			//			this.loadDemoData();
			this.initializeUI();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_activity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.context_optionsMenu_addContext:
			ActivityUtils.showTextBoxDialog(
					this, 
					this.getResources().getString(R.string.textbox_addContext_title), 
					this.getResources().getString(R.string.textbox_addContext_label), 
					null,
					new OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							String contextName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
							if (taskManager.createContext(ContextActivity.this, contextName) != null) {
								initializeUI();
							} else {
								ActivityUtils.showMessage(ContextActivity.this, R.string.error_creatingContext);
							}
						}
					});
			break;
		case R.id.context_optionsMenu_reloadData:
			taskManager = TaskManager.reset(ContextActivity.this);
			initializeUI();
			break;
		case R.id.context_optionsMenu_preferences:
			Intent i = new Intent(this, SettingsActivity.class);  
			startActivity(i);
			break;
		case R.id.context_optionsMenu_about:
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_item_menu, menu);

		int itemId = ((ExpandableListContextMenuInfo) menuInfo).targetView.getId();

		if (itemId == R.id.context_item) {
			menu.setHeaderTitle(R.string.context_contextMenu_contextTitle);
			menu.setHeaderIcon(android.R.drawable.ic_menu_agenda);
			menu.getItem(2).setVisible(false);
			menu.getItem(3).setVisible(false);
			menu.getItem(4).setVisible(false);
		} if (itemId == R.id.project_item) {
			menu.setHeaderTitle(R.string.context_contextMenu_projectTitle);
			menu.setHeaderIcon(R.drawable.ic_menu_archive);
			menu.getItem(0).setVisible(false);
			menu.getItem(1).setVisible(false);
			menu.getItem(4).setVisible(false);
			menu.getItem(5).setVisible(false);
		} else if (itemId == R.id.task_item) {
			menu.setHeaderTitle(R.string.context_contextMenu_taskTitle);
			menu.setHeaderIcon(R.drawable.ic_menu_mark);
			menu.getItem(0).setVisible(false);
			menu.getItem(1).setVisible(false);
			menu.getItem(2).setVisible(false);
			menu.getItem(3).setVisible(false);
			menu.getItem(5).setVisible(false);
			menu.getItem(6).setVisible(false);
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		ExpandableListContextMenuInfo  menuInfo = (ExpandableListContextMenuInfo) item.getMenuInfo();
		int type = ExpandableListView.getPackedPositionType(menuInfo.packedPosition);
		
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition); 
			int childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition); 

			final Context context = taskManager.elementAt(groupPos);
			final Object child = context.elementAt(childPos);

			switch (item.getItemId()) {
			case R.id.context_contextMenu_renameProject: {
				final Project project = (Project)child;
				ActivityUtils.showTextBoxDialog(
						this, 
						this.getResources().getString(R.string.textbox_renameProject_title), 
						this.getResources().getString(R.string.textbox_renameProject_label), 
						project.getName(),
						new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								String projectName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
								project.setName(ContextActivity.this, projectName);
								initializeUI();
							}
						});
				return true;
			}		
			case R.id.context_contextMenu_deleteProject: {
				final Project project = (Project)child;
				String projectName = project.getName();
				
				if (context.deleteProject(ContextActivity.this, project)) {
					initializeUI();
					Toast.makeText(this, "Project \"" + projectName + "\" successfully deleted", Toast.LENGTH_SHORT).show();
				} else {
					ActivityUtils.showMessage(ContextActivity.this, R.string.error_deletingProject);
				}

				return true;
			}
			case R.id.context_contextMenu_deleteTask: {
				final Task task = (Task)child;
				String taskName = task.getName();

				if (context.deleteTask(ContextActivity.this, task)) {
					initializeUI();
					Toast.makeText(this, "Task \"" + taskName + "\" successfully deleted", Toast.LENGTH_SHORT).show();
				} else {
					ActivityUtils.showMessage(ContextActivity.this, R.string.error_deletingTask);
				}

				return true;
			}

			case R.id.context_contextMenu_addTask: {
				final Project project = (Project)child;

				ActivityUtils.showTextBoxDialog(
						this, 
						this.getResources().getString(R.string.textbox_addTask_title), 
						this.getResources().getString(R.string.textbox_addTask_label), 
						null,
						new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								String taskName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
								if (project.createTask(ContextActivity.this, taskName, null, Task.Priority.Normal) != null) {
									initializeUI();
								} else {
									ActivityUtils.showMessage(ContextActivity.this, R.string.error_creatingTask);
								}
							}
						});
				return true;
			}
			}

			return false;

		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition); 

			final Context context = taskManager.elementAt(groupPos);

			switch(item.getItemId()) {
			case R.id.context_contextMenu_renameContext: {
				ActivityUtils.showTextBoxDialog(
						this, 
						this.getResources().getString(R.string.textbox_renameContext_title), 
						this.getResources().getString(R.string.textbox_renameContext_label), 
						context.getName(),
						new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								String contextName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
								context.setName(ContextActivity.this, contextName);
								initializeUI();
							}
						});
				return true;
			}
			case R.id.context_contextMenu_deleteContext: {
				String contextName = context.getName();
				if (taskManager.deleteContext(ContextActivity.this, context)) {
					initializeUI();
					Toast.makeText(this, "Context \"" + contextName + "\" successfully deleted", Toast.LENGTH_SHORT).show();
				} else {
					ActivityUtils.showMessage(ContextActivity.this, R.string.error_deletingContext);
				}
				return true;
			}
			case R.id.context_contextMenu_addProject: {
				ActivityUtils.showTextBoxDialog(
						this, 
						this.getResources().getString(R.string.textbox_addProject_title), 
						this.getResources().getString(R.string.textbox_addProject_label), 
						null,
						new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								String projectName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
								if (context.createProject(ContextActivity.this, projectName, null) != null) {
									initializeUI();
								} else {
									ActivityUtils.showMessage(ContextActivity.this, R.string.error_creatingProject);
								}
							}
						});
				return true;
			}
			case R.id.context_contextMenu_addTask: {
				ActivityUtils.showTextBoxDialog(
						this, 
						this.getResources().getString(R.string.textbox_addTask_title), 
						this.getResources().getString(R.string.textbox_addTask_label), 
						null,
						new OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								String taskName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
								if (context.createTask(ContextActivity.this, taskName, null, Task.Priority.Normal) != null) {
									initializeUI();
								} else {
									ActivityUtils.showMessage(ContextActivity.this, R.string.error_creatingTask);
								}
							}
						});
				return true;
			}
			}
			return false;
		}
		
		return false;
	}

	private void initializeUI() {
		setContentView(R.layout.context_layout);

		ExpandableListView listView = this.getExpandableListView();

		this.projectViewHolders = new HashMap<Long, ProjectViewHolder>();
		this.taskViewHolders = new HashMap<Long, TaskViewHolder>();

		if (taskManager != null) {
			ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
			ArrayList<ArrayList<HashMap<String, Object>>> childrenData_projects =  new ArrayList<ArrayList<HashMap<String, Object>>>();
			ArrayList<ArrayList<HashMap<String, Object>>> childrenData_tasks =  new ArrayList<ArrayList<HashMap<String, Object>>>();

			Collection<Context> contexts = taskManager.getContexts();

			for (Context ctx : contexts) {
				HashMap<String, Object> contextData = new HashMap<String, Object>();
				contextData.put("id", ctx.getId());
				contextData.put("name", ctx.getName());
				groupData.add(contextData);

				// PROJECTS LIST
				ArrayList<HashMap<String, Object>> contextChildData = new ArrayList<HashMap<String,Object>>();
				for (Project project : ctx.getProjects()) {
					ProjectViewHolder pvh = new ProjectViewHolder(null, project);
					this.projectViewHolders.put(project.getId(), pvh);
					HashMap<String, Object> projectData = pvh.getListFields();
					contextChildData.add(projectData);
				}
				childrenData_projects.add(contextChildData);

				// TASKS LIST
				contextChildData = new ArrayList<HashMap<String,Object>>();
				for (final Task task : ctx) {

					TaskViewHolder tvh = new TaskViewHolder(null, task);
					taskViewHolders.put(task.getId(), tvh);

					HashMap<String, Object> taskData = tvh.getListFields();

					contextChildData.add(taskData);
				}
				childrenData_tasks.add(contextChildData);
			}

			this.setListAdapter(new ExpandableNestedMixedListAdapter(
					this, 
					groupData, 
					R.layout.context_item, 
					new String[] {"name"}, 
					new int[] {R.id.context_name}, 

					childrenData_projects, 
					R.layout.project_item, 
					new String[] {"name", "description", "status_text", "status_icon"},
					new int[] {R.id.project_name, R.id.project_description, R.id.project_status_text, R.id.project_status_icon},
					new RowDisplayListener() {
						@Override
						public void onViewSetUp(View view, HashMap<String, Object> data) {
							Project project = (Project)data.get("_BASE_");
							ViewHolder tvh = projectViewHolders.get(project.getId());
							tvh.setView(view);
							tvh.updateView();
						}
					},

					childrenData_tasks, 
					R.layout.task_item, 
					new String[] {"name", "description", "status_check"},
					new int[] {R.id.task_name, R.id.task_description, R.id.task_status_check}, 
					new RowDisplayListener() {
						@Override
						public void onViewSetUp(View view, HashMap<String, Object> data) {
							Task task = (Task)data.get("_BASE_");
							ViewHolder tvh = taskViewHolders.get(task.getId());
							tvh.setView(view);
							tvh.updateView();
						}
					}
			));

			listView.setSelectionFromTop(mListPosition, mItemPosition);
		}

		this.registerForContextMenu(listView);

		listView.setOnChildClickListener(this);
	}


	private void loadDemoData() {
		// Test data 

		Context ctx = this.taskManager.createContext(this, "Context 1");
		Project prj = ctx.createProject(this, "Project 1.1", "Description de proyecto 1.1");
		prj.createTask(this, "Task 1", "Task number 1.1.1", Task.Priority.Critical);
		prj.createTask(this, "Task 2", "Task number 1.1.2", Task.Priority.Important);
		prj.createTask(this, "Task 3", "Task number 1.1.3", Task.Priority.Low);
		prj.createTask(this, "Task 4", "Task number 1.1.4", Task.Priority.Important);
		prj.createTask(this, "Task 5", "Task number 1.1.5", Task.Priority.Critical);

		ctx.createTask(this, "Task 1", "Task number 1.0.1", Task.Priority.Critical);
		ctx.createTask(this, "Task 2", "Task number 1.0.2", Task.Priority.Important);
		ctx.createTask(this, "Task 3", "Task number 1.0.3", Task.Priority.Normal);
		ctx.createTask(this, "Task 4", "Task number 1.0.4", Task.Priority.Low);

		prj = ctx.createProject(this, "Project 1.2", "Description de proyecto 1.2");
		prj.createTask(this, "Task 1", "Task number 1.2.1", Task.Priority.Critical);
		prj.createTask(this, "Task 2", "Task number 1.2.2", Task.Priority.Important);
		prj.createTask(this, "Task 3", "Task number 1.2.3", Task.Priority.Low);
		prj.createTask(this, "Task 4", "Task number 1.2.4", Task.Priority.Important);
		prj.createTask(this, "Task 5", "Task number 1.2.5", Task.Priority.Critical);

		this.taskManager.createContext(this, "Context 2");
		this.taskManager.createContext(this, "Context 3");

	}


	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		boolean result = super.onChildClick(parent, view, groupPosition, childPosition, id);

		Context ctx = taskManager.elementAt(groupPosition);
		Object child = ctx.elementAt(childPosition);

		if (child instanceof Project) {
			long projectId = ((Project) child).getId();
			this.triggerViewHolder = this.projectViewHolders.get(projectId);
			ActivityUtils.showProjectActivity(this, ctx, ctx.getProject(projectId));
		} else {
			long taskId = ((Task) child).getId();
			this.triggerViewHolder = this.taskViewHolders.get(taskId);
			ActivityUtils.showTaskActivity(this, ctx, null, ctx.getTask(taskId));
		}

		return result;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ActivityUtils.PROJECT_ACTIVITY) {
		    if (this.triggerViewHolder != null) {
				ProjectViewHolder projectViewHolder = (ProjectViewHolder) this.triggerViewHolder;
				projectViewHolder.updateView();
			}
		} else if (requestCode == ActivityUtils.TASK_ACTIVITY) {
			if (resultCode == RESULT_OK) {
				// task saved
				if (data.getBooleanExtra(TaskActivity.FULL_RELOAD, false)) {
					this.initializeUI();
				} else {
					TaskViewHolder taskViewHolder = (TaskViewHolder) this.triggerViewHolder;
					taskViewHolder.updateView();
				}
			} else if (this.triggerViewHolder != null) {
				// always refresh view, its status may change 
				TaskViewHolder taskViewHolder = (TaskViewHolder) this.triggerViewHolder;
				taskViewHolder.updateView();
			}
		}

		this.triggerViewHolder = null;
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
	    super.onRestoreInstanceState(state);

	    // Retrieve list state and list/item positions
	    mListState = state.getParcelable(LIST_STATE_KEY);
	    mListPosition = state.getInt(LIST_POSITION_KEY);
	    mItemPosition = state.getInt(ITEM_POSITION_KEY);
	}

	@Override
	protected void onResume() {
	    super.onResume();

	    // Load data from DB and put it onto the list
	    this.taskManager = TaskManager.getInstance(this);

	    // Restore list state and list/item positions
	    ExpandableListView listView = getExpandableListView();
	    if (mListState != null)
	        listView.onRestoreInstanceState(mListState);
	    listView.setSelectionFromTop(mListPosition, mItemPosition);
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
	    super.onSaveInstanceState(state);

	    // Save list state
	    ExpandableListView listView = getExpandableListView();
	    mListState = listView.onSaveInstanceState();
	    state.putParcelable(LIST_STATE_KEY, mListState);

	    // Save position of first visible item
	    mListPosition = listView.getFirstVisiblePosition();
	    state.putInt(LIST_POSITION_KEY, mListPosition);

	    // Save scroll position of item
	    View itemView = listView.getChildAt(0);
	    mItemPosition = itemView == null ? 0 : itemView.getTop();
	    state.putInt(ITEM_POSITION_KEY, mItemPosition);
	}

}
