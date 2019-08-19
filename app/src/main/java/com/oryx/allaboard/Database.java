/*
 * 2/9/2011 -- Changed database read buffer size from 1024 to 10000
 * 1/12/2015 -- Database in now embedded in the .apk - the program no longer downloads anything
 */


package com.oryx.allaboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/*
 * When importing the Metro-North files, make sure you get rid of the blanks in the CSV files
 */
public class Database {
	private  static SQLiteDatabase db = null;
    private static final String DATABASE_NAME = "allaboard";
    private static final int DATABASE_VERSION = 1;
    static boolean DatabaseBusy = false;
    public static final String LOG_TAG = "allaboard";
    SharedPreferences Settings;
    //Can't use boolean because we need a null value
    private static String IsHoliday = null;
    private HashMap<Integer, String> serviceIdMap;
    /*
     * When using this IP address, if errors are occurring, check the IP address in System Preferences
     */
    private String urlBase = "http://192.168.1.7/nextTrain/data/";  //home

    //private String urlBase = "http://www.oryxtech.net/";


	public boolean checkIfDatabaseCopyNeeded(Context context, AllAboardApp app){
		File file = context.getDatabasePath("allaboard");
		long flen = file.length();
      if (file.exists() == false || file.length() < 1000000 ) {
          boolean b = copyDatabaseFromAssets(context, app, file, file.length());
          if (b == false) return b;
        }

		db = SQLiteDatabase.openDatabase(file.getPath(), null, 0);
		return true;
	
	}

    public boolean copyDatabaseFromAssets(Context context, AllAboardApp app, File file, long fileLength){
        AssetManager am = context.getAssets();
        try {
            InputStream inputStream = am.open("AllaboardSqlite.mp3");
            String dbDirectoryName = file.getPath();
            int index = dbDirectoryName.lastIndexOf("/");
            dbDirectoryName = dbDirectoryName.substring(0,index);
            File dbDirectory = new File(dbDirectoryName);
            if (!dbDirectory.exists()){
                dbDirectory.mkdir();
            }

            if (file.exists()) {
                file.delete();
            }
            //this will be used to write the data from the assets directory into the file we created
            OutputStream outputStream = new FileOutputStream(file.getPath());
            //variable to store total copied bytes
            int bytesCopied = 0;

            //create a buffer...
            byte[] buffer = new byte[30000];

            //get data
            int bufferLength;
            //now, read through the input buffer and write the contents to the file
            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
                //add the data in the buffer to the file in the file output stream (the file on the sd card )
                outputStream.write(buffer, 0, bufferLength);
                //add up the size so we know how much is downloaded
                bytesCopied += bufferLength;
                //this is where you would do something to report the progress
                //updateProgress(downloadedSize, totalSize);

            }
            //close the output stream when done
            outputStream.close();
            inputStream.close();
            return true;


        }
        catch (IOException e){
            System.out.println("Error copying database: " + e.getMessage());
            return false;
        }
    }
	
	public Map<String, Integer> getRoutesMap() {
		String select = "SELECT route_id, route_long_name FROM routes";
        Cursor mCursor = db.rawQuery(select, null);
        mCursor.moveToFirst();
        int count = mCursor.getCount();
        Map<String, Integer> routeMap = new HashMap<String, Integer>();
        for (int i=0; i<count; i++){
        	int id = mCursor.getInt(0);
        	String longName = mCursor.getString(1);
        	routeMap.put(longName, new Integer(id));
        	
        	mCursor.moveToNext();
         	
        }
        mCursor.close();
        return routeMap;
	}
	
	public String[] getTripsInfo(int station1, int station2) {
		
        String now = Utils.getTimeNow();
        
		String select = "SELECT s1.departure_time, s2.arrival_time, s1.trip_id from stop_times s1, stop_times s2 where " +
			"s1.trip_id = s2.trip_id and s1.stop_id = " + station1 + 
			" and s2.stop_id = " + station2;    
       
        		
        Cursor mCursor = db.rawQuery(select, null);
        mCursor.moveToFirst();
        int count = mCursor.getCount();


        String[] departures = new String[count];
        
        for (int i=0; i<count; i++){
        	String leave = mCursor.getString(0);
        	leave = Utils.convertEarlyTimes(leave, +24);
        	String arrive = mCursor.getString(1);
        	arrive = Utils.convertEarlyTimes(arrive, +24);

        	String tripid1 = mCursor.getString(2);
         	departures[i] = leave + "," + arrive + "," + tripid1;
        	
        	mCursor.moveToNext();
         	
        }
        Arrays.sort(departures);
        mCursor.close();
        //need to sort departures array here
        return departures;
	}
	
 
/*	
	public String getServiceIdForTrip(int tripId){
		String select = "SELECT service_id from trips where trip_id = " + tripId;
		
		Cursor mCursor = db.rawQuery(select, null);
	    mCursor.moveToPosition(0);
	    //int count = mCursor.getCount();
	    //if (count != 1) return null;
	    String serviceId = mCursor.getString(0);
	    return serviceId;
	}
*/	
	
	public void makeServiceIdMap() {
		//trip_id, service_id
        AcraStuff.writeErrorMessage("In makeServiceIdMap");
        serviceIdMap = new HashMap<Integer, String>();
		String select = "SELECT trip_id, service_id from trips";
		
		Cursor mCursor = db.rawQuery(select, null);
        AcraStuff.writeErrorMessage("In makeServiceIdMap, value of mCursor after query is: " + mCursor);

        mCursor.moveToFirst();
	    int count = mCursor.getCount();
        AcraStuff.writeErrorMessage("In makeServiceIdMap, count of records returned is: " + count);

        for (int i=0; i<count; i++) {
	    	serviceIdMap.put(mCursor.getInt(0), mCursor.getString(1));
	    	mCursor.moveToNext();
	    }
	    mCursor.close();
        AcraStuff.writeErrorMessage("In makeServiceIdMap, after mCursor.close()");

    }
	
	public String getServiceIdForTrip(int tripId){
		if (tripId <= 0) return null;
		String serviceId = (String)serviceIdMap.get(tripId); 
		return serviceId;
	}
	
	/* Check for holiday 
	 * Select service_id from calendar_dates (aka holiday table) where date = today's date
	 * if (service_id == null) getServiceIdForTrip (see below)
	 */
	//String service_id = DatabaseObject.getServiceIdFromHolidayTable();
	//TODO -- Add trip_id as parameter and search by it as well
	public String getExceptionTypeFromHolidayTable(int tripId, String serviceId){
		String date = Utils.getTodayDate();
		
		String select = "SELECT exception_type from calendar_dates "
		+	" where service_id = "  + serviceId + " and date = " + date;

		
		Cursor mCursor = db.rawQuery(select, null);
	   
	    int count = mCursor.getCount();
	    if (count < 1) {
	    	mCursor.close();
	    	return "notfound";
	    }
	    mCursor.moveToFirst();
	
	    String exceptionType = mCursor.getString(0);
	 
	    mCursor.close();

	    if (exceptionType == null) return "notfound";
	    if (exceptionType.equals("1")) return "add";
	    if (exceptionType.equals("2")) return "remove";
	    return "error";
	}
	
	
	public Map<String, Integer> getStationsMap() {
		String select = "SELECT stop_id, stop_name FROM stops";
        Cursor mCursor = db.rawQuery(select, null);
        mCursor.moveToFirst();
        int count = mCursor.getCount();
        Map<String, Integer> stationMap = new HashMap<String, Integer>();
        for (int i=0; i<count; i++){
        	int id = mCursor.getInt(0);
        	String stopName = mCursor.getString(1);
        	stationMap.put(stopName, new Integer(id));
        	
        	mCursor.moveToNext();
         	
        }
        mCursor.close();
        return stationMap;
	}
/*	
	public HashMap<String, String> getTrainCalendar(){
		String select = "SELECT service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday FROM calendar";
		Cursor mCursor = db.rawQuery(select, null);
		mCursor.moveToFirst();
        int count = mCursor.getCount();
        //System.out.println("In getTrainCalendar, count =" + count);
        HashMap<String, String> calendarMap = new HashMap<String, String>();
        for (int i=0; i<count; i++){
        	String service_id = mCursor.getString(0);
        	int monday = mCursor.getInt(1);
        	int tuesday = mCursor.getInt(2);
        	int wednesday = mCursor.getInt(3);
        	int thursday = mCursor.getInt(4);
        	int friday = mCursor.getInt(5);
        	int saturday = mCursor.getInt(6);
        	int sunday = mCursor.getInt(7);
        	String calendarString = Utils.makeCalendarString(monday, tuesday, wednesday, thursday, friday, saturday, sunday);
        	calendarMap.put(service_id, calendarString);
        	
        	//calendarMap.put(stopName, new Integer(id));
        	
        	mCursor.moveToNext();
         	
        }
        mCursor.close();
        return calendarMap;
	
	}
	*/
	
	public HashMap<String, String> getTrainCalendar(){
		String select = "SELECT service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday FROM calendar";
		Cursor mCursor = db.rawQuery(select, null);
		mCursor.moveToFirst();
        int count = mCursor.getCount();
        //System.out.println("In getTrainCalendar, count =" + count);
        HashMap<String, String> calendarMap = new HashMap<String, String>();
        for (int i=0; i<count; i++){
        	String service_id = mCursor.getString(0);
        	String monday = mCursor.getString(1);
        	String tuesday = mCursor.getString(2);
        	String wednesday = mCursor.getString(3);
        	String thursday = mCursor.getString(4);
        	String friday = mCursor.getString(5);
        	String saturday = mCursor.getString(6);
        	String sunday = mCursor.getString(7);
        	String calendarString = Utils.makeCalendarString(monday, tuesday, wednesday, thursday, friday, saturday, sunday);
        	calendarMap.put(service_id, calendarString);
        	
        	//calendarMap.put(stopName, new Integer(id));
        	
        	mCursor.moveToNext();
         	
        }
        mCursor.close();
        return calendarMap;
	
	}
	
	
	
	public String findNearestStation(Location here){
		String select = "SELECT stop_lat, stop_lon, stop_name FROM stops";
        Cursor mCursor = db.rawQuery(select, null);
        mCursor.moveToFirst();
        int count = mCursor.getCount();
        String closestStation = "";
        double leastDistance = 100000.0;
        for (int i=0; i<count; i++){
        	double lat = mCursor.getDouble(0);
        	double lon = mCursor.getDouble(1);
         	String stopName = mCursor.getString(2);
         	double sum = Math.pow((lat - here.getLatitude()),2)  + Math.pow((lon - here.getLongitude()), 2);
         	double distance = Math.sqrt(sum);
         	if (distance < leastDistance) {
         		leastDistance = distance;
         		closestStation = stopName;
         	}
         	mCursor.moveToNext();
         	
        }
        mCursor.close();
        return closestStation;
	}


	
	
    public Cursor getRouteNameFromRouteId(long rowId) throws SQLException 
    {
        String select = "SELECT route_long_name FROM routes";
        Cursor mCursor = db.rawQuery(select, null);
                  
/*
        Cursor mCursor =
                db.query ("routes", new String[] {
                		"route_long_name"
                   		}, 
                   		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
           
        }
 */     
        return mCursor;
    }
	
	
	  private static class DatabaseHelper extends SQLiteOpenHelper 
	    {
		    
	        DatabaseHelper(Context context, String sqlString) 
	        {
	            super(context, DATABASE_NAME, null, DATABASE_VERSION);
	        }

	        @Override
	        public void onCreate(SQLiteDatabase db) 
	        {
	           
	        }
	        

	        @Override
	        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
	        int newVersion) 
	        {
	            db.execSQL("DROP TABLE IF EXISTS titles");
	            onCreate(db);
	            
	        }
	    }    


}
