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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;


public class APLocParser {
	Context con;
	String DB;
	ArrayList<APLocation> APLoc;
	String TAG ="iTrust";
	/*DB is a filename. This file store all the AP to location ID objects.*/
	public APLocParser (Context con, String DB) {
		this.DB = DB;
		this.con = con;
		APLoc = new ArrayList<APLocation>();
		loadPrevious();
	}
	/* Save the existing AP set to Location ID data to a file*/
	void saveCurrent() {
		try{
			File f = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ DB);
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos,16384); 
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(APLoc);
			oos.close();
			fos.close();
		}
		catch (Exception e) {
			Log.i(TAG,"APLocPaser.saveCurrent exception:" + e);
		}
	}	
	/* Load the existing AP set to Location ID data from a file*/
	void loadPrevious() {
		try{
			File f = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ DB);
			FileInputStream fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis,16384);
			ObjectInputStream ois = new ObjectInputStream(bis);	
			ArrayList<APLocation> readObject = (ArrayList<APLocation>) ois.readObject();
			APLoc = readObject;
			ois.close();
			fis.close();
		}
		catch (Exception e) {
			Log.i(TAG,"APLocPaser.loadPrevious exception: Probably running for the first time " + e);
		}
	}	

	/*files would be comma separated format - <timestamp>, <mac>, <name>, <other csv data> */
	void parseNew(String filein, String fileout, int StartTime, int match_per)  { 
		BufferedReader dis = null;
		Writer output = null;
		ArrayList<String> apList = new ArrayList<String>();
		StringTokenizer st;
		String apMac=null;
		String thisLine;
		int prevTime = 0, curTime = 0;
		int locId =0;
		try{
			File fi = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ filein);
			File fo = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ fileout);

			dis = new BufferedReader(new FileReader(fi));
			output = new BufferedWriter(new FileWriter(fo));
		}	
		catch (Exception e) {
			Log.i(TAG,"APLocPaser.parseNew exception:" + e);
			return;
		}
		try{
			while ((thisLine = dis.readLine()) != null) {	
				st = new StringTokenizer(thisLine, ";");
				if(!st.hasMoreTokens())
					continue;
				try {
					curTime =  Integer.parseInt(st.nextToken());
				} 
				catch (Exception e) {
					Log.i(TAG,"APLocPaser.parseNew exception:" + e);
					continue;
				}
				if(curTime < StartTime)
					continue;
				try {
					apMac = st.nextToken();
				} 
				catch(Exception e){
					Log.i(TAG,"APLocPaser.parseNew Next Token exception:" + e);
				}
				if(apMac.compareTo("00:00:00:00:00:00") == 0)
					continue;
				if (prevTime == curTime || prevTime == 0) {	
					apList.add(apMac);
				} else {
					locId = searchList(apList, match_per);
					if(locId == 0) {
						locId = insertList(apList);
					}	
					apList = new ArrayList<String>();
					apList.add(apMac);
					output.write(Integer.toString(prevTime) + ";" + Integer.toString(locId)+"\n");
					//System.out.println(	Integer.toString(prevTime) + "," + Integer.toString(locId));

				}	
				prevTime = curTime;
			}
		}
		catch(Exception e) {
			Log.e(TAG,"APLocParser:parse new "+e.getMessage());
		}
		try {
			output.close();
			dis.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		saveCurrent();
	}

	int searchList(ArrayList<String> apList, int matchPer) {
		APLocation aploc;
		float result;
		Iterator<APLocation> itr = APLoc.iterator();
		while(itr.hasNext()) {
			aploc = (APLocation)itr.next();
			result = aploc.match(apList);
			if (((int) (result*100)) >= matchPer) {
				return aploc.LocID;
			}
		}
		return 0;
	}

	int insertList(ArrayList<String> apList) {
		//find out the last locid;
		int locId=0;
		if(APLoc.size() == 0) {
			locId = 0;
		} else {
			locId = ((APLocation)APLoc.get(APLoc.size()-1)).LocID;
		}
		APLocation aploc = new APLocation(apList, locId+1);
		APLoc.add(aploc);
		return (locId+1);
	}

	/*
	 * This function iterates over all the APLocations encountered till now and finds the GPS coordinates.
	 */
	void updateAddress() {
		APLocation aploc;
		Iterator<APLocation> itr = APLoc.iterator();
		while(itr.hasNext()) {
			aploc = (APLocation)itr.next();
			//check if address is not added then go and get it
			if(aploc.getAddress() == null) {
				aploc.setAddress(AddLocation.getLocation(aploc.AP));
			}
		}
		saveCurrent();
	}

	String getAddress(int id) {
		//TODO: to change APLoc into a Tree Structure to speed up lookups.
		APLocation aploc = (APLocation)APLoc.get(id-1);
		return aploc.getAddress();
	}

	/*
	 * This function is useful to see what is getting stored in the APLocation database.
	 */
	void printMapping() {
		APLocation aploc;
		Iterator<APLocation> itr = APLoc.iterator();
		while(itr.hasNext()) {
			aploc = (APLocation)itr.next();
			Log.i(TAG,"AP Loc ID "+ aploc.LocID);
			aploc.printAll();
		}	
	}

	/*
	 * This function is useful to print location ID and coordinated from google
	 */
	void printIDAddress() {
		APLocation aploc;
		Iterator<APLocation> itr =  APLoc.iterator();
		while(itr.hasNext()) {
			aploc = (APLocation)itr.next();
			aploc.printIDAddress();
		}	
	}

}	


