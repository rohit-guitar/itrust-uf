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
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ClickData {

		Context con;
		FileOutputStream log=null;
		String TAG = "iTrust";
		
		public ClickData(Context con) {
			this.con = con;
			File filew = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+con.getString(R.string.ClickDataLog));
			try {
				log = new FileOutputStream(filew,true);
			} catch (Exception e)
			{
					Log.e(TAG,"ClickData:ClickData, exception opening file"+ e.getMessage());
			}
		}	
		public void write(String msg) {
			try {
			log.write((System.currentTimeMillis()/1000 + ":" + msg + "\n").getBytes());
			log.flush();
			}
			catch(Exception e) {
				Log.e(TAG,"ClickData:write, exception writing to file"+ e.getMessage());
			}
			
		}
		
		public void closeFile() {
			if(log == null)
				return;
			try {
				log.close();
			} catch (IOException e) {
				Log.e(TAG,"ClickData:closeFile, exception writing to file"+e.getMessage());
			}
		}
		
}
