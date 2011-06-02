package com.danielpecos.gtdtm.views;

import java.util.HashMap;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielpecos.gtdtm.R;
import com.danielpecos.gtdtm.model.beans.Context;

public class ContextViewHolder extends ViewHolder {

	private Context context; 
	
	public ContextViewHolder(View view, Context ctx) {
		super(view);
		this.context = ctx;
	}

	@Override
	public HashMap<String, Object> getListFields() {
		HashMap<String, Object> contextData = new HashMap<String, Object>();
		
		contextData.put("_BASE_", context);
		contextData.put("id", context.getId());
		contextData.put("name", context.getName());
		
		return contextData;
	}

	@Override
	public void updateView(Activity activity) {
		((TextView)getView(R.id.context_name)).setText(this.context.getName());
		
		ImageView icon = (ImageView)getView(R.id.context_icon);
		if (this.context.getGoogleId() != null) {
			icon.setVisibility(View.VISIBLE);
		} else {
			icon.setVisibility(View.GONE);
		}
		
	}

}
