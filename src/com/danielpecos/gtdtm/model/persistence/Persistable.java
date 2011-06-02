package com.danielpecos.gtdtm.model.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface Persistable {
	long store(Context context);
	boolean remove(Context context, SQLiteDatabase dbParent);
	boolean load(SQLiteDatabase db, Cursor cursor);
}
