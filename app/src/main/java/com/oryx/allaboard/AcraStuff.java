package com.oryx.allaboard;

import android.app.Application;

//import org.acra.*;
//import org.acra.annotation.*;

import java.util.Date;

/*
@ReportsCrashes(formUri = "",
        mailTo = "susancrayne@yahoo.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.ACRACrashReport
)
*/


/**
 * Created with IntelliJ IDEA.
 * User: susancrayne
 * Date: 7/11/13
 * Time: 1:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class AcraStuff extends Application {
    public static Database newDb;
    public static java.io.PrintWriter errorStream;
    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        //ACRA.init(this);
    }

    public static void writeErrorMessage(String msg){
        if (1 == 1) return;
        String date = new Date().toString();
        errorStream.print(date);
        errorStream.println(" " + msg);
        errorStream.flush();
    }

}
