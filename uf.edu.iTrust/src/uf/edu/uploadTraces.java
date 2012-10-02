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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;



public class uploadTraces{
	
	Context con;
	SharedPreferences pres;

	uploadTraces(Context con){
		this.con = con;
	}
	
	boolean  sendFile(String Filename) {
		boolean success = false;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(con.getResources().getString((R.string.uploadURL)));
		

		FileBody bin = new FileBody(new File(Filename));
		try {
			
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("filename", bin);
			//reqEntity.addPart("comment", comment);
			httppost.setEntity(reqEntity);
		
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			Log.i("iTrust",resEntity.toString());
			success = true;
			
			//save the last upload time
		    pres = con.getSharedPreferences("iTrust", 0); 
			SharedPreferences.Editor ed = pres.edit();
			ed.putLong("LastUploadTime", System.currentTimeMillis()/1000);
			ed.commit();

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return success;

	}
	
}
