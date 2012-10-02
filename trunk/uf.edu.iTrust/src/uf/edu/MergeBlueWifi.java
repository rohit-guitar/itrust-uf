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

import java.io.*;
import java.util.*;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class MergeBlueWifi {
	public static int mergeBlueWifi(Context con, String bluein, String wifiin, String fileout, int timeGapBlueWifi) throws IOException {
	BufferedReader blueIn = null, wifiIn = null;
	String blueData,wifiData;
	StringTokenizer blueTok, wifiTok;
	int blueT, wifiT;
	String blueN = null,wifiL = null,blueM = null;
	Writer output = null;
	wifiL = null;
	String TAG="iTrust";
	
	boolean neof = false;
	try {
		
		File fb = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ bluein);
		File fw = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ wifiin);
		File fo = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ fileout);

		blueIn = new BufferedReader(new FileReader(fb));
		wifiIn = new BufferedReader(new FileReader(fw));
		output = new BufferedWriter(new FileWriter(fo));
	}
	catch (Exception e) {
		Log.i(TAG, "MergeBlueWifi: Error in mergeBlueWifi opening the files "+e);
		return -1;
	}
	
	if((blueData = blueIn.readLine())==null || (wifiData = wifiIn.readLine())==null ) {
		return -1;	
	}
	blueTok = new StringTokenizer(blueData, ";");
	blueT = Integer.parseInt (blueTok.nextToken());
	blueM = blueTok.nextToken();
	blueN = null;
	if(blueTok.hasMoreTokens() == true) 
		blueN = blueTok.nextToken();
	wifiTok = new StringTokenizer(wifiData, ";");
	wifiT = Integer.parseInt (wifiTok.nextToken());
	wifiL = wifiTok.nextToken();
	while(true) {
		neof = true;
		while(true) {  //Match the blue and Wifi traces. 
			if ((blueT - wifiT) > timeGapBlueWifi) {
				 if((wifiData = wifiIn.readLine())!=null) {
					 wifiTok = new StringTokenizer(wifiData, ";");
					 wifiT = Integer.parseInt (wifiTok.nextToken());
					 wifiL = wifiTok.nextToken();
				 } else {  //EOF
					neof = false;	
					break;
				 }				 
			} else if ((wifiT - blueT) > timeGapBlueWifi) {
				if((blueData = blueIn.readLine())!=null) {
					blueTok = new StringTokenizer(blueData, ";");
					try{
					blueT = Integer.parseInt (blueTok.nextToken());
					}
					catch(Exception e) {
						Log.i(TAG, e.getMessage());
						blueT = 0;
						continue;
					}
					blueM = blueTok.nextToken();
					blueN = "";
					while (blueTok.hasMoreTokens() == true) 
						blueN = blueN +" " +blueTok.nextToken();
					blueN = blueN.trim();
				} else { //EOF
					neof = false;
					break;
				}
			} else {
				break ;
			}
		}
		//System.out.println(blueT +" " + wifiT);
		if(neof == false)
			break;
		output.write(blueT + ";" + blueM + ";" + wifiL + ";" + blueN + "\n" );
		int prevT = blueT;
		neof = false;
		while((blueData = blueIn.readLine()) != null) {
			neof = true;
			blueTok = new StringTokenizer(blueData, ";");
			try {
			blueT = Integer.parseInt (blueTok.nextToken());
			}
			catch (Exception e) {
				Log.e(TAG, e.getMessage());
				continue;
			}
			try {
			blueM = blueTok.nextToken();
			}
			catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			blueN = "";
			while (blueTok.hasMoreTokens() == true) 
				blueN = blueN +" "+blueTok.nextToken();
			blueN = blueN.trim();
			if (blueT != prevT)
				break;	
			output.write(blueT + ";" + blueM + ";" + wifiL + ";" + blueN + "\n" );
			//output.flush();
			}
			if(neof == false)
				break;
		}
		blueIn.close();
		wifiIn.close();
		output.close();
		return 0;
	}
}	
