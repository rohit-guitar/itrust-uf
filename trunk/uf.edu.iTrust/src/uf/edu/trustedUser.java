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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class trustedUser {
	public static String  filename = "TrustedUser"; /// save trusted users in this file.
	public static String TAG = "iTrust";
	public static ArrayList<String> mac = new ArrayList<String>(20);
	public static ArrayList<Integer> trustValue = new ArrayList<Integer>(20);
	public static String sep=",";
	
	
	public static void appendMacToTrust (Context context, String Mac, int Value) {
		File file = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.DataPath)+"/" + filename);
		try {
			mac.add(Mac);
			trustValue.add(Value);
			BufferedWriter output = new BufferedWriter(new FileWriter(file,true));
			output.write(Mac + sep + Value + "\n");
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void readMacforTrust(Context context) {
		String line;
		StringTokenizer tok;
		File file = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.DataPath)+"/" + filename);
		try {
			BufferedReader read = new BufferedReader(new FileReader(file));
			while((line=read.readLine())!=null) {
				tok = new StringTokenizer(line,sep);
				String stmac = tok.nextToken();
				if(mac.contains(stmac) == false) //add mac address only if it is not present.
					mac.add(stmac);
				if(tok.hasMoreTokens()) {					
					trustValue.add(Integer.parseInt(tok.nextToken()));
				} else {
					//Trusted user file has incorrect values... delete file
					file.delete();
				}
				Log.d(TAG,"Read this MAC for trust : "+ line);
			}
			
			read.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void removeMacfromTrust(Context context, String Mac) {
		Log.i(TAG,"trustedUser:removeMacfromTrust, Removing mac from trusted list" + Mac);
		int index = mac.indexOf((String) Mac);
		//mac.remove(Mac);
		if(index != -1) {
			trustValue.remove(index);
			mac.remove(index);
		} else {
			return; //object not in list.
		}
		File file = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.DataPath)+"/" + filename);
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(file));
			for(int i=0;i<mac.size();i++){
				output.write(mac.get(i)+sep+trustValue.get(i)+"\n");
			}	
			output.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
