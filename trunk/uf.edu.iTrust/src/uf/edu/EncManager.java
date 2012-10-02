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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


public class EncManager implements Serializable {
	TreeMap<String, EncUser> encUser;
	String encFile;
	TreeMap<Integer, EncLocation> userMap;
	public TreeMap<String,EncUser> sorted_encUser = null;
	
	int userMapLastTime =0;
	int userMapLastLoc =0;
	int scanTimeInterval=0;
    Context con;
    String TAG = "iTrust";
    
	public EncManager (Context con, String filename, int scanTimeInterval) {
		encFile = filename;
		this.scanTimeInterval = scanTimeInterval;
		this.con=con;
		load();
	}	
	public void load() {
		try{
			File f = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ encFile);
			FileInputStream fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis,16384);
			ObjectInputStream ois = new ObjectInputStream(bis);	
			encUser = (TreeMap<String, EncUser>) ois.readObject();
			userMap = (TreeMap<Integer, EncLocation>) ois.readObject();
			userMapLastTime = (Integer) ois.readObject();
			userMapLastLoc =  (Integer) ois.readObject();
			ois.close();
			fis.close();
			loadTrust(); // load the trusted users from the file
		}
		catch (InvalidClassException e) {
			//when this exception happens remove all the data .. go back to the backup trace files and refresh score again !!!
			//1. move everything in scannedData files to ZIPfiles 
			//2. rename them scannedata files.
			//3. delete objects file..
			//4. show error message and ask user to refresh score again. :)
			Log.i(TAG,"Encmanager:load exception invalid class: deleting stuff " +e.getMessage());
			moveFilesToZIP();
			moveFilesToOriginal();
			removeObjectFile();
			encUser = new TreeMap<String, EncUser> ();
			userMap = new TreeMap<Integer, EncLocation>();
			Toast.makeText(con, "Please refresh scores. Data will be preserved but Trust list may be lost.", Toast.LENGTH_LONG).show();
			
		}
		catch (Exception e) {
			Log.i(TAG, "EncManager load exception: Probably running for the first time " + e +e.getMessage());
			encUser = new TreeMap<String, EncUser> ();
			userMap = new TreeMap<Integer, EncLocation>();
		}
		
		
	}
	public void save() {
		try{
			File f = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ encFile);
			FileOutputStream fos = new FileOutputStream(f);
			BufferedOutputStream bos = new BufferedOutputStream(fos,16384); 
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(encUser);
			oos.writeObject(userMap);
			oos.writeObject(userMapLastTime);
			oos.writeObject(userMapLastLoc);
			oos.close();
			fos.close();
		}
		catch (Exception e) {
			Log.i(TAG, "EncManager.save exception:" + e);
		}
		
	}
	
	public void loadTrust() {
		trustedUser.readMacforTrust(con);
		Log.i(TAG,"Loading Trusted Users");
		for (int i=0; i< trustedUser.mac.size();i++) {
			try {
				((EncUser) encUser.get(trustedUser.mac.get(i))).trusted = trustedUser.trustValue.get(i);
				Log.i(TAG,"EncManger,loadTrust: MAC and value" + trustedUser.mac.get(i) + " " +  trustedUser.trustValue.get(i) );
			}
			catch (Exception e) {
				Log.e(TAG, "error loading trusted users");
			}
		}
	}
	
	 //already processed files are to be send out 
    void moveFilesToZIP() {
    
		String[] file = new String[] {"scannedDataW","scannedDataB"};
		for (int j=0;j<2;j++) {
			File filei = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/" + file[j]);
			File fileo = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+"ZIP" + file[j]);
			FileChannel fos;
			FileChannel fis;
			try {
				fos = new FileOutputStream(fileo,true).getChannel();
				fis = new FileInputStream(filei).getChannel();
				fos.transferFrom(fis, fos.size(), fis.size());
				fis.close();
				fos.close();
				filei.delete();

			} catch (Exception e) {
				Log.i(TAG, "Exception thrown in back up file handler creation" + e);
			}
		}
		
    }
    
    //already processed files are converted back 
    void moveFilesToOriginal() {
    
		String[] file = new String[] {"scannedDataW","scannedDataB"};
		for (int j=0;j<2;j++) {
			File filei = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/" + "ZIP" + file[j]);
			File fileo = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ file[j]);
			FileChannel fos;
			FileChannel fis;
			try {
				fos = new FileOutputStream(fileo,true).getChannel();
				fis = new FileInputStream(filei).getChannel();
				fos.transferFrom(fis, fos.size(), fis.size());
				fis.close();
				fos.close();
				filei.delete();

			} catch (Exception e) {
				Log.i(TAG, "Exception thrown in back up file handler creation" + e);
			}
		}
		
    }
    
    void removeObjectFile() {
    	
    	File filei = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ encFile );
    	try {
    		filei.delete();
    		
    	} catch (Exception e) {
    		Log.i(TAG,"EncManager:removeObjectFile, exception "+ e.getMessage());
    	}
    }
    
	
	public void updateUserMap(String filename, int startTime) {
		//this piece excepts bluetooth file for updating the user's self profile
		BufferedReader dis = null;
		String thisLine;
		StringTokenizer st;
		String mac = null, name =null;
		int locId, time;
		EncUser encuser =null;
		try{
			File f = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ filename);
			dis = new BufferedReader(new FileReader(f));
		}	
		catch (Exception e) {
			Log.i(TAG, "EncManager.updateUserMap exception:" + e);
			return;
		}
		 try {
				while ((thisLine = dis.readLine()) != null) {
						//add to the encounter data
					 	st = new StringTokenizer(thisLine, ";");
					 	if(!st.hasMoreTokens())
					 		continue;
					 	time = 0;
					 	locId = 0;
					 	try {
					 		time = Integer.parseInt(st.nextToken());
					 		locId = Integer.parseInt(st.nextToken());
					 	} catch (Exception e) {
					 		continue;
					 	}
					 	//now add to userMap.
					 	if( userMapLastTime > time ) //data already in 
					 	{	
					 		Log.i(TAG,"encmanager. Something is wrong in input data. TimeStamp is older that records already in");
							continue;	
					 	}	
					 	EncLocation encloc = null;
					 	encloc = userMap.get(locId);
					 	if(encloc == null) { //locId not in records
					 		encloc = new EncLocation(locId);
					 	} else {
					 		userMap.remove(locId);
					 	}
						if((time-userMapLastTime) <=scanTimeInterval && userMapLastLoc == locId) {
							encloc.addDuration(time-userMapLastTime);
						} else {
							encloc.addCount(1);
							encloc.addDuration(scanTimeInterval);	
						}
						userMapLastTime = time;
						userMapLastLoc = locId;
				 		userMap.put(locId, encloc);
				 }
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	 	
			try {
				dis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void updateEncounters (String filename, String filenameW, int startTime) {
		//needs to update userMap and Encounter data. 
		BufferedReader dis = null;
		String thisLine;
		StringTokenizer st;
		String mac = null, name =null;
		int locId, time;
		EncUser encuser =null;
		updateUserMap(filenameW, startTime);
		try{
			File f = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/"+ filename);
			dis = new BufferedReader(new FileReader(f));
		}	
		catch (Exception e) {
			Log.i(TAG, "EncManager.updateEncounter exception:" + e);
			return;
		}
		 try {
			while ((thisLine = dis.readLine()) != null) {
					//add to the encounter data
				 	st = new StringTokenizer(thisLine, ";");
				 	if(!st.hasMoreTokens())
				 		continue;
				 	time = Integer.parseInt(st.nextToken());
				 	mac  = st.nextToken();
				 	locId = Integer.parseInt(st.nextToken());
				 	if(st.hasMoreTokens())
				 		name = st.nextToken();
				 	encuser = encUser.get(mac);
				 	if(encuser == null) {
				 		encuser = new EncUser(mac, name);
				 		encUser.put(mac, encuser);
				 	} 
				 	encuser.addEncInfo(scanTimeInterval, locId, time,name);				
			 }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	 	
		
		try {
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		calScore();
		save();
		loadTrust();
	}
	public void calScore() {
		EncUser user = null;
		float sumCU = (float) 0.0, sumDU =(float) 0.0;
		for (Entry<Integer, EncLocation> entry : userMap.entrySet()) {
			
			sumCU += (float)((EncLocation)entry.getValue()).getCount()  * (float) ((EncLocation)entry.getValue()).getCount();
			sumDU += (float)((EncLocation)entry.getValue()).getDuration()*(float)((EncLocation)entry.getValue()).getDuration();

		}
		for (Entry<String, EncUser> entry : encUser.entrySet()){
			user = (EncUser)entry.getValue();
			user.calLvScore(userMap, sumCU, sumDU);
		}

	}
	public void calCombScore(int count,int dura, int lvC, int lvD) {
		//max FE and Max DE then call
		EncUser user = null;
		int maxFE =0, maxDE=0;
		//find out max FE and max DE
		for (Entry<String, EncUser> entry : encUser.entrySet()){
			user = (EncUser)entry.getValue();
			if(user.getScore(0)>maxFE) {
				maxFE = (int) user.getScore(0);
			}
			if(user.getScore(1)>maxDE) {
				maxDE = (int) user.getScore(1);
			}
		}
		for (Entry<String, EncUser> entry : encUser.entrySet()){
			user = (EncUser)entry.getValue();
			user.calCombScore(maxFE, maxDE, count, dura, lvC, lvD);
		}	
		///removing save to optimize when users change weights frequently.
		//save();
	}
		
		
	/* sorts the encUser according to the index and stores it in sorted_encUser */
	
	public void sort (int index) {
		
		ValueComparator bvc =  new ValueComparator(encUser,index);
        sorted_encUser = new TreeMap<String, EncUser>(bvc);
        sorted_encUser.putAll(encUser);
	}
	/*
	 * this prints from sorted_encUser
	 */
	public void printAllSorted() {
		EncUser user = null;
		for (Entry<String, EncUser> entry : sorted_encUser.entrySet()){
			user = (EncUser)entry.getValue();
			user.printAll();
		}
		Log.i(TAG, "EncManager Size of encUser: "+encUser.size());
		Log.i(TAG, "EncManager Size of UserMap: "+userMap.size());
	}
	
	public void printAll() {
		EncUser user = null;
		for (Entry<String, EncUser> entry : encUser.entrySet()){
			user = (EncUser)entry.getValue();
			user.printAll();
		}
		Log.i(TAG, "EncManager Size of encUser: "+encUser.size());
		Log.i(TAG, "EncManager Size of UserMap: "+userMap.size());
	}
	public void showEncByRank(int index, int topUser) {

	}
	public EncUser getEncUserDetail (String Mac) {
			return encUser.get(Mac);
	}

}	
