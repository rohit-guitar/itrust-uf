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

import java.util.Iterator;

import java.util.TreeSet;
import java.io.*;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class AddLocation {
	
	static String TAG = "iTrust";
	static String url = "https://www.google.com/loc/json";

	public static String getLocation (TreeSet<String> APs) {
		Iterator<String> itr = APs.iterator();
		JSONArray wifiaps = new JSONArray();
		String out = null;

		//put mac address in the json array
		while(itr.hasNext()) {
			 JSONObject wifiap = new JSONObject();
			 try {
				wifiap.put("mac_address", (String)itr.next());
				//TODO: add signal strength
			} catch (JSONException e) {
				Log.i(TAG,"AddLocation: Exception" + e.getMessage());
				e.printStackTrace();
			}
		    wifiaps.put(wifiap);
		}
	
		//Adding rest of the data to create a complete Jason Package
		JSONObject obj = new JSONObject();
		try {
			obj.put("version","1.1.0");
			obj.put("request_address",true);
			obj.put("wifi_towers",wifiaps);
		} catch (JSONException e) {
			Log.i(TAG,"AddLocation: Exception" + e.getMessage());
 		}
		
		//Now ask the Google Server for address.
		JSONObject output=SendHttpPost(url,obj);
		if(output == null) {
			return null;
		}
		//Parse out the address.
		try {
			
			out= ((JSONObject)output.get("location")).get("latitude")+","+((JSONObject)output.get("location")).get("longitude");
		} catch (Exception e) {
			Log.i(TAG,"AddLocation: Exception parsing JSON " + e.getMessage());
		}
		try{	
			out = out +","+((JSONObject)output.get("location")).get("accuracy");
		} catch (Exception e) {
			Log.i(TAG,"AddLocation: Exception parsing JSON " + e.getMessage());
		}
		/*
		try{
			JSONObject address = ((JSONObject)((JSONObject)output.get("location")).get("address"));
			try{
				out = out +","+ address.get("street_number") + "," + address.get("street")+"," + address.get("postal_code");
			} catch (Exception e) {
				Log.i(TAG,"AddLocation: Exception parsing JSON " + e.getMessage());
			}  	
			try{
				out =out +","+ address.get("city") +","+ address.get("county")+","+ address.get("region")+","+ address.get("country")+","+address.get("country_code") +"\n";
			  	
			} catch (Exception e) {
				Log.i(TAG,"AddLocation: Exception parsing JSON " + e.getMessage());
			}
			Iterator<String> it = address.keys();
			while(it.hasNext()){
				Log.i(TAG,"AddLocation: Key iterator: " + it.next());
			}
		} catch (Exception e) {
			Log.i(TAG,"AddLocation: Exception parsing JSON " + e.getMessage());
		}  	
		*/
	    Log.i(TAG,"AddLocation: " + out);		
		
	
		
		return out;
	}
	
	
	
	public static JSONObject SendHttpPost(String URL, JSONObject jsonObjSend) {
		JSONObject obj = null;
		  try {
		   DefaultHttpClient httpclient = new DefaultHttpClient();
		   HttpPost httpPostRequest = new HttpPost(URL);

		   StringEntity se;
		   se = new StringEntity(jsonObjSend.toString());

		   // Set HTTP parameters
		   httpPostRequest.setEntity(se);
		   httpPostRequest.setHeader("Accept", "application/json");
		   httpPostRequest.setHeader("Content-type", "application/json");
		  
		   HttpResponse response = (HttpResponse) httpclient.execute(httpPostRequest);
		
		   // Get hold of the response entity (-> the data):
		   HttpEntity entity = response.getEntity();

		   if (entity != null) {
		    // Read the content stream
		    InputStream instream = entity.getContent();
			    // convert content stream to a String
		    String resultString= convertStreamToString(instream);
		    instream.close();
		    resultString = resultString.substring(1,resultString.length()-1); // remove wrapping "[" and "]"

		    String s="{"+resultString;//+"}";
		    Log.i(TAG,"Location String Received ");
		    obj=(JSONObject) new JSONTokener(s).nextValue();
		   } else {
			   Log.i(TAG,"AddLocation: error response from the location server :" + response.toString());
		   }

		  }
		  catch (Exception e)
		  {
		   e.printStackTrace();
		  }
		  return obj;//jsonObjRecv;
		 }

	
	
	
	
	
	

	private static String convertStreamToString(InputStream is) {
		  /*
		   * To convert the InputStream to String we use the BufferedReader.readLine()
		   * method. We iterate until the BufferedReader return null which means
		   * there's no more data to read. Each line will appended to a StringBuilder
		   * and returned as String.
		   * 
		   * (c) public domain: http://senior.ceng.metu.edu.tr/2009/praeda/2009/01/11/a-simple-restful-client-at-android/
		   */
		  BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		  StringBuilder sb = new StringBuilder();

		  String line = null;
		  try {
		   while ((line = reader.readLine()) != null) {
		    sb.append(line + "\n");
		   }
		  } catch (IOException e) {
		   e.printStackTrace();
		  } finally {
		   try {
		    is.close();
		   } catch (IOException e) {
//		    e.printStackTrace();
		   }
		  }
		  return sb.toString();
		 }


}

