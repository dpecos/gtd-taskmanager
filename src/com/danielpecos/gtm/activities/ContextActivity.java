package com.danielpecos.gtm.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;

public class ContextActivity extends ListActivity {
	TaskManager taskManager;

	private class ContextViewHolder {
		private View mRow;
		private TextView context_name = null;
		private TextView elements_list = null;

		public ContextViewHolder(View row) {
			mRow = row;
		}
		public TextView getName() {
			if (context_name == null){
				context_name = (TextView) mRow.findViewById(R.id.context_name);
			}
			return context_name;
		}     
	}

	private class ContextItemAdapter extends ArrayAdapter<Context> {

		public ContextItemAdapter(android.content.Context context, int resource, int textViewResourceId, List<Context> objects) {
			super(context, resource, textViewResourceId, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ContextViewHolder holder = null;
			
			if (convertView == null) {
				LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.context_item, null);
				holder = new ContextViewHolder(convertView);
				convertView.setTag(holder);
			}
			
			Context ctx = this.getItem(position);
			
			holder = (ContextViewHolder) convertView.getTag();
			holder.getName().setText(ctx.getName());

			return convertView;
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.taskManager = new TaskManager();
		this.taskManager.createContext("Contexto 1").createProject("Proyecto 1.1");
		this.taskManager.createContext("Contexto 2");
		this.taskManager.createContext("Contexto 3");

		setContentView(R.layout.main);

		ContextItemAdapter adapter = new ContextItemAdapter(this, R.layout.context_item, R.id.context_name, new ArrayList<Context>(taskManager.getContexts()));
		this.setListAdapter(adapter);
		this.getListView().setTextFilterEnabled(true);

	}
}