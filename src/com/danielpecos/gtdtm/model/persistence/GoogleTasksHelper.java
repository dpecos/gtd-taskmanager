package com.danielpecos.gtdtm.model.persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.app.Activity;
import android.util.Log;

import com.danielpecos.gtdtm.activities.tasks.GoogleTasksClientAsyncTask;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.model.beans.Project;
import com.danielpecos.gtdtm.model.beans.Task;
import com.danielpecos.gtdtm.model.beans.TaskContainer;
import com.danielpecos.gtdtm.utils.DateUtils;
import com.danielpecos.gtdtm.utils.google.GoogleTasksClient;
import com.google.api.services.tasks.v1.model.Tasks;

public class GoogleTasksHelper {
	public static final String GTASKS_PREFIX_DISCARDED = "[D]";

	public static String gTasksSynchronization(Activity activity, GoogleTasksClient client, Context context) throws IOException {
		Log.d(TaskManager.TAG, "GTasks: Synchronizing context element");
		boolean isNewContextList = getOrCcreateGoogleList(activity, context, client);

		Log.d(TaskManager.TAG, "GTasks: Getting remote list of tasks");
		Tasks gTasks = client.getTasksFromList(context.getGoogleId());

		if (TaskManager.isFullVersion(activity)) {
			Log.i(TaskManager.TAG, "GTasks: Full version - Synchronizing from remote to local");
			syncrhonizeFromGoogleToLocal(activity, context, client, gTasks);
			Log.i(TaskManager.TAG, "GTasks: Full version - Finished synchronization from remote to local");
			isNewContextList = false;
		} else {
		}

		Log.i(TaskManager.TAG, "GTasks: Synchronizing from local to remote");
		synchronizeFromLocalToGoole(activity, context, client, gTasks, isNewContextList);
		Log.i(TaskManager.TAG, "GTasks: Finished synchronization from local to remote");

		return GoogleTasksClientAsyncTask.RESPONSE_OK;
	}

	public static String gTasksDeleteTask(Activity activity, GoogleTasksClient client, Task task) throws IOException {
		Log.d(TaskManager.TAG, "GTasks: Deleting task \"" +task.getName() + " (" + task.getGoogleId() + ")\"");
		Context context = TaskManager.getInstance(activity).findContextContainingTask(task);
		client.deleteTask(context.getGoogleId(), task.getGoogleId());
		return GoogleTasksClientAsyncTask.RESPONSE_OK;
	}

	private static void syncrhonizeFromGoogleToLocal(Activity activity, Context context, GoogleTasksClient client, Tasks tasks) throws IOException {
		if (tasks != null && tasks.items != null && tasks.items.size() > 0) {
			// search for projects
			Set<String> gTasksProjects = new TreeSet<String>();
			HashMap<String, String> gTasksParents = new HashMap<String, String>();

			for (com.google.api.services.tasks.v1.model.Task gTask : tasks.items) {
				if (gTask.parent != null) {
					// we're sure it's not a project, but its (top)parent is :)

					// maybe we considered it a top level project, we were wrong
					boolean updateParents = gTasksProjects.contains(gTask.id);
					if (updateParents) {
						gTasksProjects.remove(gTask.id);
					}

					String topParentId = gTask.parent;
					do {
						if (gTasksProjects.contains(topParentId)) {
							gTasksParents.put(gTask.id, topParentId);
						} else {
							if (gTasksParents.containsKey(topParentId) && gTasksParents.get(topParentId) != null) {
								topParentId = gTasksParents.get(topParentId);
							} else {
								// parent not yet processed
								gTasksParents.put(gTask.id, topParentId);
							}
						}

					} while (!gTasksParents.containsKey(gTask.id));

					if (!gTasksProjects.contains(topParentId)) {
						gTasksProjects.add(topParentId);
					}

					if (updateParents) {
						// let's update previous processed childs
						for (String taskId : gTasksParents.keySet()) {
							if (gTasksParents.get(taskId) != null && gTasksParents.get(taskId).equalsIgnoreCase(gTask.id)) {
								gTasksParents.put(taskId, topParentId);
							}
						}
					}

				} else {
					gTasksParents.put(gTask.id, null);
				}
			}

			for (com.google.api.services.tasks.v1.model.Task gTask : tasks.items) {
				if (gTasksProjects.contains(gTask.id) || context.getProjectByGoogleId(gTask.id) != null) {
					// it's a project
					Project p = createOrUpdateLocalProject(activity, context, gTask);
				} else {
					// it's a task
					Task t = null;
					if (gTask.parent == null) {
						t = createOrUpdateLocalTask(activity, context, gTask);
					} else {
						Project project = context.getProjectByGoogleId(gTasksParents.get(gTask.id));
						if (project != null) {
							t = createOrUpdateLocalTask(activity, project, gTask);
						} else {
							Log.e(TaskManager.TAG, "GTasks: task (" + gTask.title + ")'s project parent doesn't exist yet");
						}
					}
					if (t != null) {
					}
				}
			}
		}
	}

	private static void synchronizeFromLocalToGoole(Activity activity, Context context, GoogleTasksClient client, Tasks gTasks, boolean isNewContextList) throws IOException {

		String contextListId = context.getGoogleId();

		String previousProjectId = null;

		for (Project project : context.getProjects()) {
			boolean projectUpdatedInGTasks = false;
			boolean isNewProject = isNewContextList;
			String projectId = null;
			Task.Status status = project.getCompletedTasksCount() == (project.getTasksCount() - project.getDiscardedTasksCount()) ? Task.Status.Completed : Task.Status.Active;
			if (project.getGoogleId() != null && !isNewContextList) {
				com.google.api.services.tasks.v1.model.Task gTask = findTask(gTasks, project.getGoogleId());
				isNewProject = false;

				if (project.getLastTimePersisted() == null || gTask.updated == null || DateUtils.parseDate(gTask.updated).getTime() < project.getLastTimePersisted().getTime() || 
						!TaskManager.isFullVersion(activity) && DateUtils.parseDate(gTask.updated).getTime() > project.getLastTimePersisted().getTime() ||
						status == Task.Status.Completed && (gTask.title.startsWith(GoogleTasksHelper.GTASKS_PREFIX_DISCARDED) || !gTask.containsKey("status") || !gTask.status.equalsIgnoreCase("completed")) ||
						status == Task.Status.Active && (gTask.title.startsWith(GoogleTasksHelper.GTASKS_PREFIX_DISCARDED) || gTask.containsKey("status") && gTask.status.equalsIgnoreCase("completed"))	) {
					projectId = project.getGoogleId();
					com.google.api.services.tasks.v1.model.Task tResult = client.updateTask(contextListId, previousProjectId, project.getGoogleId(), project.getName(), project.getDescription(), null, status);
					projectUpdatedInGTasks = tResult != null;
					if (!projectUpdatedInGTasks) {
						Log.e(TaskManager.TAG, "GTasks: Error updating remote project");
					} else {
						project.store(activity, DateUtils.parseDate(tResult.updated));
						Log.d(TaskManager.TAG, "GTasks: Updated remote project " + project.getName());
					}
				} else {
					Log.d(TaskManager.TAG, "GTasks: No need to update remote project " + project.getName());
					projectUpdatedInGTasks = true;
				}
			}
			if (!projectUpdatedInGTasks) {
				isNewProject = true;
				com.google.api.services.tasks.v1.model.Task pResult = client.createTask(contextListId, null, previousProjectId, project.getName(), project.getDescription(), null, status);
				projectId = pResult.id;

				if (projectId != null) {
					project.setGoogleId(projectId);
					project.store(activity.getBaseContext(), DateUtils.parseDate(pResult.updated));
				} else {
					Log.e(TaskManager.TAG, "GTasks: Error creating project/task");
				}
			}
			previousProjectId = project.getGoogleId();

			exportTasks(activity, client, gTasks, contextListId, project, null, isNewProject);
		}

		exportTasks(activity, client, gTasks, contextListId, context, previousProjectId, isNewContextList);
	}

	private static boolean getOrCcreateGoogleList(Activity activity, Context context, GoogleTasksClient client) throws IOException {

		boolean isNewListOrNotSynchronized = false;

		String gTaskListId = null;
		if (context.getGoogleId() != null) {

			gTaskListId = client.getTaskList(context.getGoogleId());
			if (gTaskListId == null) {
				isNewListOrNotSynchronized = true;
				gTaskListId = client.getTaskListByName(context.getName());
				if (gTaskListId == null) {
					resetSynchronizationData(context);
					gTaskListId  = client.createTaskList(context.getName());
				} else {
					if (!client.updateTaskList(context.getGoogleId(), context.getName())) {
						Log.w(TaskManager.TAG, "GTasks: Error updating remote context/taskList");
					}
				}
			} else {
				if (!client.updateTaskList(context.getGoogleId(), context.getName())) {
					Log.e(TaskManager.TAG, "GTasks: Error updating remote context/taskList");
				}
			}

		} else {
			isNewListOrNotSynchronized = true;
			gTaskListId = client.getTaskListByName(context.getName());
			if (gTaskListId == null) {
				gTaskListId = client.createTaskList(context.getName());
			}
		}

		if (isNewListOrNotSynchronized) {
			context.setGoogleId(gTaskListId);
			context.store(activity);
		}

		return isNewListOrNotSynchronized;

	}

	public static void resetSynchronizationData(Context context) {
		for (Project p : context.getProjects()) {
			resetSynchronizationData(p);
		}
		for (Task t : context) {
			t.setGoogleId(null);
		}
	}

	private static void resetSynchronizationData(Project p) {
		p.setGoogleId(null);
		for (Task t : p) {
			t.setGoogleId(null);
		}
	}


	private static Project createOrUpdateLocalProject(Activity activity, Context context, com.google.api.services.tasks.v1.model.Task gTask) {
		Project project = context.getProjectByGoogleId(gTask.id);
		if (project != null) {
			if (project.getLastTimePersisted() == null || DateUtils.parseDate(gTask.updated).getTime() > project.getLastTimePersisted().getTime()) {
				// update local existing project
				// TODO: what to do when deleting remote project
				project.setName(gTask.title);
				project.setDescription(gTask.notes);
				project.store(activity, DateUtils.parseDate(gTask.updated));
				Log.d(TaskManager.TAG, "GTaks: Updated local project " + project.getName());
			} else {
				Log.d(TaskManager.TAG, "GTaks: No need to update local project " + project.getName());
			}
		} else {
			// create new project
			if (!gTask.containsKey("deleted") || !gTask.deleted) {
				project = context.createProject(activity, gTask.title, gTask.notes);
				project.setGoogleId(gTask.id);
				project.store(activity, DateUtils.parseDate(gTask.updated));
				Log.d(TaskManager.TAG, "GTaks: Created local project " + project.getName());
			}
		}

		return project;
	}

	private static Task createOrUpdateLocalTask(Activity activity, TaskContainer container, com.google.api.services.tasks.v1.model.Task gTask) {
		Task task = container.getTaskByGoogleId(gTask.id);
		if (task != null) {
			if (task.getLastTimePersisted() == null || DateUtils.parseDate(gTask.updated).getTime() > task.getLastTimePersisted().getTime()) {
				updateLocalTask(activity, container, task, gTask);	
				Log.d(TaskManager.TAG, "GTaks: Updated local task " + task.getName());
			} else {
				Log.d(TaskManager.TAG, "GTaks: No need to update local task " + task.getName());
				return null;
			}
		} else {
			task = createLocalTask(activity, container, gTask);
			if (task != null) {
				Log.d(TaskManager.TAG, "GTaks: Created local task " + task.getName());
			}
		}
		return task;
	}

	private static Task createLocalTask(Activity activity, TaskContainer container, com.google.api.services.tasks.v1.model.Task gTask) {
		if (!gTask.containsKey("deleted") || !gTask.deleted) {
			Task t = container.createTask(activity, gTask.title, gTask.notes, Task.Priority.Normal);
			t.setGoogleId(gTask.id);

			updateLocalTask(activity, container, t, gTask);

			return t;
		} else {
			return null;
		}
	}

	private static void updateLocalTask(Activity activity, TaskContainer container, Task t, com.google.api.services.tasks.v1.model.Task gTask) {
		if (gTask.containsKey("deleted") && gTask.deleted) {
			container.deleteTask(activity, t);
			Log.i(TaskManager.TAG, "GTaks: Deleted local task " + t.getName());
		} else {
			if (gTask.title.toUpperCase().startsWith(GoogleTasksHelper.GTASKS_PREFIX_DISCARDED)) {
				t.setName(gTask.title.substring(GoogleTasksHelper.GTASKS_PREFIX_DISCARDED.length()).trim());
			} else {
				t.setName(gTask.title);
			}
			t.setDescription(gTask.notes);

			t.setDueDate(DateUtils.parseDate(gTask.due));

			if (gTask.title.toUpperCase().startsWith(GTASKS_PREFIX_DISCARDED)) {
				if (gTask.status.equalsIgnoreCase("completed")) {
					t.setStatus(Task.Status.Discarded_Completed);
				} else if (gTask.status.equalsIgnoreCase("needsAction")) {
					t.setStatus(Task.Status.Discarded);
				}
			} else {
				if (gTask.status.equalsIgnoreCase("completed")) {
					t.setStatus(Task.Status.Completed);
				} else if (gTask.status.equalsIgnoreCase("needsAction")) {
					t.setStatus(Task.Status.Active);
				}
			}

			t.store(activity, DateUtils.parseDate(gTask.updated));
		}
	}

	private static void exportTasks(Activity activity, GoogleTasksClient client, Tasks gTasks, String contextListId, TaskContainer parent, String previousId, boolean forceCreate) throws IOException {
		String parentId = null;
		if (parent instanceof Project) {
			parentId = ((Project) parent).getGoogleId();
		}
		String previousTaskId = previousId;

		long previousTaskPostion = 0;
		com.google.api.services.tasks.v1.model.Task previousTask = findTask(gTasks,previousTaskId);
		if (parentId == null && previousTask != null) {
			previousTaskPostion = Long.parseLong(previousTask.position);
		} else {
			com.google.api.services.tasks.v1.model.Task firsTask = findFirstTaskInParent(gTasks, parentId);
			if (firsTask != null) {
				previousTaskPostion = Long.parseLong(firsTask.position);
			}
		}

		for (Task task : parent) {
			boolean elementUpdated = false;
			if (task.getGoogleId() != null) {
				com.google.api.services.tasks.v1.model.Task gTask = findTask(gTasks, task.getGoogleId());
				if (gTask != null) {
					if (task.getLastTimePersisted() == null || gTask.updated == null || DateUtils.parseDate(gTask.updated).getTime() < task.getLastTimePersisted().getTime() || 
							!TaskManager.isFullVersion(activity) && DateUtils.parseDate(gTask.updated).getTime() > task.getLastTimePersisted().getTime() ||
							previousTaskPostion == 0  || Long.parseLong(gTask.position) < previousTaskPostion || 
							gTask.title.startsWith(GTASKS_PREFIX_DISCARDED.toLowerCase())) {
						com.google.api.services.tasks.v1.model.Task tResult = client.updateTask(contextListId, previousTaskId, task.getGoogleId(), task.getName(), task.getDescription(), task.getDueDate(), task.getStatus());
						elementUpdated = tResult != null;
						if (!elementUpdated) {
							Log.e(TaskManager.TAG, "GTasks: Error updating remote task");
						} else {
							task.store(activity, DateUtils.parseDate(tResult.updated));
							previousTaskPostion = Long.parseLong(tResult.position);
							Log.d(TaskManager.TAG, "GTasks: Updated remote task " + task.getName());
						}
					} else {
						Log.d(TaskManager.TAG, "GTasks: No need to update remote task " + task.getName());
						previousTaskPostion = Long.parseLong(gTask.position);
						elementUpdated = true;
					}
				}
			}
			if (!elementUpdated) {
				com.google.api.services.tasks.v1.model.Task tResult = client.createTask(contextListId, parentId, previousTaskId, task.getName(), task.getDescription(), task.getDueDate(), task.getStatus());
				if (tResult != null) {
					task.setGoogleId(tResult.id);
					task.store(activity.getBaseContext(), DateUtils.parseDate(tResult.updated));
					previousTaskPostion = Long.parseLong(tResult.position);
					Log.d(TaskManager.TAG, "GTasks: Created remote task " + task.getName());
				} else {
					Log.e(TaskManager.TAG, "GTasks: Error creating remote task");
				}
			}

			previousTaskId = task.getGoogleId();
		}

	}

	private static com.google.api.services.tasks.v1.model.Task findFirstTaskInParent(Tasks gTasks, String parentId) {
		if (gTasks != null && gTasks.items != null) {
			for(com.google.api.services.tasks.v1.model.Task task : gTasks.items) {
				if ((task.parent == null && parentId == null) || (task.parent != null && task.parent.equalsIgnoreCase(parentId))) {
					return task;
				}
			}
		}
		return null;
	}

	private static com.google.api.services.tasks.v1.model.Task findTask(Tasks gTasks, String googleId) {
		if (gTasks != null && gTasks.items != null) {
			for(com.google.api.services.tasks.v1.model.Task task : gTasks.items) {
				if (task.id.equalsIgnoreCase(googleId)) {
					return task;
				}
			}
		}
		return null;
	}

}
