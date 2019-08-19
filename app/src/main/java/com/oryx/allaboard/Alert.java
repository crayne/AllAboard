package com.oryx.allaboard;

import android.app.AlertDialog;
import android.content.DialogInterface;

public class Alert {
	Boolean doExit = null;
	public Alert (String msg, Boolean exit){
		doExit = exit;
		AlertDialog alertDialog = new AlertDialog.Builder(Utils.CurrentActivity).create();
		alertDialog.setMessage(msg);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
		      public void onClick(DialogInterface dialog, int id) {
		  		if (doExit == true) System.exit(-1);  
		        return;
		    } }); 
		alertDialog.show();
		return;

	}


}
