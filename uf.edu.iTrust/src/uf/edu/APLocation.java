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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import android.util.Log;

@SuppressWarnings("serial")
public class APLocation implements Serializable {
	
	TreeSet<String> AP;
	public int LocID;
	transient static String TAG = "iTrust";
	String address; // This field will be set after obtaining addresses for the AP from google or skyhook.
	public APLocation (ArrayList<String> APs, int LocationID) {
		AP = new TreeSet<String>();
		Iterator<String> iterator = APs.iterator();
		address= null;
		while(iterator.hasNext()) {
			AP.add((String) iterator.next());
		}
		LocID = LocationID;
	}
	public float match (ArrayList<String> APs ) {
		int c=0,m=0; 
		Iterator<String> iterator = APs.iterator();
		while(iterator.hasNext()) {
			c++;
			if (AP.contains((String) iterator.next()) == true) 
				m++;
		}
		return (float)m/((float)c + (float)AP.size() -(float)m);
		//return (float)m/(float)c; //initially used.
	}
	public void printAll() {
		String message=null;
		Iterator<String> iterator = AP.iterator();
		message = Integer.toString(LocID) + ":";
		while (iterator.hasNext())
		        message = message + iterator.next() + ", ";
		message = message + ": " + address;
		
		Log.i(TAG,"APLocation : " + message);
		
	}	
	
	public void printIDAddress() {
		Log.i(TAG,LocID+","+address);
	}
			
	//once address is set it can be retrieved from google/skyhook its to be set here.
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		if(address != null)
			this.address = address;
		else 
			this.address = null;
	}
	
	//this is needed to get the set of APs considered in this LocID. will be used when retrieving the addresses.
	public TreeSet<String> getAPSet() {
		return AP;
	}
}	
				
			

