package com.danielpecos.gtm.activities;

import android.app.Activity;
import android.os.Bundle;

import com.danielpecos.gtm.R;
import com.danielpecos.gtm.model.TaskManager;
import com.danielpecos.gtm.model.beans.Context;
import com.danielpecos.gtm.model.beans.Project;
import com.danielpecos.gtm.model.beans.Task;

public class NewTask extends Activity {
	TaskManager taskManager;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.context_layout);
    }
    
    private void createTask() {
    	Task task = new Task();
    	/*
    	//TODO: recuperar contexto apropiado
    	Context context = taskManager.getContext(ctxName);
    	//TODO: recuperar proyecto apropiado
    	Project project = context.getProject(prjName);
    	
    	if (project != null) {
    		project.addTask(task);
    	} else {
    		context.addTask(task);
    	}
    	*/
    }
}