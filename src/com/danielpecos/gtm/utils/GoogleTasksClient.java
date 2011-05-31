package com.danielpecos.gtm.utils;

import java.io.IOException;

import android.app.Activity;
import android.util.Log;

import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Task;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;



/* 
 * http://code.google.com/apis/tasks/v1/using.html
 */

public class GoogleTasksClient {

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

	public Task createTask() {
		return null;
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
		
		TaskList list = service.tasklists.get(id).execute();
		Log.d(TaskManager.TAG, "GTasks: got context/list " + list.title + " with ID " + list.id);
		
		list.title = name;
		TaskList result = service.tasklists.update(id, list).execute();
		
		return id.equalsIgnoreCase(result.id);
	}
}