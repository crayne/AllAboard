package com.oryx.allaboard;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Calendar;


public class Utils {
	public static Activity CurrentActivity;
	static String dateToday = null;
	static String timeNow = null;
	public static String getTodayDate(){
		if (dateToday == null){
			Calendar cal = Calendar.getInstance();
			int iYear = cal.get(cal.YEAR);
			String year = ((Integer)(iYear)).toString();
			int iMonth = cal.get(cal.MONTH) + 1;
			//MONTH is zero-based
			String month = ((Integer)(iMonth)).toString();
			if (month.length() == 1) month = "0" + month;
			int iDayMonth = cal.get(cal.DAY_OF_MONTH);
			String dayOfMonth = ((Integer)(iDayMonth)).toString();
			dateToday = year + month + dayOfMonth;
	
		}
		return dateToday;
	}

    public static void sendNotification(String title, String text, Context ctx, int number){
        //Intent notificationIntent = new Intent(ctx, YourClass.class);
        //PendingIntent contentIntent = PendingIntent.getActivity(ctx,
        //       YOUR_PI_REQ_CODE, notificationIntent,
        //       PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager nm = (NotificationManager) ctx
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Resources res = ctx.getResources();
        Notification.Builder builder = new Notification.Builder(ctx);

        builder.setContentIntent(null)
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.icon))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text);
        Notification n = builder.build();

        nm.notify(number, n);

    }


    public static String getTimeNow() {
        String dateTime = Calendar.getInstance().getTime().toString();
        String timeOnly = dateTime.substring(11,19);
        timeNow = timeOnly;
		return timeNow;
	}
	
	
	public static String convertEarlyTimes(String time, int addAmount){
		if (!time.startsWith("0")) return time;
		int digit2 = new Integer(time.substring(1,2)).intValue();
		if (digit2 > 4) return time;
   		String firstTwo = time.substring(0,2);
		int firstTwoInt = new Integer(firstTwo).intValue();
		firstTwoInt += addAmount;
		firstTwo = ((Integer) firstTwoInt).toString();
		time = firstTwo + time.substring(2);
		return time;
	
	}


/*	
	public static String makeCalendarString(int monday, int tuesday, 
			int wednesday, int thursday, int friday,int saturday, int sunday){
		int sum = monday*1 + tuesday*2 + wednesday*4 + thursday*8 + friday*16 + saturday*32 + sunday*64;
		String out;
		if (sum == 1+2+4+8+16) out = "2,3,4,5,6";
		else if (sum == 32+64) out = "0,1";
		else if (sum == 16) out = "6";
		else if (sum == 32) out = "0";
		else if (sum == 64) out = "1";
		else if (sum == 64+1) out = "1,2";
		else out = "8";
		return out;
			
		
	}
*/	
	
	public static String makeCalendarString(String monday, String tuesday, 
			String wednesday, String thursday, String friday,  String saturday, String sunday){
		String out = "";
		if (monday.equals("1")) out += "monday,";
		if (tuesday.equals("1")) out += "tuesday,";
		if (wednesday.equals("1")) out += "wednesday,";
		if (thursday.equals("1")) out += "thursday,";
		if (friday.equals("1")) out += "friday,";
		if (saturday.equals("1")) out += "saturday,";
		if (sunday.equals("1")) out += "sunday,";

		return out;
			
		
	}


    public static byte[] serializeObject(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.close();

            // Get the bytes of the serialized object
            byte[] buf = bos.toByteArray();

            return buf;
        } catch(IOException ioe) {
            System.out.println("serializeObject" +  "error" +  ioe);

            return null;
        }
    }

    public static Object deserializeObject(byte[] b) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            Object object = in.readObject();
            in.close();

            return object;
        } catch(ClassNotFoundException cnfe) {
            System.out.println("deserializeObject" +  "class not found error" + cnfe);

            return null;
        } catch(IOException ioe) {
            System.out.println("deserializeObject" + "io error" + ioe);

            return null;
        }
    }







}
