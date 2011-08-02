package com.danielpecos.gtdtm.activities;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.accounts.Account;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.utils.google.AccountChooser;
import com.danielpecos.gtdtm.utils.google.AuthManager;
import com.danielpecos.gtdtm.utils.google.AuthManager.AuthCallback;
import com.danielpecos.gtdtm.utils.google.AuthManagerFactory;
import com.danielpecos.gtdtm.utils.google.Constants;

public class GoogleAccountActivity extends Activity {
	public static final String GOOGLE_AUTH_TOKEN = "google_authToken";
	public static final String GOOGLE_ACCOUNT_NAME = "google_accountName";

	private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";
	private static final int REQUEST_AUTHENTICATE = 0;
	private static final int DIALOG_ACCOUNTS = 0;

	private Long contextId;

	//new
	private AuthManager lastAuth;
	private final HashMap<String, AuthManager> authMap = new HashMap<String, AuthManager>();
	private final AccountChooser accountChooser = new AccountChooser();


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_google_account);

		if (TaskManager.isFullVersion(this)) {
			findViewById(R.id.gtasks_freeVersion_message_1).setVisibility(View.GONE);
			findViewById(R.id.gtasks_freeVersion_message_2).setVisibility(View.GONE);
		}

		Logger.getLogger("com.google.api.client").setLevel(Level.ALL);

		Boolean invalidate = (Boolean)getIntent().getSerializableExtra("invalidate_token");
		contextId = (Long)getIntent().getSerializableExtra("context_id");

		authenticate(AUTH_TOKEN_TYPE);
	}

	//	@Override
	//	protected Dialog onCreateDialog(int id) {
	//		switch (id) {
	//		case DIALOG_ACCOUNTS:
	//
	//		}
	//		return null;
	//	}

	private void authenticate(final String service) {
		lastAuth = authMap.get(service);
		if (lastAuth == null) {
			Log.i(TaskManager.TAG, "Creating a new authentication for service: " + service);
			lastAuth = AuthManagerFactory.getAuthManager(this,
					Constants.GET_LOGIN,
					null,
					true,
					service);
			authMap.put(service, lastAuth);
		}

		Log.d(TaskManager.TAG, "Logging in to " + service + "...");
		if (AuthManagerFactory.useModernAuthManager()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					chooseAccount(service);
				}
			});
		} else {
			doLogin(service, null);
		}
	}

	private void chooseAccount(final String service) {
		accountChooser.chooseAccount(GoogleAccountActivity.this, new AccountChooser.AccountHandler() {
			@Override
			public void onAccountSelected(Account account) {
				if (account == null) {
					finish();
					return;
				}

				doLogin(service, account);
			}
		});
	}

	private void doLogin(final String service, final Object account) {
		lastAuth.doLogin(new AuthCallback() {
			@Override
			public void onAuthResult(boolean success) {
				Log.i(TaskManager.TAG, "Login success for " + service + ": " + success);
				if (!success) {
					//					executeStateMachine(SendState.SHOW_RESULTS);
					return;
				}

				onLoginSuccess(account);
			}
		}, account);
	}

	private void onLoginSuccess(Object account) {
		SharedPreferences settings = TaskManager.getPreferences();
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(GOOGLE_ACCOUNT_NAME, ((Account)account).name);
		editor.putString(GOOGLE_AUTH_TOKEN, lastAuth.getAuthToken());
		editor.commit();

		Intent resultIntent = new Intent();
		resultIntent.putExtra("context_id", contextId);

		Log.d(TaskManager.TAG, "GTasks: finishing GoogleAccountActivity");

		this.setResult(RESULT_OK, resultIntent);
		this.finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
//			if (resultCode == RESULT_OK) {
//				gotAccount(false);
//			} else {
//				showDialog(DIALOG_ACCOUNTS);
//			}
			authenticate(AUTH_TOKEN_TYPE);
			break;
		}
	}

}
