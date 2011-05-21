package com.danielpecos.gtm.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

public class ExpandableNestedMixedListAdapter extends BaseExpandableListAdapter {
	private HashMap<String, View> viewsCache;

	private Context context;

	private ArrayList<HashMap<String, Object>> groupData;
	private int groupItem;
	private String[] groupElemNames;
	private int[] groupElemenIds;

	private ArrayList<ArrayList<HashMap<String, Object>>> childrenData1;
	private int children1Item;
	private String[] children1ElemNames;
	private int[] children1ElemIds;
	private RowDisplayListener children1Listener;

	private ArrayList<ArrayList<HashMap<String, Object>>> childrenData2;
	private int children2Item;
	private String[] children2ElemNames;
	private int[] children2ElemIds;
	private RowDisplayListener children2Listener;

	public ExpandableNestedMixedListAdapter(
			Context context,
			ArrayList<HashMap<String, Object>> groupData, 
			int groupItem, String[] groupElemNames,	int[] groupElemenIds,
			ArrayList<ArrayList<HashMap<String, Object>>> childrenData1,
			int children1Item, String[] children1ElemNames, int[] children1ElemIds, RowDisplayListener children1Listener,
			ArrayList<ArrayList<HashMap<String, Object>>> childrenData2,
			int children2Item, String[] children2ElemNames, int[] children2ElemIds, RowDisplayListener rowDisplayListener) {

		this.viewsCache = new HashMap<String, View>();

		this.context = context;

		this.groupData = groupData;
		this.groupItem = groupItem;
		this.groupElemNames = groupElemNames;
		this.groupElemenIds = groupElemenIds;

		this.childrenData1 = childrenData1;
		this.children1Item = children1Item;
		this.children1ElemNames = children1ElemNames;
		this.children1ElemIds = children1ElemIds;
		this.children1Listener = children1Listener;

		this.childrenData2 = childrenData2;
		this.children2Item = children2Item;
		this.children2ElemNames = children2ElemNames;
		this.children2ElemIds = children2ElemIds;
		this.children2Listener = rowDisplayListener;
	}

	public Object getChild(int groupPosition, int childPosition) {
		if (childPosition < this.childrenData1.get(groupPosition).size()) {
			return this.childrenData1.get(groupPosition).get(childPosition);
		} else {
			return this.childrenData2.get(groupPosition).get(childPosition - this.childrenData1.get(groupPosition).size());
		}
	}

	public long getChildId(int groupPosition, int childPosition) {
		return Integer.parseInt("" + groupPosition + childPosition);
	}

	public int getChildrenCount(int groupPosition) {
		if (groupPosition < this.childrenData1.size() && groupPosition < this.childrenData2.size()) {
			return this.childrenData1.get(groupPosition).size() + this.childrenData2.get(groupPosition).size();
		} else {
			return 0;
		}
	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,	View convertView, ViewGroup parent) {

		if (this.viewsCache.containsKey(groupPosition + "-" + childPosition)) {
			convertView = this.viewsCache.get(groupPosition + "-" + childPosition);
		} else {
			if (childPosition < this.childrenData1.get(groupPosition).size()) {
				if (convertView == null || convertView.getId() != this.children1Item) {
					LayoutInflater mInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
					convertView = mInflater.inflate(this.children1Item, parent, false);
				}

				int i = 0;
				for (int elemId : this.children1ElemIds) {
					String keyName = this.children1ElemNames[i++];
					Object value = this.childrenData1.get(groupPosition).get(childPosition).get(keyName);
					View v = convertView.findViewById(elemId);
					SimpleListAdapter.setViewValue(v, value);
				}

			} else {
				int realChildPosition = childPosition - this.childrenData1.get(groupPosition).size();

				if (convertView == null || convertView.getId() != this.children2Item) {
					LayoutInflater mInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
					convertView = mInflater.inflate(this.children2Item, parent, false);
				}

				int i = 0;
				for (int elemId : this.children2ElemIds) {
					String keyName = this.children2ElemNames[i++];
					Object value = this.childrenData2.get(groupPosition).get(realChildPosition).get(keyName);
					View v = convertView.findViewById(elemId);
					SimpleListAdapter.setViewValue(v, value);
				}
			}
			
			if (childPosition < this.childrenData1.get(groupPosition).size()) {
				if (this.children1Listener != null) {
					this.children1Listener.onViewSetUp(convertView, (HashMap<String, Object>)this.getChild(groupPosition, childPosition));
				}
			} else {
				if (this.children2Listener != null) {
					this.children2Listener.onViewSetUp(convertView, (HashMap<String, Object>)this.getChild(groupPosition, childPosition));
				}
			}
			
			this.viewsCache.put(groupPosition + "-" + childPosition, convertView);
		}

		return convertView;


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

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(this.groupItem, parent, false);
		}

		int i = 0;
		for (int elemId : this.groupElemenIds) {
			String keyName = this.groupElemNames[i++];
			Object value = this.groupData.get(groupPosition).get(keyName);
			View v = convertView.findViewById(elemId);
			SimpleListAdapter.setViewValue(v, value);
		}

		/*if (this.getChildrenCount(groupPosition) == 0)
			convertView.setEnabled(false);*/

		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	public interface RowDisplayListener {
		public void onViewSetUp(View view, HashMap<String, Object> data);
	}
}