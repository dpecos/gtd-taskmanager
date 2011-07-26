package com.danielpecos.gtdtm.activities.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.activities.ContextActivity;
import com.danielpecos.gtdtm.model.beans.Context;
import com.danielpecos.gtdtm.model.persistence.GoogleTaskHelper;
import com.danielpecos.gtdtm.views.ContextViewHolder;

public class GoogleTasksClientAsyncTask extends AsyncTask<Object, Integer, String>{

	private ContextActivity activity;
	private Context context;

	ProgressDialog progressDialog;

	public GoogleTasksClientAsyncTask(ContextActivity activity, Context context, ContextViewHolder contextViewHolder) {
		this.activity = activity;
		this.context = context;
	}

	@Override
	protected String doInBackground(Object... params) {
		return GoogleTaskHelper.doInGTasks(activity, GoogleTaskHelper.GTASKS_ACTION_SYNCHRONIZE, context, null, null);

	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.gtasks_synchronizing), true);
	}

	@Override
	protected void onPostExecute(String response) {
		super.onPostExecute(response);
		progressDialog.dismiss();
		if (response != null) {
			Toast.makeText(activity, response, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(activity, activity.getString(R.string.gtasks_synchronizationFinished), Toast.LENGTH_SHORT).show();
		} 
		activity.initializeUI();
	}
}
