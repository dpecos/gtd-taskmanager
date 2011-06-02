package com.danielpecos.gtdtm.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.model.beans.Task.Status;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;
import com.google.api.services.tasks.v1.Tasks.TasksOperations.Move;
import com.google.api.services.tasks.v1.model.Task;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;



/* 
 * http://code.google.com/apis/tasks/v1/using.html
 */

public class GoogleTasksClient {

	private static SimpleDateFormat rfc3339Format = new SimpleDateFormat("yyyy-MM-dd'T'h:m:ss.SZ");

	private static String apiKey = "AIzaSyB9Uw7kh3jdyoO9FkzTjAtAkf48on1HI8U";

	private Tasks service;

	public GoogleTasksClient(final Activity activity, final Context context, String authToken) {
		Log.d(TaskManager.TAG, "New GoogleTasksClient with authToken " + authToken);

		new GoogleAccessProtectedResource(authToken) {
			@Override
			protected void onAccessToken(String accessToken) {
				Log.w(TaskManager.TAG, "GTasks: onAccessToken");
				ActivityUtils.showGoogleAccountActivity(activity, context, Boolean.TRUE);
			}
		};

		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		this.service = new Tasks("Google-TaskSample/1.0", transport, jsonFactory);

		this.service.setAccessToken(authToken);
		this.service.accessKey = apiKey;
	}

	public void getTaskLists() throws IOException {
		TaskLists taskLists = this.service.tasklists.list().execute();

		for (TaskList taskList : taskLists.items) {
			System.out.println(taskList.title);
		}
	}

	public String createTaskList(String name) throws IOException {
		Log.d(TaskManager.TAG, "GTasks: creating context/list " + name);
		TaskList list = new TaskList();
		list.title = name;
		TaskList result = this.service.tasklists.insert(list).execute();

		return result.id;
	}

	public boolean updateTaskList(String id, String name) throws IOException {
		Log.d(TaskManager.TAG, "GTasks: updating context/list " + name + " with ID " + id);

		try {
			TaskList list = service.tasklists.get(id).execute();
			Log.d(TaskManager.TAG, "GTasks: got context/list " + list.title + " with ID " + list.id);

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

	public String createTask(String taskListId, String parentTaskId, String previousTaskId, String name, String description, Date duedate, Status status) throws IOException {
		Log.d(TaskManager.TAG, "GTasks: creating project/task " + name);

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
			if (previousTaskId != null) {
				Log.d(TaskManager.TAG, "GTasks: setting previous ID: " + previousTaskId);
				move.previous = previousTaskId;
			}
			move.execute();
		}

		return result.id;
	}

	public boolean updateTask(String taskListId, String id, String name, String description, Date duedate, Status status) throws IOException {
		Log.d(TaskManager.TAG, "GTasks: updating project/task " + name + " with ID " + id);

		try {
			Task task = service.tasks.get(taskListId, id).execute();

			fillTask(task, name, description, status, duedate);

			Task result = service.tasks.update(taskListId, task.id, task).execute();

			return id.equalsIgnoreCase(result.id);

		} catch (IOException e) {
			if (e instanceof HttpResponseException) {
				HttpResponse response = ((HttpResponseException) e).response;
				int statusCode = response.statusCode;

				if (statusCode == 404) {
					Log.w(TaskManager.TAG, "GTasks: project/task with ID: " + id + " not found");
					return false;
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
			//Use the RFC 3339 timestamp format. For example: 2005-08-09T10:57:00-08:00Z
			task.due = rfc3339Format.format(duedate);
		}
		if (status != null) {
			if (status == Status.Completed || status == Status.Discarded_Completed) {
				task.status = "completed";
			} else {
				task.status = "needsAction";
			}
			if (status == Status.Discarded || status == Status.Discarded_Completed) {
				task.deleted = true;
			} else {
				task.deleted = false;
			}
		}
	}
}