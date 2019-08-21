/*
 * 02/16/2011 -- fixed version number
 * 				 put program on SD card
 */


package com.oryx.allaboard;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
 * 12/5/2011 Version 2.2 released -- the next release will be bug fixes
 * 12/6/2011 Changed the "No trains found" message
 * 12/6/2011 Fixed the NumberFormatException in Integer.parse() in isServerVersionGreater in Database.java.  This happens when there
 *  is garbage in the version.txt file on the server.  The fix is to allow the database to be downloaded, since we can't read the
 *  server version number. 
 * 12/7/2011 Validity checks and error messages in pickup message function
 * 08/21/2019 Version message
 */


public class MainApp extends Activity {
	double myLatitute;
	double myLongitude;
	Activity CurrentActivity;
	Dialog mSplashDialog;
	Timer GPSTimer;
	boolean locationFound = false;
	LocationListener mlocListener;

	
   /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    
    @Override
    public void onStart() {
        super.onStart();
        initializeApp();
  
  
    }
    
    @Override
    public void onRestart() {
        super.onRestart();
        Stations.getTripsAgain = false;
    }
    
    @Override
    public void onStop() {
        super.onStop();
        AcraStuff.errorStream.close();

    }

   
    private void initializeApp(){
    	CurrentActivity = this;
        setContentView(R.layout.main);
		setTitleColor(Color.WHITE);

		Button gpsButton = (Button) findViewById(R.id.nearest_station_id);
		gpsButton.setOnClickListener(gpsButtonClickListener);

        AcraStuff.writeErrorMessage("In MainApp.initializeApp(), calling Stations.init");
    	Stations.init(this, 0, "");
        }
        

    
    private OnClickListener gpsButtonClickListener = new OnClickListener() {
	    public void onClick(View v) {
	      // do something when the button is clicked
			ActivityCompat.requestPermissions(CurrentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
	    }
	};

	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					boolean b = checkLocationPermission();
					if (b == false) return;
					findLocation();

				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}
			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	public boolean checkLocationPermission()
	{
		String permission = "android.permission.ACCESS_FINE_LOCATION";
		int res = this.checkCallingOrSelfPermission(permission);
		return (res == PackageManager.PERMISSION_GRANTED);
	}

	private void findLocation(){
		LocationManager mlocManager = (LocationManager)CurrentActivity.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = mlocManager.getProviders(true);
		int numProviders = providers.size();

		mlocListener = new MyLocation(CurrentActivity, Globals.Db);
		locationFound = false;
		GPSTimer = new Timer();
		//Use GPS to look for location for 3 minutes
		GPSTimer.schedule(new GPSTimeout(), 3000);
		try {
			mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		}
		catch (SecurityException e){
			Toast.makeText( CurrentActivity, "SecurityException getting location is: " + e.getMessage(), Toast.LENGTH_SHORT ).show();
		}

	}

	class GPSTimeout extends TimerTask {
	    public void run() {
	      locationFound = false;
	      mlocListener = null;
	      GPSTimer.cancel(); 
	    }
	  }
    
    public void onConfigurationChanged(Configuration newConfig){
    	super.onConfigurationChanged(newConfig);
    }
    
    public class MyLocation implements LocationListener{
        Activity CurrentActivity;
        Database Db;
        
		public MyLocation(Activity t, Database db){
			CurrentActivity = t;
			Db = db;
		}
		
		public void onLocationChanged(Location loc)

		{
		locationFound = true;
	
		loc.getLatitude();
		loc.getLongitude();
		String nearestStation = Globals.Db.findNearestStation(loc);
		
        //Stop locationlistener
		LocationManager mlocManager = (LocationManager)CurrentActivity.getSystemService(Context.LOCATION_SERVICE);
		
        Stations.init(CurrentActivity, 0, nearestStation);
		mlocManager.removeUpdates(this);
		mlocManager = null;
		mlocListener = null;
	
	}


		public void onProviderDisabled(String provider)

		{

		Toast.makeText( CurrentActivity,

		"Gps Disabled",

		Toast.LENGTH_SHORT ).show();

		}


		public void onProviderEnabled(String provider)

		{

		Toast.makeText( CurrentActivity,

		"Gps Enabled",

		Toast.LENGTH_SHORT).show();

		}


		public void onStatusChanged(String provider, int status, Bundle extras)

		{
			Toast.makeText( CurrentActivity, "LocationManager status = " + status, Toast.LENGTH_SHORT).show();


		}
		


	  
	}
    
 
 

}