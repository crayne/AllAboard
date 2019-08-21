package com.oryx.allaboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;


public class Stations {
	static Map<String, Integer> StationMap;
	Database DatabaseObject;
	static Activity CurrentActivity;
	static ArrayAdapter<CharSequence> Adapter1;
	static ArrayAdapter<CharSequence> Adapter2;
	public static final String PREFS_NAME = "AllAboardFile";
	static SharedPreferences Settings;
	static Map<String, String> TrainCalendar;
	static ListView MainListView1, MainListView2;
	ArrayAdapter<String> ListAdapter1, ListAdapter2;
    static boolean getTripsAgain = true;
    static String[] trips;
    static int list1NowIndex;
    static int list2NowIndex;
    static String[] from1to2;
    static String[] from2to1;
    static int num1, num2;



    public static void init(Activity t, int currentRouteId, String gpsStationName) {
        Context ctx = t.getApplicationContext();
		Settings = ctx.getSharedPreferences(PREFS_NAME, 0);
		CurrentActivity = t;
        Utils.CurrentActivity = t;
        Intent intent = t.getIntent();
        Database db = AcraStuff.newDb;
        Globals.Db = db;
        //ACRA.getErrorReporter().putCustomData("Globals.Db", Globals.Db.toString());
        //AcraStuff.writeErrorMessage("Calling makeServiceIdMap, Globals.Db = " + Globals.Db.toString());
        Globals.Db.makeServiceIdMap();     //null pointer crash here
        //AcraStuff.writeErrorMessage("Back from makeServiceIdMap");


        Boolean b = initTrainCalendar();
        if (b == false) return;
		// Create spinner for first station
		Spinner spinner1 = (Spinner) t.findViewById(R.id.station1_spinner);
		ArrayAdapter<CharSequence> adapter1 = getAdapterForStation(t, spinner1);
		Adapter1 = adapter1;

        Spinner spinner2 = (Spinner) t.findViewById(R.id.station2_spinner);
        ArrayAdapter<CharSequence> adapter2 = getAdapterForStation(t, spinner2);
        String stationName2 = Settings.getString("stationName2",
                "Grand Central");
         Adapter2 = adapter2;
        // Create train list stuff
		initializeLists(t);

		// Get data
		StationMap = getAllStations();
		// fill spinner for first station
		String stationName1;
		if (gpsStationName.equals(""))
			stationName1 = Settings.getString("stationName1",
					"Grand Central");
		else
			stationName1 = gpsStationName;

        spinner1.setOnItemSelectedListener(new Station1OnItemSelectedListener());
        int pos = fillSpinner(adapter1, stationName1);
		spinner1.setSelection(pos, false);


        spinner2.setOnItemSelectedListener(new Station1OnItemSelectedListener());
        pos = fillSpinner(adapter2, stationName2);
		spinner2.setSelection(pos, false);

		displayVersionNumber(t);

	}

	private static void displayVersionNumber(Activity t){
		//Get version number
		TextView versionTextView = (TextView) t.findViewById(R.id.version_number);
		int versionCode = 0;
		try {
			PackageInfo pInfo = t.getPackageManager().getPackageInfo(t.getPackageName(), 0);
			versionCode = pInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		String versionText = "version " + versionCode;
		versionTextView.setText(versionText);
	}

	private static void initializeLists(Activity t) {
		MainListView1 = (ListView) t.findViewById(R.id.stations12_listview);
		MainListView2 = (ListView) t.findViewById(R.id.stations21_listview);
	}

	private static void getTripsUsingHttp(int origin, int destination){
		System.out.println("In getTripsUsingHttp");
		String urlParameters = "t=12/24/2012&c=1730&s=PM'&at=y&o=" + origin + "&d=" + destination;
		URL url = null;
		try {
			url = new URL("http://as0.mta.info/mnr/schedules/sched_results.cfm");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		URLConnection conn = null;
		try {
			conn = url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		conn.setDoOutput(true);

		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(conn.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			writer.write(urlParameters);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String line;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//need to parse html here
		try {
			while ((line = reader.readLine()) != null) {
			    System.out.println(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  		
	}
	
	private static void getTrips(ArrayAdapter<CharSequence> adapter1,
			ArrayAdapter<CharSequence> adapter2, Activity t) {
		if (adapter1.getCount() == 0 || adapter2.getCount() == 0){
            return;
        }
		Spinner spinner1 = (Spinner) t.findViewById(R.id.station1_spinner);
		String stationName1 = (String) spinner1.getSelectedItem();
		if (stationName1 == null)
			stationName1 = (String) adapter1.getItem(0);
		Spinner spinner2 = (Spinner) t.findViewById(R.id.station2_spinner);
		String stationName2 = (String) spinner2.getSelectedItem();
		if (stationName2 == null)
			stationName2 = (String) adapter2.getItem(0);
		int stationId1 = StationMap.get(stationName1);
		int stationId2 = StationMap.get(stationName2);
	    //Avoid doing database search again when returning from PickupMessage
        TextView tv1 = (TextView) t.findViewById(R.id.title12_textview);
        TextView tv2 = (TextView) t.findViewById(R.id.title21_textview);

        if (getTripsAgain == true){
		    trips = Globals.Db.getTripsInfo(stationId1, stationId2);
            list1NowIndex = -1;
            list2NowIndex = -1;
            String s;
            tv1.setText("");
            tv2.setText("");
            from1to2 = new String[trips.length];
            // This contains departure,arrival,tripid
            String[] departureAndArrival;
            num1 = 0;

            String lastDeparture = " ";
            int i;
            for (i = 0; i < trips.length; i++) {
                s = trips[i];
                departureAndArrival = s.split(",");
                int cmp = departureAndArrival[0].compareTo(departureAndArrival[1]);
                if (cmp < 0 && i <= trips.length - 1
                        && !lastDeparture.equals(departureAndArrival[0])) {
                    String stopHere = null;
                    if (departureAndArrival[0].indexOf("09:36") !=-1) {
                        stopHere = "true";
                    }
                    boolean b = doesTrainRunToday(departureAndArrival[2]);
                    if (b == false) {
                        continue;
                    }
                    lastDeparture = departureAndArrival[0];
                    // if departureAndArrival[0] > now and listNowIndex1 == -1,
                    // listNowIndex = num1;
                    b = isTrainLaterThanNow(departureAndArrival[0]);
                    if (b == true && list1NowIndex == -1)
                        list1NowIndex = num1;
                    String time1 = convertToTwelveHour(departureAndArrival[1]);
                    String time0 = convertToTwelveHour(departureAndArrival[0]);
                    from1to2[num1++] = time0 + "     " + time1;
                } else
                    continue;
            }

            lastDeparture = " ";
            from2to1 = new String[trips.length];
            num2 = 0;
            //Fix trip order in reverse trip -- need to sort by second field
            Arrays.sort(trips, new SecondFieldComparator());
            for (i = 0; i < trips.length; i++) {
                s = trips[i];
                departureAndArrival = s.split(",");

                int cmp = departureAndArrival[0].compareTo(departureAndArrival[1]);
                if (cmp >= 0 && i <= trips.length
                        && !lastDeparture.equals(departureAndArrival[1])) {
                    if (!doesTrainRunToday(departureAndArrival[2]))
                        continue;
                    lastDeparture = departureAndArrival[1];
                    boolean b = isTrainLaterThanNow(departureAndArrival[1]);
                    if (b == true && list2NowIndex == -1)
                        list2NowIndex = num2;

                    String time1 = convertToTwelveHour(departureAndArrival[1]);
                    String time0 = convertToTwelveHour(departureAndArrival[0]);
                    // from2to1[num2] = departureAndArrival[1] + "     " +
                    // departureAndArrival[0];
                    from2to1[num2++] = time1 + "     " + time0;
                } else
                    continue;
            }

        }
        else {
            getTripsAgain = true;
        }
		// TODO AND instead of OR?
		if (trips.length == 0 || from1to2[0] == null || from2to1[0] == null) {
			emptyTrainLists(t, tv1, tv2, MainListView1, MainListView2);
			MainListView1.setVisibility(View.INVISIBLE);
			MainListView2.setVisibility(View.INVISIBLE);
			return;
		}
		else {
			MainListView1.setVisibility(View.VISIBLE);
			MainListView2.setVisibility(View.VISIBLE);
		}

		writeTrains(stationName1, stationName2, from1to2, t, tv1,
				MainListView1, num1);
		MainListView1
				.setOnItemClickListener(new TrainList1OnItemClickListener());
		MainListView1.setSelection(list1NowIndex);

		writeTrains(stationName2, stationName1, from2to1, t, tv2,
				MainListView2, num2);
		MainListView2
				.setOnItemClickListener(new TrainList2OnItemClickListener());
		MainListView2.setSelection(list2NowIndex);

		TextView pkpPrompt = (TextView) t.findViewById(R.id.pickup_prompt);
		pkpPrompt.setText("Click on a train to send a pickup message.");

		// Debug.stopMethodTracing();

	}

	// trainString contains departure followed by arrival. We are comparing only
	// departure
	private static boolean isTrainLaterThanNow(String trainString) {
		Date now = new Date();
		int nowHours = now.getHours();
		int nowMin = now.getMinutes();
		int trainHours = Integer.parseInt(trainString.substring(0, 2));
		int trainMin = Integer.parseInt(trainString.substring(3, 5));
		int nowTime = 100 * nowHours + nowMin;
		int trainTime = 100 * trainHours + trainMin;
		boolean b = false;
		if (trainTime >= nowTime) {
			b = true;
			;
		}
		return b;
	}

	private static void emptyTrainLists(Activity t, TextView tv1, TextView tv2,
			ListView mainListView1, ListView mainListView2) {
		tv1
				.setText("There are no more trains between these two stations today.");
		tv2.setText("");

		ArrayList<String> trainsList1 = new ArrayList<String>(0);

		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(t,
				R.layout.listitem, trainsList1);
		mainListView1.setAdapter(listAdapter);
		listAdapter.clear();
		mainListView2.setAdapter(listAdapter);
		listAdapter.clear();

	}

	/*
	 * BUG Trains past midnight are replaced with nulls, and the app crashes
	 * Immediate fix: replace null with blank // done Correct fix: allow trains
	 * to wrap -- there may be a problem with "today"
	 */

	private static void writeTrains(String stationName1, String stationName2,
			String[] trains, Activity t, TextView tv, ListView mainListView,
			int numTrips) {
		String text = stationName1 + " to " + stationName2 + "\n";
		tv.setText(text);

		ArrayList<String> trainsList1 = new ArrayList<String>(numTrips);

		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(t,
				R.layout.listitem, trainsList1);
		mainListView.setAdapter(listAdapter);

		for (int i = 0; i < numTrips; i++) {
			if (trains[i] == null)
				trains[i] = " ";
			trainsList1.add(trains[i]);
		}

	}

	/*
	 * Key is service_id Value is the days that trains with that service id run.
	 * 0 means trains with that service id don't run on that day Sat=0, Sun=1,
	 * ..., Fri=6
	 */

	// Day numbers have been translated into the numbers used by the Calendar
	// class
	private static Boolean initTrainCalendar() {
		int i=0;
		//new Alert("In initTrainCalendar, Globals.Db = " + Globals.Db, false);
		while (Globals.Db == null){
			i++;
			System.out.println("In initTrainCalendar, Globals.Db = null");
			if (i==10) {
				Toast.makeText(CurrentActivity, "In initTrainCalendar, database object is null",
						Toast.LENGTH_SHORT).show();
				return false;				
			}
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e){
				Toast.makeText(CurrentActivity, "In initTrainCalendar, sleep was interrupted.",
						Toast.LENGTH_SHORT).show();
				return false;
	
			}
		}
		TrainCalendar = Globals.Db.getTrainCalendar();
		return true;
	}
/*
	private boolean compareTodayToDaysOfWeekTrainRuns(String service_id) {
		// shameless hack
		// if (service_id.equals("2477") || service_id.equals("2476") ||
		// service_id.equals("2473")) return true;
		if (service_id.startsWith("2") && service_id.length() == 4)
			return true;
		Calendar cal = Calendar.getInstance();
		int idayOfWeek = cal.get(cal.DAY_OF_WEEK);
		if (idayOfWeek == 7)
			idayOfWeek = 0;

		String dayList = TrainCalendar.get(service_id);
		String strDayOfWeek = ((Integer) idayOfWeek).toString();
		int index = dayList.indexOf(strDayOfWeek);
		if (index == -1)
			return false;
		else
			return true;

	}
*/
	
	private static boolean compareTodayToDaysOfWeekTrainRuns(String service_id) {
		// shameless hack
		// if (service_id.equals("2477") || service_id.equals("2476") ||
		// service_id.equals("2473")) return true;
		if (service_id.startsWith("2") && service_id.length() == 4)
			return true;
		Calendar cal = Calendar.getInstance();
		int idayOfWeek = cal.get(cal.DAY_OF_WEEK);
	
		//TODO:  Problem here
		//Calendar.DAY_OF_WEEK returns Calendar.SUNDAY .. Calendar.SATURDAY
		//In Utils.java, makeCalendarString should output a string such as "monday,tuesday,wednesday" to be compared with 
		//DAY_OF_WEEK
		//monday - 2, tuesday - 3, wed - 4, thu - 5, fri - 6, sat - 7 sun - 8
	      String dayString;
        switch (idayOfWeek) {
            case Calendar.SUNDAY:  dayString = "sunday";
                     break;
            case Calendar.MONDAY:  dayString = "monday";
                     break;
            case Calendar.TUESDAY:  dayString = "tuesday";
                     break;
            case Calendar.WEDNESDAY:  dayString = "wednesday";
                     break;
            case Calendar.THURSDAY:  dayString = "thursday";
                     break;
            case Calendar.FRIDAY:  dayString = "friday";
                     break;
            case Calendar.SATURDAY:  dayString = "saturday";
                     break;
            default: dayString = "sunday";
                     break;
        }

		String dayList = TrainCalendar.get(service_id);
		//String strDayOfWeek = ((Integer) idayOfWeek).toString();
		int index = dayList.indexOf(dayString);
		if (index == -1)
			return false;
		else
			return true;

	}
	

	private static boolean doesTrainRunToday(String tripId) {
		int intTripId = (new Integer(tripId)).intValue();

		String service_id = Globals.Db.getServiceIdForTrip(intTripId);
		if (service_id == null)
			return false;
		boolean b = compareTodayToDaysOfWeekTrainRuns(service_id);
		// if b == false return
		// See if service is in holiday table
		// If it is, and exception type == 2, eliminate it
		//if (b == false)
		//	return b;
		String exceptionType = Globals.Db.getExceptionTypeFromHolidayTable(
				intTripId, service_id);
		if (tripId.equals("5063781")) System.out.println("Return from getExceptionTypeFromHolidayTable = " + exceptionType);
		//Not in holiday table
		if (exceptionType.equals("notfound")) {
			if (tripId.equals("5063781")) System.out.println("Returning b, exceptionType = " + exceptionType);

			return b;
		}
		else if (exceptionType.equals("remove")){
				return false;
		}

		else {
			if (tripId.equals("5063781")) System.out.println("Returning true, exceptionType = " + exceptionType);

			return true;
		}

	}

	// Compare 2 strings in HH:MM:SS format
	// Return "before" if time1 < time2
	// Return "after" if time1 > time2
	private String compareTimes(String t1, String t2) {
		/*
		 * t1 = t1.trim(); String[] t1Split = t1.split(":"); long hours1 = (new
		 * Integer(t1Split[0])).intValue(); long min1 = (new
		 * Integer(t1Split[1])).intValue(); long sec1 = (new
		 * Integer(t1Split[2])).intValue(); long time1 = hours1*3600 + min1*60 +
		 * sec1;
		 * 
		 * t2 = t2.trim(); String[] t2Split = t2.split(":"); long hours2 = (new
		 * Integer(t2Split[0])).intValue(); long min2 = (new
		 * Integer(t2Split[1])).intValue(); long sec2 = (new
		 * Integer(t2Split[2])).intValue(); long time2 = hours2*3600 + min2*60 +
		 * sec2;
		 * 
		 * if (time1 < time2) return "before"; else return "after";
		 */
		t1 = t1.trim();
		t2 = t2.trim();
		int cmp = t1.compareTo(t2);
		if (cmp < 0)
			return "before";
		else
			return "after";

	}

	private static String convertToTwelveHour(String timeString) {
		timeString = timeString.trim();
		timeString = timeString.substring(0, 5);
		String[] timeStringSplit = timeString.split(":");
		long hours = (new Integer(timeStringSplit[0])).intValue();
		String suffix;
		if (hours >= 24) {
			suffix = " AM";
			hours -= 24;
			String newHours = ((Long) hours).toString();
			newHours = "0" + newHours;
			if (newHours.equals("00"))
				newHours = "12";
			timeString = newHours + timeString.substring(2, 5);
		} else if (hours > 12) {
			suffix = " PM";
			hours -= 12;
			String newHours = ((Long) hours).toString();
			if (hours <= 9)
				newHours = "  " + newHours;
			timeString = newHours + timeString.substring(2, 5);
		} else if (hours == 12) {
			suffix = " PM";
		} else {
			suffix = " AM";

		}
		timeString += suffix;
		return timeString;
	}

	/*
	 * Returns position of stationName
	 */
	private static int fillSpinner(ArrayAdapter<CharSequence> adapter,
			String stationName) {
		// This sorts the keys
		TreeSet<String> keys = new TreeSet<String>(StationMap.keySet());
		Iterator<String> k = keys.iterator();
		int index = 0;
		int pos = -1;
		while (k.hasNext()) {
			String key = (String) k.next();
			adapter.add(key);
			if (key.equals(stationName)) {
				pos = index;

			}
			index += 1;
		}
		return pos;

	}

	private static Map<String, Integer> getAllStations() {
		Map<String, Integer> stations = Globals.Db.getStationsMap();
		return stations;
	}

	private static ArrayAdapter<CharSequence> getAdapterForStation(Activity t,
			Spinner spinner) {
		ArrayList<CharSequence> stationsArrayList = new ArrayList<CharSequence>();
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(t,
				android.R.layout.simple_spinner_item, stationsArrayList);
		adapter.setDropDownViewResource(R.layout.my_spinner_item);
		spinner.setAdapter(adapter);
		return adapter;
	}

	public static class Station1OnItemSelectedListener implements
			OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			String stationName = parent.getItemAtPosition(pos).toString();
			// Save station name as default
			SharedPreferences.Editor editor = Settings.edit();
			int spinnerId = parent.getId();
			String stationPreference;
			if (R.id.station1_spinner == spinnerId) {
				stationPreference = "stationName1";
			} else
				stationPreference = "stationName2";
			editor.putString(stationPreference, stationName);
			editor.commit();

			/*
			 * Toast.makeText(parent.getContext(), "The id of the station is: "
			 * + id, Toast.LENGTH_LONG).show();
			 */
			ArrayAdapter<CharSequence> adapter1 = Adapter1;
			ArrayAdapter<CharSequence> adapter2 = Adapter2;
			getTrips(adapter1, adapter2, CurrentActivity);

        }

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public static void goToPickupMessageScreen(AdapterView<?> parent, int pos,
			String arrivalStation) {
		String trainTimesString = parent.getItemAtPosition(pos).toString();
		if (trainTimesString == null) {
			Toast.makeText(CurrentActivity, "Train times string is empty.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		String[] trainTimes = trainTimesString.split("     ");
		if (trainTimes.length != 2)
			return;
		String arrivalTime = trainTimes[1];
		// Will send parameters later
		Intent intent = new Intent();
		intent.setClassName("com.oryx.allaboard",
				"com.oryx.allaboard.PickupMessage");
		//key-value pair, where key needs current package prefix
		intent.putExtra("com.oryx.allaboard.ArrivalStation", arrivalStation); 
		intent.putExtra("com.oryx.allaboard.ArrivalTime", arrivalTime);
        getTripsAgain = false;
		CurrentActivity.startActivity(intent);

	}

	public static class TrainList1OnItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int pos,
				long id) {
			Spinner spinner2 = (Spinner) CurrentActivity
					.findViewById(R.id.station2_spinner);
			String arrivalStation = spinner2.getSelectedItem().toString();
			goToPickupMessageScreen(parent, pos, arrivalStation);
		}

		public void onNothingClicked(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public static class TrainList2OnItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> parent, View view, int pos,
				long id) {
			Spinner spinner1 = (Spinner) CurrentActivity
					.findViewById(R.id.station1_spinner);
			String arrivalStation = spinner1.getSelectedItem().toString();
			goToPickupMessageScreen(parent, pos, arrivalStation);
		}

		public void onNothingClicked(AdapterView<?> parent) {
			// Do nothing.
		}
	}
	
	public static class SecondFieldComparator implements Comparator<String> {
		  public int compare(String o1, String o2) {
			String[] arrDep1 = o1.split(","); 
			String c1 = arrDep1[1];
			String[] arrDep2 = o2.split(",");
			String c2 = arrDep2[1];
			return c1.compareTo(c2);			
		  }
		}

}
