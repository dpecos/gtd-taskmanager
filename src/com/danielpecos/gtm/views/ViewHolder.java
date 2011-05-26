package com.danielpecos.gtm.views;

import java.util.HashMap;

import android.view.View;

public abstract class ViewHolder {
	protected View view;
	protected HashMap<Integer, Object[]> viewListeners;
	
	private boolean callbacksEnabled = true;
	
	public abstract HashMap<String, Object> getListFields();
	
	public abstract void updateView();
	
	public void setCallbacksEnabled(boolean callbacksEnabled) {
		this.callbacksEnabled = callbacksEnabled;
	}
	
	public boolean isCallbacksEnabled() {
		return this.callbacksEnabled;
	}
	
	public ViewHolder() {
		this.viewListeners = new HashMap<Integer, Object[]>();
	}
	
	public ViewHolder(View view) {
		this();
		this.view = view;
	}
	
	public void setView(View view) {
		this.view = view;
	}
	
	public View getView(int id) {
		return this.view.findViewById(id);
	}

	public void registerChainedFieldEvents(Integer viewId, Object[] fieldEvents) {
		this.viewListeners.put(viewId, fieldEvents);
	}
}