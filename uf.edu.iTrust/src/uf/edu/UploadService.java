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
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;



public class UploadService extends Service {
	private static final String TAG = "iTrust: UploadService";
	SharedPreferences prefs;
	Context context;
	boolean uploadSuccess=false;
	private PowerManager pm = null;
	PowerManager.WakeLock wl = null;

	@Override
	public void onCreate() {		
		Toast.makeText(this,"iTrust: Starting upload", Toast.LENGTH_LONG).show();
		context = getApplicationContext();
    	prefs = this.getSharedPreferences("iTrust", 0); 	
    	 pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
         wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
         wl.acquire();
	}

	@Override
	public void onDestroy() {
		if(uploadSuccess) {
    		Toast.makeText(context, "iTrust: upload successful", Toast.LENGTH_LONG).show();
    	} else {
    		Toast.makeText(context, "iTrust: upload failed. Please check Internet connectivity and try again.", Toast.LENGTH_LONG).show();
    	}
		wl.release();
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		new DoUpload().execute();		
	}

	private class DoUpload extends AsyncTask<Object, Object, Object> {

		@Override
		protected Object doInBackground(Object... arg0) {
			String address = prefs.getString("BLUEMAC", "UNKNOWN");
			String filename = address.replace(':', '_')+"-"+System.currentTimeMillis()+".zip";    	        		
			zip z = new zip (context);
			z.zipFolder(filename);
			uploadTraces ut = new uploadTraces(context);
			uploadSuccess = ut.sendFile(Environment.getExternalStorageDirectory()+"/"+getString(R.string.DataPath)+"/"+filename); 
			File filei = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/" + filename);
			filei.delete();
	    	Log.i(TAG,"Upload successful="+uploadSuccess);
	    	stopSelf();
			return arg0;
		}
	}

	
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
