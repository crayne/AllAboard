package com.oryx.allaboard;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

import java.util.Map;

public class Routes {

	Map<String, Integer> RoutesMap;
	Database DatabaseObject;
	Activity AllAboardActivity;
	
    public Routes(Activity t, Database db){
    	DatabaseObject = db;
    	AllAboardActivity = t;
    	/*
 		t.setContentView(R.layout.main);
        Spinner spinner = (Spinner) t.findViewById(R.id.routes_spinner);
        ArrayList<CharSequence> routesArrayList = new ArrayList<CharSequence>();
        //ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(t, android.R.layout.simple_spinner_item, routesArrayList);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(t, android.R.layout.simple_spinner_item, routesArrayList);
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
         RoutesMap = getAllRoutes(db);
         //This sorts the keys
        TreeSet<String> keys = new TreeSet<String>(RoutesMap.keySet());
        int s = keys.size();
        Iterator<String> k = keys.iterator();
        while (k.hasNext()) {
          String key = (String) k.next();
          adapter.add(key);
        }
        //spinner.setOnItemSelectedListener(new RoutesOnItemSelectedListener());
        int currentRouteId = (int)adapter.getItemId(0);
      
        new Stations(AllAboardActivity, DatabaseObject, currentRouteId);
        */
    
	}
	
	public Map<String, Integer> getAllRoutes(Database db) {
		Map<String, Integer>routes = db.getRoutesMap();
		return routes;
	}
	
	public int getRouteIdForLongName(Database db, String name){
		Map<String, Integer> routes = getAllRoutes(db);
		int id = routes.get(name);
		return id;
	}
	
	
	public class RoutesOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	   
	      Toast.makeText(parent.getContext(), "The long name of the route is " +
	      parent.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
	      int currentRouteId = RoutesMap.get(parent.getItemAtPosition(pos).toString());
	      Stations st = new Stations();
          st.init(AllAboardActivity, currentRouteId, "");
	    }
	    
	    public void onNothingSelected(AdapterView<?> parent) {
	      // Do nothing.
	    }
	}

}


