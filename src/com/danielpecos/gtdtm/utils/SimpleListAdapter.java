package com.danielpecos.gtdtm.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielpecos.gtdtm.utils.ExpandableNestedMixedListAdapter.RowDisplayListener;

public class SimpleListAdapter extends BaseAdapter {
	private Context context;
	
	private ArrayList<HashMap<String, Object>> itemData;
	private int item;
	private String[] itemNames;
	private int[] itemIds;
	private RowDisplayListener itemListener;
	
	public SimpleListAdapter(Context context,
			ArrayList<HashMap<String, Object>> itemData, int item,
			String[] itemNames, int[] itemIds, RowDisplayListener itemListener) {
		this.context = context;
		this.itemData = itemData;
		this.item = item;
		this.itemNames = itemNames;
		this.itemIds = itemIds;
		this.itemListener = itemListener;
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
			Object value = this.itemData.get(position).get(keyName);
			setViewValue(v, value);
		}
		if (this.itemListener != null) 
			this.itemListener.onViewSetUp(convertView, (HashMap<String, Object>)this.getItem(position));
		return convertView;
	}
	
	public static void setViewValue(View v, Object value) {
		if (v instanceof CheckBox) {
			((CheckBox) v).setChecked((Boolean) value);
		} else if (v instanceof ImageView) {
			((ImageView) v).setImageResource((Integer)value);
		} else if (v instanceof TextView) {
			((TextView)v).setText((String)value);
		}
	}
}
