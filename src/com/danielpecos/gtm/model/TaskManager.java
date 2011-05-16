package com.danielpecos.gtm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.persistence.GTDSQLHelper;
import com.danielpecos.gtm.utils.ActivityUtils;


public class TaskManager {
	public static final String TAG = "GTD-TaskManager";
	
	static TaskManager instance;

	HashMap<Long, Context> contexts;

	public static TaskManager getInstance(android.content.Context ctx) {
		if (instance == null) {
			instance = new TaskManager(ctx);
		}
		return instance;
	}

	public static TaskManager reset(android.content.Context ctx) {
		instance = new TaskManager(ctx);
		return instance;
	}

	private TaskManager(android.content.Context ctx) {
		this.contexts = new LinkedHashMap<Long, Context>();
		this.loadDatabase(ctx);
	}

	public Context createContext(android.content.Context ctx, String name) {
		Context context = new Context(name);
		if (context.store(GTDSQLHelper.getInstance(ctx)) > 0) {
			this.contexts.put(context.getId(), context);
			return context;
		} else { 
			return null;
		}
	}

	public boolean deleteContext(android.content.Context ctx, Context context) {
		if (context.remove(GTDSQLHelper.getInstance(ctx))) {
			this.contexts.remove(context.getId());
			return true;
		} else {
			return false;
		}
	}

	private boolean loadDatabase(android.content.Context ctx) {
		GTDSQLHelper helper = GTDSQLHelper.getInstance(ctx);

		helper.getWritableDatabase();

		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = null;

		try {
			cursor = db.query(GTDSQLHelper.TABLE_CONTEXTS, null, null, null, null, null, null);

			while (cursor.moveToNext()) {
				Context c = new Context(db, cursor);
				if (c.getId() < 0) {
					ActivityUtils.showMessage(ctx, R.string.error_loadingData);
					return false;
				} else {
					this.contexts.put(c.getId(), c);
				}
			}
			return true;
			
		} catch (SQLException e) {
			return false;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public Context getContext(Long id) {
		return this.contexts.get(id);
	}

	public Collection<Context> getContexts() {
		return this.contexts.values();
	}

	public Context elementAt(int contextPosition) {
		Context ctx = (Context) this.getContexts().toArray()[contextPosition];
		return ctx;
	}

}
