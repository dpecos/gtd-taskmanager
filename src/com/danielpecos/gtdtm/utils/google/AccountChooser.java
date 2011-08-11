package com.danielpecos.gtdtm.utils.google;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.TaskManager;

/**
 * Choose which account to upload track information to.
 */
public class AccountChooser {

	private static final String ACCOUNT_TYPE = "com.google";

	private int selectedAccountIndex = -1;
	private Account selectedAccount = null;

	/**
	 * An interface for receiving updates once the user has selected the account.
	 */
	public interface AccountHandler {
		/**
		 * Handle the account being selected.
		 * @param account The selected account or null if none could be found
		 */
		public void onAccountSelected(Account account);
	}

	/**
	 * Chooses the best account to upload to.
	 * If no account is found the user will be alerted.
	 * If only one account is found that will be used.
	 * If multiple accounts are found the user will be allowed to choose.
	 *
	 * @param activity The parent activity
	 * @param handler The handler to be notified when an account has been selected
	 */
	public void chooseAccount(final Activity activity, final AccountHandler handler) {
		final Account[] accounts = AccountManager.get(activity).getAccountsByType(ACCOUNT_TYPE);
		SharedPreferences preferences = TaskManager.getPreferences();

		if (preferences.getString(GoogleClient.GOOGLE_ACCOUNT_NAME, null) != null) {
			String accountName = preferences.getString(GoogleClient.GOOGLE_ACCOUNT_NAME, null);
			for (Account account : accounts) {
				if (account.name.equalsIgnoreCase(accountName)) {
					this.selectedAccount = account;
					break;
				}
			}
			handler.onAccountSelected(selectedAccount);
		} else {
			if (accounts.length < 1) {
				alertNoAccounts(activity, handler);
				return;
			}
			if (accounts.length == 1) {
				handler.onAccountSelected(accounts[0]);
				return;
			}

			// Let the user choose.
			Log.e(TaskManager.TAG, "Multiple matching accounts found.");
			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(R.string.gtasks_selectAccount);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();

					if (selectedAccountIndex >= 0) {
						selectedAccount = accounts[selectedAccountIndex];
					}

					handler.onAccountSelected(selectedAccount);
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					handler.onAccountSelected(null);
				}
			});
			String[] choices = new String[accounts.length];
			for (int i = 0; i < accounts.length; i++) {
				choices[i] = accounts[i].name;
			}
			builder.setSingleChoiceItems(choices, selectedAccountIndex,
					new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					selectedAccountIndex = which;
				}
			});
			builder.show();
		}
	}

	public void setChosenAccount(String accountName, String accountType) {
		selectedAccount = new Account(accountName, accountType);
	}

	/**
	 * Puts up a dialog alerting the user that no suitable account was found.
	 */
	private void alertNoAccounts(final Activity activity, final AccountHandler handler) {
		Log.e(TaskManager.TAG, "No matching accounts found.");
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.gtasks_noAccountFoundTitle);
		builder.setMessage(R.string.gtasks_noAccountFound);
		builder.setCancelable(true);
		builder.setNegativeButton(R.string.ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				handler.onAccountSelected(null);
			}
		});
		builder.show();
	}
}
