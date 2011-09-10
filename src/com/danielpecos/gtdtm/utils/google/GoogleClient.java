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


	public Account getSelectedAccount() {
		return this.selectedAccount;
	}

	public void selectGoogleAccount(final Activity activity) {
		AccountChooser chooser = new AccountChooser();

		chooser.chooseAccount(activity, new AccountHandler() {
			@Override
			public void onAccountSelected(Account account) {
				GoogleClient.this.selectedAccount = account;
			}
		});
	}

	public String login(final Activity activity) {
		if (this.accountManager == null) {
			this.accountManager = new GoogleAccountManager(activity);
		}
		return login(activity, this.selectedAccount);
	}

	private String login(final Activity activity, final Account account) {
		if (account == null) {
			throw new IllegalArgumentException("Login requires to provide an account and a callback to invoke when finished");
		} else {
			//			this.accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, null, activity, new AccountManagerCallback<Bundle>() {
			//				public void run(AccountManagerFuture<Bundle> future) {
			//					try {
			//						GoogleClient.this.authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
			//						Log.i(TaskManager.TAG, "Got auth token");
			//
			//						/*Bundle result = future.getResult();
			//
			//						// AccountManager needs user to grant permission
			//						if (result.containsKey(AccountManager.KEY_INTENT)) {
			//							Intent intent = (Intent) result.get(AccountManager.KEY_INTENT);
			//							clearNewTaskFlag(intent);
			//							activity.startActivityForResult(intent, Constants.GET_LOGIN);
			//						} else {
			//							authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
			//							Log.i(TaskManager.TAG, "Got auth token.");
			//							runAuthCallback();
			//						}*/
			//					} catch (OperationCanceledException e) {
			//						Log.e(TaskManager.TAG, "Operation Canceled", e);
			//					} catch (IOException e) {
			//						Log.e(TaskManager.TAG, "IOException", e);
			//					} catch (AuthenticatorException e) {
			//						Log.e(TaskManager.TAG, "Authentication Failed", e);
			//					}
			//
			//					authCallback.onAuthResult(GoogleClient.this.authToken);
			//				}
			//			}, null /* handler */);

			AccountManagerFuture<Bundle> future = this.accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, null, activity, null, null /* handler */);
			try {
				this.authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
				Log.i(TaskManager.TAG, "Got auth token");
			} catch (OperationCanceledException e) {
				Log.e(TaskManager.TAG, "Operation Canceled", e);
			} catch (IOException e) {
				Log.e(TaskManager.TAG, "IOException", e);
			} catch (AuthenticatorException e) {
				Log.e(TaskManager.TAG, "Authentication Failed", e);
			}

			return this.authToken;
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
