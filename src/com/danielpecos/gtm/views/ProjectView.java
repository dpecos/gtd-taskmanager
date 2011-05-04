package com.danielpecos.gtm.views;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.danielpecos.gtm.R;

public class ProjectView extends View {
	private View mRow;
	private TextView project_name = null;
	private TextView project_description = null;

	public ProjectView(Context context, View row) {
		super(context);
		mRow = row;
	}
	public TextView getName() {
		if (project_name == null){
			project_name = (TextView) mRow.findViewById(R.id.project_name);
		}
		return project_name;
	} 
	public TextView getDescription() {
		if (project_description == null){
			project_description = (TextView) mRow.findViewById(R.id.project_description);
		}
		return project_description;
	} 
}
