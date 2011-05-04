package com.danielpecos.gtm.activities;

import android.app.Activity;
import android.os.Bundle;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.beans.Project;

public class ProjectActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.project_layout);
		
		Bundle extras = getIntent().getExtras();
		Project project = (Project) extras.getSerializable("project");
		
		this.setTitle(project.getName());
	}
}
