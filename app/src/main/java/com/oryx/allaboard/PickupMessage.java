package com.oryx.allaboard;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/*
This is now obsolete (8/22/2019)
 */
public class PickupMessage extends Activity {
	 String msg1 = "My train will be arriving at ";
	 String msg2 = " at the ";
	 String msg3 = " station.  Can you pick me up?";
	 Activity CurrentActivity;
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        CurrentActivity = this;
	        setContentView(R.layout.pickupmessage);
	        Intent intent = getIntent();
	        String arrivalTime = intent.getStringExtra("com.oryx.allaboard.ArrivalTime");
	        if (arrivalTime.startsWith(" ")) arrivalTime = arrivalTime.substring(1);
	        String arrivalStation = intent.getStringExtra("com.oryx.allaboard.ArrivalStation");
	        EditText message = (EditText)findViewById(R.id.pickup_message);
	        //message.setText("Station = " + arrivalStation + ", " + "Time =" + arrivalTime);
	        String msgText = msg1 + arrivalTime + msg2 + arrivalStation + msg3;
	        message.setText(msgText);
	        Button returnButton = (Button)findViewById(R.id.finish);
	        returnButton.setOnClickListener(finishListener);
	        Button smsButton = (Button)findViewById(R.id.send_message);
	        smsButton.setOnClickListener(sendMessageListener);
	  
	    }
	 
	 private OnClickListener finishListener = new OnClickListener() {
		    public void onClick(View v) {
		      // do something when the button is clicked
				onBackPressed();
		    }
    	};
    	
	 private OnClickListener sendMessageListener = new OnClickListener() {
		    public void onClick(View v) {
		      EditText phoneWidget = (EditText)findViewById(R.id.phone_number);
		      String phoneNumber = phoneWidget.getText().toString();
		      if (phoneNumber == null || phoneNumber.equals("")) {
		    	  Toast.makeText( CurrentActivity, "Please enter phone number.", Toast.LENGTH_SHORT).show();
		    	  return;
		      }
		      EditText messageWidget = (EditText)findViewById(R.id.pickup_message);
		      Editable editMessage = messageWidget.getText();
		      String message = editMessage.toString();
		      if (message == null || message.equals("")) {
		    	  Toast.makeText( CurrentActivity, "Please enter message.", Toast.LENGTH_SHORT).show();
		    	  return;
		      }
		      //SmsManager sm = SmsManager.getDefault();
			  //sm.sendTextMessage(phoneNumber, null, message, null, null);
			  sendTextMessage(message, phoneNumber);
		    	
		    }
	};


	private void sendTextMessage(String message, String phoneNumber){
		/*
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"
				+ phoneNumber)));
		*/
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
		intent.putExtra("sms_body", message);
		startActivity(intent);
	}

    @Override
    public void onStop() {
        super.onStop();
        //Stations.getTripsAgain = false;

    }
}


