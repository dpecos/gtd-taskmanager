package com.danielpecos.gtdtm.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.activities.TaskActivity;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.model.beans.Task;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		Log.i(TaskManager.TAG, "AlarmReceiver triggered!");
		
		Resources res = ctx.getResources();

		Long context_id = (Long)intent.getSerializableExtra("context_id");
		Long project_id = (Long)intent.getSerializableExtra("project_id");
		Long task_id = (Long)intent.getSerializableExtra("task_id");

		Task task = new Task(ctx, task_id);
		
		NotificationManager nm = (NotificationManager)ctx.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(
				R.drawable.stat_notify_alarm, 
				res.getString(R.string.notification_title), 
				System.currentTimeMillis()
		);
		notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		
		Intent notificationIntent = new Intent(ctx, TaskActivity.class);
		notificationIntent.putExtra("task_id", task_id);
		notificationIntent.putExtra("project_id", project_id);
		notificationIntent.putExtra("context_id", context_id);
		// this line is required in order the activity to received parameters above ?? 
		// (see: http://stackoverflow.com/questions/1198558/how-to-send-parameters-from-a-notification-click-to-an-activity/1257280#1257280)
		notificationIntent.setData((Uri.parse("foobar://"+SystemClock.elapsedRealtime())));

		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0, notificationIntent, 0);
		notification.setLatestEventInfo(
				ctx, 
				res.getString(R.string.app_name), 
				task.getName() + " " + res.getString(R.string.notification_msg), 
				contentIntent
		);
		
		Log.i(TaskManager.TAG, "Creating android notification");
		nm.notify((int)task.getId(), notification);
	}

}
