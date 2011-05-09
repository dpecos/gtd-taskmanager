package com.danielpecos.gtm.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SimpleListAdapter extends BaseAdapter {
	private Context context;
	
	private ArrayList<HashMap<String, String>> itemData;
	private int item;
	private String[] itemNames;
	private int[] itemIds;
	ArrayList<HashMap<String, Object>> itemEvents;
	
	public SimpleListAdapter(Context context,
			ArrayList<HashMap<String, String>> itemData, int item,
			String[] itemNames, int[] itemIds, ArrayList<HashMap<String, Object>> itemsEvents) {
		this.context = context;
		this.itemData = itemData;
		this.item = item;
		this.itemNames = itemNames;
		this.itemIds = itemIds;
		this.itemEvents = itemsEvents;
	}

	@Override
	public int getCount() {
		return this.itemData.size();
	}

	@Override
	public Object getItem(int position) {
		return this.itemData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(this.item, parent, false);
		}

		int i = 0;
		for (int elemId : this.itemIds) {
			View v = convertView.findViewById(elemId);
			String keyName = this.itemNames[i++];
			String value = this.itemData.get(position).get(keyName);
			Object event = this.itemEvents.get(position).get(keyName);
			setViewValue(v, value, event);
		}
		return convertView;
	}
	
	public static void setViewValue(View v, String value, Object event) {
		if (v instanceof CheckBox) {
			((CheckBox) v).setChecked(Boolean.parseBoolean(value));
			if (event != null) {
				((CheckBox) v).setOnCheckedChangeListener((OnCheckedChangeListener) event);
			}
		} else if (v instanceof ImageView) {
			((ImageView) v).setImageResource(Integer.parseInt(value));
		} else if (v instanceof TextView) {
			((TextView)v).setText(value);
		}
	}
}
