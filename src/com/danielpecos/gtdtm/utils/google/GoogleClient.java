package com.danielpecos.gtdtm.utils.google;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.danielpecos.gtdtm.model.TaskManager;
import com.danielpecos.gtdtm.utils.google.AccountChooser.AccountHandler;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;


public class GoogleClient {

	public static final String GOOGLE_ACCOUNT_NAME = "google_accountName";

	private static final String AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/tasks";

	private GoogleAccountManager accountManager;
	private Account selectedAccount = null;
	private String authToken = null;

	public interface AuthCallback {
		void onAuthResult(String authToken);
	}


	public void selectGoogleAccount(final Activity activity, final AuthCallback authCallback) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {

				AccountChooser chooser = new AccountChooser();

				chooser.chooseAccount(activity, new AccountHandler() {
					@Override
					public void onAccountSelected(Account account) {
						GoogleClient.this.selectedAccount = account;
						if (authCallback != null) {
							GoogleClient.this.login(activity, account, authCallback);
						}
					}
				});
			}
		});
	}

	public void login(final Activity activity, final AuthCallback authCallback) {
		if (this.accountManager == null) {
//			this.accountManager = AccountManager.get(activity);
			this.accountManager = new GoogleAccountManager(activity);
		}
		if (this.selectedAccount == null) {
			selectGoogleAccount(activity, authCallback);
		} else {
			login(activity, this.selectedAccount, authCallback);
		}
	}

	private void login(final Activity activity, final Account account, final AuthCallback authCallback) {
		if (account == null || authCallback == null) {
			throw new IllegalArgumentException("Login requires to provide an account and a callback to invoke when finished");
		} else {
			this.accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, null, activity, new AccountManagerCallback<Bundle>() {
				public void run(AccountManagerFuture<Bundle> future) {
					try {
						GoogleClient.this.authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
						Log.i(TaskManager.TAG, "Got auth token");

						/*Bundle result = future.getResult();

						// AccountManager needs user to grant permission
						if (result.containsKey(AccountManager.KEY_INTENT)) {
							Intent intent = (Intent) result.get(AccountManager.KEY_INTENT);
							clearNewTaskFlag(intent);
							activity.startActivityForResult(intent, Constants.GET_LOGIN);
						} else {
							authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
							Log.i(TaskManager.TAG, "Got auth token.");
							runAuthCallback();
						}*/
					} catch (OperationCanceledException e) {
						Log.e(TaskManager.TAG, "Operation Canceled", e);
					} catch (IOException e) {
						Log.e(TaskManager.TAG, "IOException", e);
					} catch (AuthenticatorException e) {
						Log.e(TaskManager.TAG, "Authentication Failed", e);
					}

					authCallback.onAuthResult(GoogleClient.this.authToken);
				}
			}, null /* handler */);
		}
	}

	public void invalidateAuthToken() {
		if (this.authToken != null) {
			Log.i(TaskManager.TAG, "GTasks: invalidating authToken: " + authToken);
			this.accountManager.invalidateAuthToken(this.authToken);
			this.authToken = null;
		}
	}
}
