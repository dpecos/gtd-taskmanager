package com.danielpecos.gtdtm.activities;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.model.persistence.GoogleTasksHelper;
import com.danielpecos.gtdtm.utils.google.GoogleClient;
import com.danielpecos.gtdtm.utils.google.GoogleClient.AuthCallback;
import com.danielpecos.gtdtm.utils.google.GoogleTasksClient;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;

public class GoogleAccountActivity extends Activity {
	public static final String GTASKS_ACTION_SYNCHRONIZE = "gTasks_synchronization";
	public static final String GTASKS_ACTION_DELETE_TASK = "gTasks_deleteTask";

	private String action;
	private long[] ids;

	private TaskManager taskManager;
	private GoogleClient gClient;
	private GoogleTasksClient gTasksClient;
	private int retries = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.action = (String)getIntent().getSerializableExtra("action");
		this.ids = getIntent().getLongArrayExtra("ids");

		setContentView(R.layout.activity_google_account);

		if (TaskManager.isFullVersion(this)) {
			findViewById(R.id.gtasks_freeVersion_message_1).setVisibility(View.GONE);
			findViewById(R.id.gtasks_freeVersion_message_2).setVisibility(View.GONE);
		}

		gClient = new GoogleClient();
		taskManager = TaskManager.getInstance(this);
		
		doAuth();
	}


	private void doAuth() {
		gClient.login(this, new AuthCallback() {
			@Override
			public void onAuthResult(String authToken) {
				gTasksClient = new GoogleTasksClient(GoogleAccountActivity.this, authToken);
				processAction();
			}
		});
	}
	
	private String processAction() {
		Log.i(TaskManager.TAG, "GTasks: Synchronizing with Google Tasks...");

		try {
			if (action.equalsIgnoreCase(GTASKS_ACTION_SYNCHRONIZE)) {
				StringBuilder message = new StringBuilder(); 
				Context[] contexts = this.getContexts();
				for (Context context: contexts) {
					message.append(GoogleTasksHelper.gTasksSynchronization(this, gTasksClient, context));
				}
				return message.toString();
//			} else if (action.equalsIgnoreCase(GTASKS_ACTION_DELETE_TASK)) {
//				Task tasks = this.getTasks();
//				return GoogleTasksHelper.gTasksDeleteTask(this, gTasksClient, task);
			} else {
				return null;
			}
		} catch (Exception e) {
			String message = this.handleException(e);
			if (message == null && this.retries  > 0) {
				// it was a 401
				Log.i(TaskManager.TAG, "GTasks: retrying action...");
				gClient.invalidateAuthToken();
				this.retries--;
				this.doAuth();
			}
			return message;
		}
	}

//	private Task getTasks() {
//		return null;
//	}


	private Context[] getContexts() {
		Context[] contexts = new Context[this.ids.length];
		int i = 0;
		for (long id: this.ids) {
			contexts[i++] = taskManager.getContext(id);
		}
		return contexts;
	}


	private String handleException(Exception e) {
		Log.e(TaskManager.TAG, e.getMessage(), e);
		String message = null;
		if (e instanceof HttpResponseException) {
			HttpResponse response = ((HttpResponseException) e).response;
			int statusCode = response.statusCode;
			try {
				response.ignore();
			} catch (IOException ioe) {
				Log.e(TaskManager.TAG, "GoogleClient: error", ioe);
			}
			if (statusCode == 400) {
				Log.e(TaskManager.TAG, "GTasks: error in request " + e.getMessage(), e);
				message = this.getString(R.string.gtasks_errorInRequest);
			} else if (statusCode == 401) {
				Log.w(TaskManager.TAG, "GoogleClient: authToken invalid! " + statusCode);
			} else {
				Log.e(TaskManager.TAG, "GTasks: error in communication", e);
				message = this.getString(R.string.gtasks_errorInCommunication);
			}
		} else {
			Log.e(TaskManager.TAG, "GTasks: unknown error: " + e.getMessage(), e);
			message = this.getString(R.string.error_unknown) + ": " + e.getMessage();
		}
		return message;
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		switch (requestCode) {
//		case REQUEST_AUTHENTICATE:
//			if (resultCode == RESULT_OK) {
//				gotAccount(false);
//			} else {
//				showDialog(DIALOG_ACCOUNTS);
//			}
//			break;
//		}
//	}

}
