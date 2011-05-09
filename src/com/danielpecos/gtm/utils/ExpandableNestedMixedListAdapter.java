package com.danielpecos.gtm.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableNestedMixedListAdapter extends BaseExpandableListAdapter {
	private Context context;
	
	private ArrayList<HashMap<String, String>> groupData;
	private int groupItem;
	private String[] groupElemNames;
	private int[] groupElemenIds;

	private ArrayList<ArrayList<HashMap<String, String>>> childrenData1;
	private int children1Item;
	private String[] children1ElemNames;
	private int[] children1ElemIds;
	
	private ArrayList<ArrayList<HashMap<String, String>>> childrenData2;
	private int children2Item;
	private String[] children2ElemNames;
	private int[] children2ElemIds;


	public ExpandableNestedMixedListAdapter(
			Context context,
			ArrayList<HashMap<String, String>> groupData, 
			int groupItem, String[] groupElemNames,	int[] groupElemenIds,
			ArrayList<ArrayList<HashMap<String, String>>> childrenData1,
			int children1Item, String[] children1ElemNames, int[] children1ElemIds,
			ArrayList<ArrayList<HashMap<String, String>>> childrenData2,
			int children2Item, String[] children2ElemNames, int[] children2ElemIds) {
		
		this.context = context;
		
		this.groupData = groupData;
		this.groupItem = groupItem;
		this.groupElemNames = groupElemNames;
		this.groupElemenIds = groupElemenIds;
		
		this.childrenData1 = childrenData1;
		this.children1Item = children1Item;
		this.children1ElemNames = children1ElemNames;
		this.children1ElemIds = children1ElemIds;

		this.childrenData2 = childrenData2;
		this.children2Item = children2Item;
		this.children2ElemNames = children2ElemNames;
		this.children2ElemIds = children2ElemIds;
	}

	public Object getChild(int groupPosition, int childPosition) {
		if (childPosition < this.childrenData1.get(groupPosition).size()) {
			return this.childrenData1.get(groupPosition).get(childPosition);
		} else {
			return this.childrenData2.get(groupPosition).get(childPosition - this.childrenData1.get(groupPosition).size());
		}
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildrenCount(int groupPosition) {
		return this.childrenData1.get(groupPosition).size() + this.childrenData2.get(groupPosition).size();
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		
		if (childPosition < this.childrenData1.get(groupPosition).size()) {
			if (convertView == null || convertView.getId() != this.children1Item) {
				LayoutInflater mInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(this.children1Item, null);
			}

			int i = 0;
			for (int elemId : this.children1ElemIds) {
				String keyName = this.children1ElemNames[i++];
				String value = this.childrenData1.get(groupPosition).get(childPosition).get(keyName);
				View v = convertView.findViewById(elemId);
				this.setViewValue(v, value);
			}

		} else {
		
			if (convertView == null || convertView.getId() != this.children2Item) {
				LayoutInflater mInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(this.children2Item, null);
			}

			int i = 0;
			for (int elemId : this.children2ElemIds) {
				String keyName = this.children2ElemNames[i++];
				String value = this.childrenData2.get(groupPosition).get(childPosition - this.childrenData1.get(groupPosition).size()).get(keyName);
				View v = convertView.findViewById(elemId);
				this.setViewValue(v, value);
			}
			
		}
	
		return convertView;


	}

	private void setViewValue(View v, String value) {
		if (v instanceof CheckBox) {
			((CheckBox) v).setChecked(Boolean.parseBoolean(value));
		} else if (v instanceof ImageView) {
			((ImageView) v).setImageResource(Integer.parseInt(value));
		} else if (v instanceof TextView) {
			((TextView)v).setText(value);
		}
	}

	public Object getGroup(int groupPosition) {
		return this.groupData.get(groupPosition);
	}

	public int getGroupCount() {
		return this.groupData.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(this.groupItem, null);
		}

		int i = 0;
		for (int elemId : this.groupElemenIds) {
			String keyName = this.groupElemNames[i++];
			String value = this.groupData.get(groupPosition).get(keyName);
			View v = convertView.findViewById(elemId);
			this.setViewValue(v, value);
		}

		return convertView;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}

}