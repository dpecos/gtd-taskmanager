package com.danielpecos.gtm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.activities.GoogleAccountActivity;
import com.danielpecos.gtm.activities.ProjectActivity;
import com.danielpecos.gtm.activities.TaskActivity;
import com.danielpecos.gtm.activities.TaskMapActivity;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;

public class ActivityUtils {
	public static final int PROJECT_ACTIVITY = 0;
	public static final int TASK_ACTIVITY = 1;
	public static final int CAMERA_ACTIVITY = 2;
	public static final int MAP_ACTIVITY = 3;
	public static final int GOOGLE_ACCOUNT_ACTIVITY = 4;
	public static final int PREFERENCES_ACTIVITY = 5;

	public static void showProjectActivity(Activity activity, Context context, Project project) {
		Intent intent = new Intent(activity.getBaseContext(), ProjectActivity.class);
		intent.putExtra("context_id", context.getId());
		intent.putExtra("project_id", project.getId());
		activity.startActivityForResult(intent, PROJECT_ACTIVITY);
	}

	public static void showTaskActivity(Activity activity, Context context, Project project, Task task) {
		Intent intent = new Intent(activity.getBaseContext(), TaskActivity.class);
		intent.putExtra("context_id", context.getId());
		if (project != null)
			intent.putExtra("project_id", project.getId());
		intent.putExtra("task_id", task.getId());
		activity.startActivityForResult(intent, TASK_ACTIVITY);

	}

	public static void callDefaultCameraApp(Activity activity) {
		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		activity.startActivityForResult(intent, CAMERA_ACTIVITY);
	}


	public static void showMapActivity(Activity activity, Task task) {
		Intent intent = new Intent(activity.getBaseContext(), TaskMapActivity.class);   
		intent.putExtra("task_id", task.getId());
		activity.startActivityForResult(intent, MAP_ACTIVITY);
	} 

	public static void showGoogleAccountActivity(Activity activity, Context context, Boolean invalidate) {
		Intent intent = new Intent(activity.getBaseContext(), GoogleAccountActivity.class);   
		intent.putExtra("invalidate_token", invalidate);
		intent.putExtra("context_id", context.getId());
		Log.d(TaskManager.TAG, "GTasks: invoking GoogleAccountActivity");
		activity.startActivityForResult(intent, GOOGLE_ACCOUNT_ACTIVITY);
	}	

	public static void showTextBoxDialog(final android.content.Context context, String title, String label, String text, final OnDismissListener listener) {

		final Dialog textboxDialog = new Dialog(context);
		textboxDialog.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
				WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		textboxDialog.setTitle(title);

		//textboxDialog.setOnDismissListener(listener);

		LayoutInflater li = (LayoutInflater) context.getSystemService(android.content.Context.LAYOUT_INFLATER_SERVICE);
		View dialogView = li.inflate(R.layout.dialog_textbox, null);
		textboxDialog.setContentView(dialogView);

		textboxDialog.show();

		Button okButton = (Button) dialogView.findViewById(R.id.textbox_ok_button);
		Button cancelButton = (Button) dialogView.findViewById(R.id.textbox_cancel_button);
		TextView labelView = (TextView) dialogView.findViewById(R.id.textbox_label);
		labelView.setText(label);
		final EditText textBox = (EditText) dialogView.findViewById(R.id.textbox_text);
		textBox.setText(text);

		okButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				if (textBox.getText().length() == 0) {
					Toast.makeText(context, "Enter a value.", Toast.LENGTH_LONG).show();
				} else { 
					textboxDialog.dismiss();
					listener.onDismiss(textboxDialog);
				}
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				textboxDialog.cancel();
			}
		});

	}

	public static AlertDialog.Builder createConfirmDialog(android.content.Context context, int msgId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(msgId)
		.setCancelable(false)
		.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		})
		.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		return builder;
	}
}
