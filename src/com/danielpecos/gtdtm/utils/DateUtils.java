package com.danielpecos.gtdtm.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;

public class DateUtils {
	private static SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat rfc3339DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
	private static SimpleDateFormat rfc3339DateFormat_FractSecs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");

	public static Date parseDate(String str) {
		if (str != null) {
			Date date = null;
			try {
				date = rfc3339DateFormat_FractSecs.parse(str);
			} catch (ParseException e1) {
				if (date == null) {
					try {
						date = rfc3339DateFormat.parse(str);
					} catch (ParseException e2) {
						// this block of code assures backwards compatibility in ddbb dates
						try {
							date = iso8601Format.parse(str);
						} catch (ParseException e3) {
							Log.w(TaskManager.TAG, "Error parsing date", e3);
						}
					}
				}

			}
			return date;
		} else {
			return null;
		}
	}

	public static String formatDate(Date date) {
		return rfc3339DateFormat.format(date);
		//		return rfc3339DateFormat_FractSecs.format(date);
	}
}
