package com.oryx.allaboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import static com.oryx.allaboard.Globals.Db;

public class SplashScreen extends Activity {
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        Utils.CurrentActivity = this;
            setContentView(R.layout.splashscreen);
            View view = findViewById(R.id.SplashScreen);
            view.setVisibility(View.VISIBLE);
            view.bringToFront();

        try {
            new AsyncLoadDatabase().execute(this);

            //LoadTrainInfoDatabase(this);
            } catch (Exception e){
                StackTraceElement[] s = e.getStackTrace();
                String ss = s.toString();
                new Alert("Error in Database Loading = " + ss, false);
                System.exit(-1);

            }


    }

    private void OpenErrorStream(Context context){
        //File file = context.getDatabasePath("errorLog");
        //File file = context.getFileStreamPath("/allaboardErrorLog");
        File file = new File(((Context)this).getExternalFilesDir(null), "allaboardErrorLog");
        try {
            if (!file.exists())
                file.createNewFile();
        }
        catch (IOException e){
            System.out.println("Error creating error log");
            System.exit(-1);
        }

        String currentPath = file.getAbsolutePath();
        System.out.println("absolute path of error file is: " + currentPath);
        /*
        if (file.exists()) {
            file.delete();
        }
        */
        try {
            AcraStuff.errorStream = new PrintWriter(file.getPath());
            //write test message
            String testErrMsg = "test error message";
            AcraStuff.writeErrorMessage(testErrMsg);
        }
        catch (IOException e){
            System.out.println("Error opening error log: " + e.getMessage());
            System.exit(-1);
        }

    }

    private void LoadTrainInfoDatabase(Activity act){
        OpenErrorStream(act.getApplicationContext());
        AcraStuff.newDb = new Database();
        //Make a class with a public static member variable to save "db"
        boolean DbReturn = AcraStuff.newDb.checkIfDatabaseCopyNeeded(act.getApplicationContext(), null);
        System.out.println("In doInBackground, DbReturn, newDb = " +
                DbReturn + ", " + AcraStuff.newDb);
        if (DbReturn == true) {
            return;
        }
        else {
            new Alert("AllAboard could not be initialized.   Please check your phone or wifi connection.", true);
            System.exit(-1);
        }

    }
	
	  @Override
	    public void onStart() {
	        super.onStart();

	    }
      @Override
        public void onStop(){
          super.onStop();
      }

    private class AsyncLoadDatabase extends AsyncTask<Activity, Void, Void>{
        boolean DbReturn;
        Activity act;
        @Override
        protected void onPreExecute(){

        }

        @Override
        protected Void doInBackground(Activity... activities ){
            Database db = new Database();
            //Make a class with a public static member variable to save "db"
            act = activities[0];

            LoadTrainInfoDatabase(act);
            Globals.Db = db;
            System.out.println("In doInBackground, DbReturn, Globals.db = " +
                    DbReturn + ", " + Db);

            return null;
        }

        @Override
        protected void onPostExecute(Void params){

            // "act" is null here
            String DbNotNull;
            if (Globals.Db == null) DbNotNull = "false";
            else DbNotNull = "true";

            //next line for testing only
            Intent intent = new Intent();
            intent.setClassName("com.oryx.allaboard", "com.oryx.allaboard.MainApp");
            startActivity(intent);

        }



    }

}


