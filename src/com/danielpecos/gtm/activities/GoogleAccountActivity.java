package com.danielpecos.gtm.activities;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.utils.GoogleTasksClient;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.tasks.v1.Tasks;

/*
 * http://code.google.com/p/google-api-java-client/wiki/AndroidAccountManager
 */
public class GoogleAccountActivity extends Activity {
	private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";
	private static final int REQUEST_AUTHENTICATE = 0;
	private static final int DIALOG_ACCOUNTS = 0;

	private static String apiKey = "AIzaSyB9Uw7kh3jdyoO9FkzTjAtAkf48on1HI8U";

	private static final String PREF = "com.danielpecos.gtm_preferences";

	// TODO(yanivi): save auth token in preferences
	public String authToken;
	public GoogleAccountManager accountManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		System.out.println("onCreate");
		super.onCreate(savedInstanceState);
		accountManager = new GoogleAccountManager(this);
		Logger.getLogger("com.google.api.client").setLevel(Level.ALL);
		gotAccount(false);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ACCOUNTS:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select a Google account");
			final Account[] accounts = accountManager.getAccounts();
			final int size = accounts.length;
			String[] names = new String[size];
			for (int i = 0; i < size; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gotAccount(accounts[which]);
				}
			});
			return builder.create();
		}
		return null;
	}

	void gotAccount(boolean tokenExpired) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		String accountName = settings.getString("google_accountName", null);
		Account account = accountManager.getAccountByName(accountName);
		if (account != null) {
			if (tokenExpired) {
				accountManager.invalidateAuthToken(authToken);
				authToken = null;
			}
			gotAccount(account);
			return;
		}
		showDialog(DIALOG_ACCOUNTS);
	}

	void gotAccount(final Account account) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("google_accountName", account.name);
		editor.commit();
		accountManager.manager.getAuthToken(
				account, AUTH_TOKEN_TYPE, true, new AccountManagerCallback<Bundle>() {

					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle bundle = future.getResult();
							if (bundle.containsKey(AccountManager.KEY_INTENT)) {
								Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
								intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivityForResult(intent, REQUEST_AUTHENTICATE);
							} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
								authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
								onAuthToken();
							}
						} catch (Exception e) {
							handleException(e);
						}
					}
				}, null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
			if (resultCode == RESULT_OK) {
				gotAccount(false);
			} else {
				showDialog(DIALOG_ACCOUNTS);
			}
			break;
		}
	}

	void handleException(Exception e) {
		e.printStackTrace();
		if (e instanceof HttpResponseException) {
			HttpResponse response = ((HttpResponseException) e).response;
			int statusCode = response.statusCode;
			try {
				response.ignore();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			// TODO(yanivi): should only try this once to avoid infinite loop
			if (statusCode == 401) {
				gotAccount(true);
				return;
			}
		}
		Log.e(TaskManager.TAG, e.getMessage(), e);
	}

	private void onAuthToken() {

		SharedPreferences settings = getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("google_authToken", authToken);
		editor.commit();
		
		new GoogleAccessProtectedResource(authToken) {
			@Override
			protected void onAccessToken(String accessToken) {
				gotAccount(true);
			}
		};

		HttpTransport transport = AndroidHttp.newCompatibleTransport();
		JacksonFactory jsonFactory = new JacksonFactory();
		Tasks service = new Tasks("Google-TaskSample/1.0", transport, jsonFactory);

		service.setAccessToken(authToken);
		service.accessKey = apiKey;

		GoogleTasksClient client = new GoogleTasksClient(service);
		TaskManager tm = TaskManager.getInstance(this);

		try {
			tm.synchronizeGTasks(this, tm.elementAt(0), client);
		} catch (Exception e) {
			handleException(e);
		}
	}
}
