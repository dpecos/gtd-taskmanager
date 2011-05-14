package com.danielpecos.gtm.model.persistence;

import android.database.Cursor;

public interface Persistable {
	long store(GTDSQLHelper helper);
	boolean remove(GTDSQLHelper helper);
	boolean load(Cursor cursor);
}
