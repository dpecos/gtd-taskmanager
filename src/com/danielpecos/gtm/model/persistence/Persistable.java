package com.danielpecos.gtm.model.persistence;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface Persistable {
	long store(GTDSQLHelper helper);
	boolean remove(GTDSQLHelper helper);
	boolean load(SQLiteDatabase db, Cursor cursor);
}
