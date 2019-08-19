/*
 * 02/16/2011 -- fixed version number
 * 				 put program on SD card
 */


package com.oryx.allaboard;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

public class AllAboardApp extends Activity {
	double myLatitute;
	double myLongitude;
	Activity CurrentActivity;
	Dialog mSplashDialog;
	
   /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
  		//setTheme(R.style.Theme);

        super.onCreate(savedInstanceState);
        Intent intent = new Intent();
	   	intent.setClassName("com.oryx.allaboard", "com.oryx.allaboard.SplashScreen");
	   	startActivity(intent);

  
  
    }
    
    @Override
    public void onStart() {
        super.onStart();
  
    }
    
    public void onConfigurationChanged(Configuration newConfig){
    	super.onConfigurationChanged(newConfig);
    }
    
  
 

}