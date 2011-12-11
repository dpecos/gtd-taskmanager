package com.danielpecos.gtdtm.utils.google;

import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Task.Status;
import com.danielpecos.gtdtm.model.persistence.GoogleTasksHelper;
import com.danielpecos.gtdtm.utils.DateUtils;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;
import com.google.api.services.tasks.v1.Tasks.TasksOperations.List;
import com.google.api.services.tasks.v1.Tasks.TasksOperations.Move;
import com.google.api.services.tasks.v1.model.Task;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;

public class GoogleTasksClient {
	private static String apiKey = "AIzaSyB9Uw7kh3jdyoO9FkzTjAtAkf48on1HI8U";

	private Tasks service;

	public GoogleTasksClient(final Activity activity, String authToken) {
		Log.d(TaskManager.TAG, "New GoogleTasksClient with authToken " + authToken);

		new GoogleAccessProtectedResource(authToken) {
			@Override
			protected void onAccessToken(String accessToken) {
				Log.w(TaskManager.TAG, "GTasks: onAccessToken");
//				ActivityUtils.showGoogleAccountActivity(activity, context, Boolean.TRUE);
			}
		};

		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		this.service = new Tasks("Google-TaskSample/1.0", transport, jsonFactory);

		this.service.setAccessToken(authToken);
		this.service.accessKey = apiKey;
	}

	public String getTaskListByName(String title) throws IOException {
		try {
			TaskLists taskLists = this.service.tasklists.list().execute();
			for (TaskList taskList : taskLists.items) {
				if (taskList.title.equalsIgnoreCase(title)) {
					return taskList.id;
				}
			}
		} catch (IOException e) {
			if (e instanceof HttpResponseException) {
				HttpResponse response = ((HttpResponseException) e).response;
				int statusCode = response.statusCode;
				if (statusCode == 404) {
					Log.e(TaskManager.TAG, "Gtasks: could not get user's lists", e);
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
		return null;
	}

	public String getTaskList(String googleId) throws IOException {

		TaskList taskList = null;
		try {
			taskList = this.service.tasklists.get(googleId).execute();
			return taskList.id;
		} catch (IOException e) {
			if (e instanceof HttpResponseException) {
				HttpResponse response = ((HttpResponseException) e).response;
				int statusCode = response.statusCode;
				if (statusCode == 404) {
					Log.e(TaskManager.TAG, "Gtasks: list not found", e);
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
		return null;
	}

	public com.google.api.services.tasks.v1.model.Tasks getTasksFromList(String googleId) throws IOException {
		List operation = service.tasks.list(googleId);
		operation.showDeleted = true;
		com.google.api.services.tasks.v1.model.Tasks tasks = operation.execute();
		return tasks;
	}

	public String createTaskList(String name) throws IOException {
		//		Log.d(TaskManager.TAG, "GTasks: creating context/list " + name);
		TaskList list = new TaskList();
		list.title = name;

		TaskList result = this.service.tasklists.insert(list).execute();
		return result.id;
	}

	public boolean updateTaskList(String id, String name) throws IOException {
		//		Log.d(TaskManager.TAG, "GTasks: updating context/list " + name + " with ID " + id);

		try {
			//TaskList list = service.tasklists.get(id).execute();
			//Log.d(TaskManager.TAG, "GTasks: got context/list " + list.title + " with ID " + list.id);

			TaskList list = new TaskList();
			list.id = id;
			list.title = name;

			TaskList result = service.tasklists.update(id, list).execute();

			return id.equalsIgnoreCase(result.id);

		} catch (IOException e) {
			if (e instanceof HttpResponseException) {
				HttpResponse response = ((HttpResponseException) e).response;
				int statusCode = response.statusCode;

				if (statusCode == 404) {
					return false;
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
	}

	public Task createTask(String taskListId, String parentTaskId, String previousTaskId, String name, String description, Date duedate, Status status) throws IOException {
		//		Log.d(TaskManager.TAG, "GTasks: creating project/task " + name);

		Task task = new Task();

		fillTask(task, name, description, status, duedate);

		Task result = service.tasks.insert(taskListId, task).execute();
		Log.d(TaskManager.TAG, "GTasks: project/task created with ID: " + result.id);

		if (parentTaskId != null || previousTaskId != null) {	
			Move move = service.tasks.move(taskListId, result.id);
			if (parentTaskId != null) {
				Log.d(TaskManager.TAG, "GTasks: setting parent ID: " + parentTaskId);
				move.parent = parentTaskId;
			}
			Log.d(TaskManager.TAG, "GTasks: setting previous ID: " + previousTaskId);
			move.previous = previousTaskId;
			result = move.execute();
		}

		return result;
	}

	public Task updateTask(String taskListId, String previousTaskId, String id, String name, String description, Date duedate, Status status) throws IOException {
		//		Log.d(TaskManager.TAG, "GTasks: updating project/task " + name + " with ID " + id);

		try {
			Task tmp = new Task();
			tmp.id = id;

			fillTask(tmp, name, description, status, duedate);

			Task result = service.tasks.update(taskListId, tmp.id, tmp).execute();

			Move move = null;
			if (previousTaskId != null) {	
				Log.d(TaskManager.TAG, "GTasks: setting previous ID: " + previousTaskId);
				move = service.tasks.move(taskListId, result.id);
				move.previous = previousTaskId;
			} else if (previousTaskId == null && result.position != null) {
				Log.d(TaskManager.TAG, "GTasks: updating previous ID: " + previousTaskId);
				move = service.tasks.move(taskListId, result.id);
				move.previous = null;
			}
			if (move != null) {
				if (result.parent != null) {
					move.parent = result.parent;
				}
				result = move.execute();
			}

			return result;

		} catch (IOException e) {
			if (e instanceof HttpResponseException) {
				HttpResponse response = ((HttpResponseException) e).response;
				int statusCode = response.statusCode;

				if (statusCode == 404) {
					Log.w(TaskManager.TAG, "GTasks: project/task with ID: " + id + " not found");
					return null;
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
	}

	private void fillTask(Task task, String name, String description, Status status, Date duedate) {
		task.title = name;
		if (description != null) {
			task.notes = description;
		}
		if (duedate != null) {
			task.due = DateUtils.formatDate(duedate);
		}
		if (status != null) {
			if (status == Status.Completed || status == Status.Discarded_Completed) {
				task.status = "completed";
			} else {
				task.status = "needsAction";
			}
			if (status == Status.Discarded || status == Status.Discarded_Completed) {
				if (!name.toUpperCase().startsWith(GoogleTasksHelper.GTASKS_PREFIX_DISCARDED)) {
					task.title = GoogleTasksHelper.GTASKS_PREFIX_DISCARDED + " " + name;
				} else {
					task.title = GoogleTasksHelper.GTASKS_PREFIX_DISCARDED + " " + name.substring(GoogleTasksHelper.GTASKS_PREFIX_DISCARDED.length()).trim();
				}
			}
		}
	}

	public boolean deleteTask(String taskListId, String googleId) throws IOException {
		try {
			service.tasks.delete(taskListId, googleId).execute();
			service.tasks.clear(googleId).execute();
			return true;
		} catch (IOException e) {
			if (e instanceof HttpResponseException) {
				HttpResponse response = ((HttpResponseException) e).response;
				int statusCode = response.statusCode;

				if (statusCode == 404) {
					Log.w(TaskManager.TAG, "GTasks: project/task with ID: " + googleId + " not found");
					return false;
				} else {
					throw e;
				}
			} else {
				throw e;
			}
		}
	}
}