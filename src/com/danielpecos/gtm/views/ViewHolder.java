package com.danielpecos.gtm.views;

import java.util.HashMap;

import com.danielpecos.gtm.R;

import android.view.View;

public abstract class ViewHolder {
	protected View view;
	
	public abstract HashMap<String, Object> getListFields();
	
	public abstract void updateView();
	
	public ViewHolder(View view) {
		this.view = view;
	}
	
	public void setView(View view) {
		this.view = view;
	}
	
	public View getView(int id) {
		return this.view.findViewById(id);
	}

}