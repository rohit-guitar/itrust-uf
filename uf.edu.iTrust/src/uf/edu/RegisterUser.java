/*******************************************************************************
 * Copyright (c) 2012 Udayan Kumar.
 * All rights reserved. 
 * 
 * This file is part of iTrust application for android.
 * iTrust is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. 
 * 
 * iTrust is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with iTrust.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Udayan Kumar - initial API and implementation
 ******************************************************************************/
package uf.edu;

 

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterUser extends Activity {
	/** Called when the activity is first created. */
	private static String TAG = "iTrust";
	boolean trustResult = false;
    TextView tv =null;
    CheckBox cb = null;
	String fname,lname,email,privacy,profile,macaddress;
     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        Button b = null;
        SharedPreferences pres;
    	pres = this.getSharedPreferences("iTrust", 0); 
    	
        //Set Mac Address
        tv =  (TextView)  findViewById(R.id.Reg_MacAddress);
        tv.setText(pres.getString("BLUEMAC", "UNKNOWN"));
      
        
        b = (Button)  findViewById(R.id.buttonRegistration);
        b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//check all fields are set and then send a msg to server and wait for response. 
				//if not successful initiate correction procedure.
				readData();
				if(verifyData()==true) {
					postData();
				} else {
					//Show error 
				}
				
			}
		});
    }
     public void readData() {
    	 tv =  (TextView)  findViewById(R.id.Reg_FName);
    	 fname = tv.getText().toString();
    	 tv =  (TextView)  findViewById(R.id.Reg_LName);
    	 lname = tv.getText().toString();
    	 tv =  (TextView)  findViewById(R.id.Reg_Email);
    	 email = tv.getText().toString();
    	 tv =  (TextView)  findViewById(R.id.Reg_ProfileLink);
    	 profile = tv.getText().toString();
    	 tv =  (TextView)  findViewById(R.id.Reg_MacAddress);
    	 macaddress = tv.getText().toString();
    	 cb = (CheckBox) findViewById(R.id.Reg_Privacy);
    	 privacy = cb.isChecked()?"0":"1";
     }
     
     public boolean verifyData() {
    	 //TODO: need to verify the inputs
    	 Log.i(TAG,"FNAME: "+fname);
    	 return true;
     }
     public void postData() {
    	 	//Data should be verified before calling this code. no verification done here.
    	    // Create a new HttpClient and Post Header
    	    HttpClient httpclient = new DefaultHttpClient();
    	    HttpPost httppost = new HttpPost("http://128.227.176.22:8182/cgi-bin/putData.py");
    	    try {
    	        // Add your data
    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    	        nameValuePairs.add(new BasicNameValuePair("fname", fname));
    	        nameValuePairs.add(new BasicNameValuePair("lname", lname));
    	        nameValuePairs.add(new BasicNameValuePair("email", email));
    	        nameValuePairs.add(new BasicNameValuePair("privacy", privacy));
    	        nameValuePairs.add(new BasicNameValuePair("profile", profile));
    	        nameValuePairs.add(new BasicNameValuePair("mac", macaddress));

    	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

    	        // Execute HTTP Post Request
    	        ResponseHandler<String> responseHandler=new BasicResponseHandler();
    	        String responseBody = httpclient.execute(httppost,responseHandler).replace('\n', ' ').trim();
    	        //JSONObject response=new JSONObject(responseBody);
    	        Log.i(TAG,"Registration Response: "+ responseBody);
    	        if (responseBody.compareToIgnoreCase("success")==0) {
    	        	Toast.makeText(this, "Registration Successful", Toast.LENGTH_LONG).show();
    	        } else if(responseBody.compareToIgnoreCase("Error: duplicate record")==0) {
    	        	Toast.makeText(this, "Already Registered", Toast.LENGTH_LONG).show();
    	        } else if(responseBody.substring(0, 25>responseBody.length()?responseBody.length():25).compareToIgnoreCase("Note: changing owner from")==0) {
    	        	Toast.makeText(this, "Moved already registered device to your account", Toast.LENGTH_LONG).show();
    	        } else {
    	        	Toast.makeText(this, responseBody, Toast.LENGTH_LONG).show();
    	        }

    	        	
    	    } catch (ClientProtocolException e) {
    	        // TODO Auto-generated catch block
    	    } catch (IOException e) {
    	        // TODO Auto-generated catch block
			}
    	}    
}   
  



