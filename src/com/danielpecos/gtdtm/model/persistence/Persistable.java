package com.danielpecos.gtdtm.model.persistence;

import java.io.Serializable;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public interface Persistable extends Serializable{
	long store(Context context);
	boolean remove(Context context, SQLiteDatabase dbParent);
	boolean load(SQLiteDatabase db, Cursor cursor);
}
