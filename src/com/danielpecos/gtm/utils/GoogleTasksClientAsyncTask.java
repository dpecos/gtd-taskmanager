package com.danielpecos.gtm.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;

public class GoogleTasksClientAsyncTask extends AsyncTask<Object, Integer, Boolean>{

	private Activity activity;
	private Context context;

	ProgressDialog progressDialog;

	public GoogleTasksClientAsyncTask(Activity activity, Context context) {
		this.activity = activity;
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		final TaskManager taskManager = TaskManager.getInstance(activity);
		return taskManager.synchronizeGTasks(activity, context);

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.gtasks_synchronizing), true);
	}

	@Override
	protected void onPostExecute(Boolean response) {
		super.onPostExecute(response);
		progressDialog.dismiss();
		Toast.makeText(activity, activity.getString(R.string.gtasks_synchronizationFinished), Toast.LENGTH_SHORT).show();
	}
}
