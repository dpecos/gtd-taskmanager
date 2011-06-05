package com.danielpecos.gtdtm.activities.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.views.ContextViewHolder;

public class GoogleTasksClientAsyncTask extends AsyncTask<Object, Integer, Boolean>{

	private Activity activity;
	private Context context;
	private ContextViewHolder contextViewHolder;

	ProgressDialog progressDialog;

	public GoogleTasksClientAsyncTask(Activity activity, Context context, ContextViewHolder contextViewHolder) {
		this.activity = activity;
		this.context = context;
		this.contextViewHolder = contextViewHolder;
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
		if (response) {
			Toast.makeText(activity, activity.getString(R.string.gtasks_synchronizationFinished), Toast.LENGTH_SHORT).show();
		}
		this.contextViewHolder.updateView(activity);
	}
}