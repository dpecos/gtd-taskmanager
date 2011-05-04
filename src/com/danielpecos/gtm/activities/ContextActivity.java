package com.danielpecos.gtm.activities;

import java.util.Collection;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.views.ContextView;
import com.danielpecos.gtm.views.ProjectView;

/**
 * Demonstrates expandable lists using a custom {@link ExpandableListAdapter}
 * from {@link BaseExpandableListAdapter}.
 */
public class ContextActivity extends ExpandableListActivity {

	TaskManager taskManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		this.taskManager = new TaskManager();
		this.taskManager.createContext("Contexto 1").createProject("Proyecto 1.1", "Descripción de proyecto 1.1");
		this.taskManager.createContext("Contexto 2");
		this.taskManager.createContext("Contexto 3");

		// Set up our adapter
		this.setListAdapter(new MyExpandableListAdapter(this));
		//registerForContextMenu(getExpandableListView());
	}

	/*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Sample menu");
        menu.add(0, 0, 0, R.string.expandable_list_sample_action);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

        String title = ((TextView) info.targetView).getText().toString();

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition); 
            Toast.makeText(this, title + ": Child " + childPos + " clicked in group " + groupPos,
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
            Toast.makeText(this, title + ": Group " + groupPos + " clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }*/


	public class MyExpandableListAdapter extends BaseExpandableListAdapter {
		private String[] groups;
		private String[][][] children;
		
		android.content.Context context;

		public MyExpandableListAdapter(android.content.Context context) {
			this.context = context;
			
			Collection<Context> contexts = taskManager.getContexts();
			this.groups = new String[contexts.size()];
			this.children = new String[contexts.size()][][];
			int i = 0;
			for (Context ctx : contexts) {
				this.groups[i] = ctx.getName();
				Collection<Project> projects = ctx.getProjects();
				this.children[i] = new String[projects.size()][];

				int j = 0;
				for (Project prj : projects) {
					this.children[i][j++] = new String[] {prj.getName(), prj.getDescription()};
				}

				i++;
			}
		}

		public Object getChild(int groupPosition, int childPosition) {
			return children[groupPosition][childPosition];
		}

		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		public int getChildrenCount(int groupPosition) {
			return children[groupPosition].length;
		}

		public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
				View convertView, ViewGroup parent) {
			/*
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;*/

			ProjectView view = null;

			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.project_item, null);
				view = new ProjectView(this.context, convertView);
				convertView.setTag(view);
			}

			view = (ProjectView) convertView.getTag();
			view.getName().setText(this.children[groupPosition][childPosition][0]);
			view.getDescription().setText(this.children[groupPosition][childPosition][1]);

			return convertView;


		}

		public Object getGroup(int groupPosition) {
			return groups[groupPosition];
		}

		public int getGroupCount() {
			return groups.length;
		}

		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
				ViewGroup parent) {
			/*TextView textView = getGenericView();
			textView.setText(getGroup(groupPosition).toString());
			return textView;*/
			
			ContextView view = null;

			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.context_item, null);
				view = new ContextView(this.context, convertView);
				convertView.setTag(view);
			}

			view = (ContextView) convertView.getTag();
			view.getName().setText(this.groups[groupPosition]);

			return convertView;
		}

		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public boolean hasStableIds() {
			return true;
		}

	}
}