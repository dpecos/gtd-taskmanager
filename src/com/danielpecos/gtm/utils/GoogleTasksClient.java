package com.danielpecos.gtm.utils;

import java.io.IOException;

import com.danielpecos.gtm.model.beans.Task;
import com.google.api.services.tasks.v1.Tasks;
import com.google.api.services.tasks.v1.model.TaskList;
import com.google.api.services.tasks.v1.model.TaskLists;



/* 
 * http://code.google.com/apis/tasks/v1/using.html
 */

public class GoogleTasksClient {

	private Tasks service;

	public GoogleTasksClient(Tasks service) {
		this.service = service;
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
		
		TaskList list = new TaskList();
		list.title = name;
		TaskList result = this.service.tasklists.insert(list).execute();

		return result.id;
	}
}