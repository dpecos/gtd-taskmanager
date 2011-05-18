package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;
import com.danielpecos.gtm.model.beans.TaskContainer;
import com.danielpecos.gtm.model.persistence.GTDSQLHelper;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.taskManager = TaskManager.getInstance(this);

//		loadTestData();

		this.initializeUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_options_menu, menu);
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
			return true;
		case R.id.context_optionsMenu_reloadData:
			taskManager = TaskManager.reset(ContextActivity.this);
			initializeUI();
		case R.id.context_optionsMenu_configuration:
		case R.id.context_optionsMenu_about:
		default:
			return super.onOptionsItemSelected(item);
		}
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

		final ExpandableListContextMenuInfo  menuInfo = (ExpandableListContextMenuInfo) item.getMenuInfo();

		//int pos = (int)menuInfo.id;
		//final Context context = (Context)taskManager.getContexts().toArray()[pos];
		//final Context context = taskManager.getContext(menuInfo.t)

		switch (item.getItemId()) {
		case R.id.context_contextMenu_renameContext: {
			final Context context = taskManager.getContext(Long.parseLong(menuInfo.targetView.getContentDescription().toString()));
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
			Context context = taskManager.getContext(Long.parseLong(menuInfo.targetView.getContentDescription().toString()));
			if (taskManager.deleteContext(ContextActivity.this, context)) {
				initializeUI();
			} else {
				ActivityUtils.showMessage(ContextActivity.this, R.string.error_deletingContext);
			}
			return true;
		}
		case R.id.context_contextMenu_addProject: {
			final Context context = taskManager.getContext(Long.parseLong(menuInfo.targetView.getContentDescription().toString()));
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
		case R.id.context_contextMenu_renameProject: {
			long project_id = Long.parseLong(menuInfo.targetView.getContentDescription().toString());
			final Project project = ((ProjectViewHolder)this.projectViewHolders.get(project_id)).getProject();
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
			long project_id = Long.parseLong(menuInfo.targetView.getContentDescription().toString());
			final Project project = ((ProjectViewHolder)this.projectViewHolders.get(project_id)).getProject();
			final Context context = findContextContaining(project);

			if (context.deleteProject(ContextActivity.this, project)) {
				initializeUI();
			} else {
				ActivityUtils.showMessage(ContextActivity.this, R.string.error_deletingProject);
			}

			return true;
		}
		case R.id.context_contextMenu_deleteTask: {
			long task_id = Long.parseLong(menuInfo.targetView.getContentDescription().toString());
			final Task task = ((TaskViewHolder)this.taskViewHolders.get(task_id)).getTask();
			final Context context = findContextContaining(task);

			if (context.deleteTask(ContextActivity.this, task)) {
				initializeUI();
			} else {
				ActivityUtils.showMessage(ContextActivity.this, R.string.error_deletingTask);
			}

			return true;
		}

		case R.id.context_contextMenu_addTask: {
			OnDismissListener listener = null;
			int itemId = ((ExpandableListContextMenuInfo) menuInfo).targetView.getId();
			if (itemId == R.id.context_item) {
				final Context context = taskManager.getContext(Long.parseLong(menuInfo.targetView.getContentDescription().toString()));

				final TaskContainer taskContainer = context;
				listener = new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						String taskName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
						if (taskContainer.createTask(ContextActivity.this, taskName, null, Task.Priority.Normal) != null) {
							initializeUI();
						} else {
							ActivityUtils.showMessage(ContextActivity.this, R.string.error_creatingTask);
						}
					}
				};
			} else if (itemId == R.id.project_item) {
				long project_id = Long.parseLong(menuInfo.targetView.getContentDescription().toString());
				final Project project = ((ProjectViewHolder)this.projectViewHolders.get(project_id)).getProject();

				final TaskContainer taskContainer = project;
				listener = new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						String taskName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
						if (taskContainer.createTask(ContextActivity.this, taskName, null, Task.Priority.Normal) != null) {
							initializeUI();
						} else {
							ActivityUtils.showMessage(ContextActivity.this, R.string.error_creatingTask);
						}
					}
				};
			}
			ActivityUtils.showTextBoxDialog(
					this, 
					this.getResources().getString(R.string.textbox_addTask_title), 
					this.getResources().getString(R.string.textbox_addTask_label), 
					null,
					listener);
			return true;
		}
		default: {
			return false;
		}
		}
	}

	private Context findContextContaining(Task task) {
		for (Context c : taskManager.getContexts()) {
			if (c.getTask(task.getId()) != null) {
				return c;
			}
		}
		return null;
	}

	private Context findContextContaining(Project project) {
		for (Context c : taskManager.getContexts()) {
			if (c.getProject(project.getId()) != null) {
				return c;
			}
		}
		return null;
	}

	private void initializeUI() {
		setContentView(R.layout.context_layout);

		this.projectViewHolders = new HashMap<Long, ProjectViewHolder>();
		this.taskViewHolders = new HashMap<Long, TaskViewHolder>();

		if (taskManager != null) {
			ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
			ArrayList<ArrayList<HashMap<String, Object>>> childrenData_projects =  new ArrayList<ArrayList<HashMap<String, Object>>>();
			ArrayList<ArrayList<HashMap<String, Object>>> childrenData_tasks =  new ArrayList<ArrayList<HashMap<String, Object>>>();
			ArrayList<ArrayList<HashMap<String, Object>>> childrenEvents_tasks =  new ArrayList<ArrayList<HashMap<String, Object>>>();

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
				ArrayList<HashMap<String, Object>> contextChildEvents = new ArrayList<HashMap<String,Object>>();
				for (final Task task : ctx) {

					TaskViewHolder tvh = new TaskViewHolder(null, task);
					taskViewHolders.put(task.getId(), tvh);

					HashMap<String, Object> taskData = tvh.getListFields();
					HashMap<String, Object> taskEvents = tvh.getListEvents();

					contextChildData.add(taskData);
					contextChildEvents.add(taskEvents);
				}
				childrenData_tasks.add(contextChildData);
				childrenEvents_tasks.add(contextChildEvents);
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
					null, 
					new RowDisplayListener() {
						@Override
						public void onViewSetUp(View view, HashMap<String, Object> data) {
							Project project = (Project)data.get("_BASE_");
							ViewHolder tvh = projectViewHolders.get(project.getId());
							tvh.setView(view);
							//tvh.updateView();
						}
					},

					childrenData_tasks, 
					R.layout.task_item, 
					new String[] {"name", "description", "status_check"},
					new int[] {R.id.task_name, R.id.task_description, R.id.task_status_check}, 
					childrenEvents_tasks, 
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

			this.getExpandableListView().expandGroup(0);
		}

		this.registerForContextMenu(this.getExpandableListView());

		this.getExpandableListView().setOnChildClickListener(this);
	}


	private void loadTestData() {
		// Test data

		Context ctx = this.taskManager.createContext(this, "Contexto 1");
		Project prj = ctx.createProject(this, "Proyecto 1.1", "Descripción de proyecto 1.1");
		prj.createTask(this, "Tarea 1", "Tarea num 1.1.1", Task.Priority.Critical);
		prj.createTask(this, "Tarea 2", "Tarea num 1.1.2", Task.Priority.Important);
		prj.createTask(this, "Tarea 3", "Tarea num 1.1.3", Task.Priority.Low);
		prj.createTask(this, "Tarea 4", "Tarea num 1.1.4", Task.Priority.Important);
		prj.createTask(this, "Tarea 5", "Tarea num 1.1.5", Task.Priority.Critical);

		ctx.createTask(this, "Tarea 1", "Tarea num 1.0.1", Task.Priority.Critical);
		ctx.createTask(this, "Tarea 2", "Tarea num 1.0.2", Task.Priority.Important);
		ctx.createTask(this, "Tarea 3", "Tarea num 1.0.3", Task.Priority.Normal);
		ctx.createTask(this, "Tarea 4", "Tarea num 1.0.4", Task.Priority.Low);

		prj = ctx.createProject(this, "Proyecto 1.2", "Descripción de proyecto 1.2");
		prj.createTask(this, "Tarea 1", "Tarea num 1.2.1", Task.Priority.Critical);
		prj.createTask(this, "Tarea 2", "Tarea num 1.2.2", Task.Priority.Important);
		prj.createTask(this, "Tarea 3", "Tarea num 1.2.3", Task.Priority.Low);
		prj.createTask(this, "Tarea 4", "Tarea num 1.2.4", Task.Priority.Important);
		prj.createTask(this, "Tarea 5", "Tarea num 1.2.5", Task.Priority.Critical);

		this.taskManager.createContext(this, "Contexto 2");
		this.taskManager.createContext(this, "Contexto 3");

	}


	@Override
	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
		boolean result = super.onChildClick(parent, view, groupPosition, childPosition, id);

		Context ctx = taskManager.elementAt(groupPosition);

		if (childPosition < ctx.getProjects().size()) {
			long projectId = Long.parseLong(view.getContentDescription().toString());
			this.triggerViewHolder = this.projectViewHolders.get(projectId);
			ActivityUtils.showProjectActivity(this, ctx, ctx.getProject(projectId));
		} else {
			long taskId = Long.parseLong(view.getContentDescription().toString());
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
			if (this.triggerViewHolder != null) {

				TaskViewHolder taskViewHolder = (TaskViewHolder) this.triggerViewHolder;
				taskViewHolder.updateView();
			}
		}

		this.triggerViewHolder = null;
	}
}
