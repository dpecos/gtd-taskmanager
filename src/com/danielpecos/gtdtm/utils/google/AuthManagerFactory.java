package com.danielpecos.gtdtm.utils.google;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;

/**
 * A factory for getting the platform specific AuthManager.
 */
public class AuthManagerFactory {

	private AuthManagerFactory() {
	}

	/**
	 * Returns whether the modern AuthManager should be used
	 */
	public static boolean useModernAuthManager() {
		return Integer.parseInt(Build.VERSION.SDK) >= 7;
	}

	/**
	 * Get a right {@link AuthManager} for the platform.
	 * @return A new AuthManager
	 */
	public static AuthManager getAuthManager(Activity activity, int code,
			Bundle extras, boolean requireGoogle, String service) {
		if (useModernAuthManager()) {
			Log.i(TaskManager.TAG, "Creating modern auth manager: " + service);
			return new ModernAuthManager(activity, service);
		} else {
			Log.i(TaskManager.TAG, "Creating legacy auth manager: " + service);
			return new AuthManagerOld(activity, code, extras, requireGoogle, service);
		}
	}

}
