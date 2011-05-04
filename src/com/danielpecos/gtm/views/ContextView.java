package com.danielpecos.gtm.views;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.danielpecos.gtm.R;

public class ContextView extends View {
	private View mRow;
	private TextView context_name = null;

	public ContextView(Context context, View row) {
		super(context);
		mRow = row;
	}
	public TextView getName() {
		if (context_name == null){
			context_name = (TextView) mRow.findViewById(R.id.context_name);
		}
		return context_name;
	}     
}
