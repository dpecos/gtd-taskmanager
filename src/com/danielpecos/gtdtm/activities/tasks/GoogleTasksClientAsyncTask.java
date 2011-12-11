package com.danielpecos.gtdtm.activities.tasks;

import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.model.persistence.GoogleTasksHelper;
import com.danielpecos.gtdtm.utils.DoubleProgressDialog;
import com.danielpecos.gtdtm.utils.google.GoogleClient;
import com.danielpecos.gtdtm.utils.google.GoogleTasksClient;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;

public class GoogleTasksClientAsyncTask extends AsyncTask<Long, Integer, Void>{

	public static String GTASKS_ACTION_SYNCHRONIZE = "gTasks_synchronize";
	public static final int PROGRESS_DIALOG_ID = 1;
	
	private static Integer PROGRESS_STARTED = 0;
	private static Integer PROGRESS_PRE_LOGIN = 10;
	private static Integer PROGRESS_POST_LOGIN = 20;
	private static Integer PROGRESS_PRE_ACTION = 30;
	private static Integer PROGRESS_POST_ACTION = 90;
	private static Integer PROGRESS_FINISHED = 100;

	private Activity activity;
	private String action;

	private TaskManager taskManager;
	private GoogleClient gClient;
	private GoogleTasksClient gTasksClient;
	private int retries = 1;

	private DoubleProgressDialog progressDialog;

	public GoogleTasksClientAsyncTask(Activity activity, String action, DoubleProgressDialog progressDialog) {
		this.activity = activity;
		this.action = action;
		this.progressDialog = progressDialog;

		this.taskManager = TaskManager.getInstance(this.activity);
		this.gClient = new GoogleClient();
	}

	@Override
	protected void onPreExecute() {
		if (this.gClient.getSelectedAccount() == null) {
			this.gClient.selectGoogleAccount(this.activity);
		}
	}
	
	@Override
	protected void onPostExecute(Void result) {
		this.activity.dismissDialog(PROGRESS_DIALOG_ID);
		
		//TODO: update interface
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		if (values[0] != null) {
			this.progressDialog.setProgress(values[0]);
		}
		if (values.length > 1 && values[1] != null) {
			this.progressDialog.setSecondaryProgress(values[1]);
		}
	}

	@Override
	public Void doInBackground(Long...ids) {
		Log.d(TaskManager.TAG, "GTasks: Started async task");
		this.publishProgress(PROGRESS_STARTED);

		doAuth(ids);

		return null;
	}
	
	

	private void doAuth(final Long[] ids) {
		GoogleTasksClientAsyncTask.this.publishProgress(PROGRESS_PRE_LOGIN);
		String authToken = gClient.login(this.activity);
		gTasksClient = new GoogleTasksClient(GoogleTasksClientAsyncTask.this.activity, authToken);
		GoogleTasksClientAsyncTask.this.publishProgress(PROGRESS_POST_LOGIN);
		processAction(ids);
	}

	private void processAction(final Long[] ids) {
		Log.i(TaskManager.TAG, "GTasks: Synchronizing with Google Tasks...");

		try {
			//if (action.equalsIgnoreCase(GTASKS_ACTION_SYNCHRONIZE)) {
			StringBuilder message = new StringBuilder(); 
			Context[] contexts = this.getContexts(ids);
			for (Context context: contexts) {
				message.append(GoogleTasksHelper.gTasksSynchronization(this.activity, gTasksClient, context, new ProgressHandler(GoogleTasksHelper.TOTAL_STEPS, PROGRESS_PRE_ACTION, PROGRESS_POST_ACTION) {
					@Override
					public void onFinish(String response) {
					}
					@Override
					public void updateProgress(Integer progress, Integer secondaryProgress) {
						if (progress != null) {
							this.lastStep = progress;
							GoogleTasksClientAsyncTask.this.publishProgress(this.rangeStart + (this.rangeSize / total) * progress, null);
						} else {
							GoogleTasksClientAsyncTask.this.publishProgress(null, secondaryProgress);
						}
					}
				}));
			}
			//			return message.toString();
			//			} else if (action.equalsIgnoreCase(GTASKS_ACTION_DELETE_TASK)) {
			//				Task tasks = this.getTasks();
			//				return GoogleTasksHelper.gTasksDeleteTask(this, gTasksClient, task);
			//			} else {
			//				return null;
			//			}

			this.publishProgress(PROGRESS_FINISHED);

			Log.d(TaskManager.TAG, "GTasks: Finished async task");

			//			this.activity.finish();

		} catch (Exception e) {
			String message = this.handleException(e);
			if (message == null && this.retries  > 0) {
				// it was a 401
				Log.i(TaskManager.TAG, "GTasks: retrying action...");
				gClient.invalidateAuthToken();
				this.retries--;
				this.doAuth(ids);
			}
		}
	}

	private Context[] getContexts(final Long[] ids) {
		Context[] contexts = new Context[ids.length];
		int i = 0;
		for (long id: ids) {
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
				message = this.activity.getString(R.string.gtasks_errorInRequest);
			} else if (statusCode == 401) {
				Log.w(TaskManager.TAG, "GoogleClient: authToken invalid! " + statusCode);
			} else {
				Log.e(TaskManager.TAG, "GTasks: error in communication", e);
				message = this.activity.getString(R.string.gtasks_errorInCommunication);
			}
		} else {
			Log.e(TaskManager.TAG, "GTasks: unknown error: " + e.getMessage(), e);
			message = this.activity.getString(R.string.error_unknown) + ": " + e.getMessage();
		}
		return message;
	}

	/*@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.gtasks_synchronizing), true);
	}

	@Override
	protected void onPostExecute(String response) {
		super.onPostExecute(response);
		try { 
			progressDialog.dismiss();
		} catch (Exception e) {
			Log.w(TaskManager.TAG, "Exception raised dismissing progress dialog");
		}
		if (response != null) {
			if (response.equalsIgnoreCase(RESPONSE_OK)) {
				Toast.makeText(activity, activity.getString(R.string.gtasks_synchronizationFinished), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
			}
		} 
//		activity.initializeUI();
	}*/
}
