package com.oryx.allaboard;

import android.widget.TextView;

public class ErrorHandler {
	public void fatalError(String errMsg, AllAboardApp app){
		app.setContentView(R.layout.error);
		TextView tv = (TextView) app.findViewById(R.id.error_id);
		tv.setText(errMsg);
	
	}

}
