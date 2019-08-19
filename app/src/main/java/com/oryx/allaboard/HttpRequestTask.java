package com.oryx.allaboard;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpRequestTask extends AsyncTask<String, String, String>{
	@Override
	protected String doInBackground(String... uri){
		//Do "AJAX" here
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;
		
		try {
			HttpUriRequest request = (HttpUriRequest) new HttpGet(uri[0]);
			response = httpclient.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    StatusLine statusLine = response.getStatusLine();
	    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        try {
				response.getEntity().writeTo(out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        String responseString = out.toString();
	        return responseString;
	        
	    	} else{
	        //Closes the connection.
	        try {
				response.getEntity().getContent().close();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				throw new IOException(statusLine.getReasonPhrase());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    return null;
	}
	
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
 
    }
	

}
