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
import java.util.*;

import android.util.Log;

public class EncUser implements Serializable {
	float [] score = new float[5]; //0 = FE, 1 = DE, 2 = LV-C, 3 = LV-D, 4=Combined
	int [] rank = new int[5]; //corresponding to above order
	float [] decayScore = new float[5];//0 = FE, 1 = DE, 2 = LV-C, 3 = LV-D, 4=Combined
	ArrayList<Integer> timeSeries = new ArrayList<Integer>();
	String Mac;
	String Name;
	int lastEncounterTime;
	int lastEncounterApId;
	int trusted; /* range from -5 to 5. 0 means unknown.. -5 means untrusted */
	transient String TAG = "iTrust";
	transient int halfTime = 15552000;
	transient float tmp;
	TreeMap<Integer, EncLocation> locMap;
	
	void decay(float value, int index, int Time) {	
		tmp =  (float) Math.pow(.5d, (((double)Time-(double)lastEncounterTime))/(double)halfTime);
		//Log.i(TAG," Value of tmp;"+  tmp+"; Time ;"+Time+ "; lastEncounterTime;"+ lastEncounterTime);
		decayScore[index] = value + decayScore[index] * tmp;
		//Log.i(TAG,"decayScore: " + decayScore[index] + " index: "+index);
		return;
	}
	
	public EncUser (String Mac, String Name) {
		int i;
		for (i = 0; i<5 ;i++) {
			score[i] = 0.0F;
			decayScore[i] = 0.0F;
			rank[i] = 0;
		}
		this.Mac = Mac;
		this.Name = Name;
		this.lastEncounterApId = 0;
		this.lastEncounterTime = 0;
		this.trusted = 0;
		locMap = new TreeMap<Integer, EncLocation>();
	}
	
	public int addEncInfo ( int scanTimeInterval, int locId, int lastTime, String name) {
		EncLocation encLoc = null;
		int count=0, dura=0;
		if(!name.equalsIgnoreCase("null")) {
			Name = name;
		}
		if(lastEncounterTime > lastTime ) //data already in 
			return 0;
		if((lastTime-lastEncounterTime) <=scanTimeInterval && lastEncounterApId == locId) {
			dura = lastTime-lastEncounterTime;
			score[1] += dura;
			decay((float)dura,1,lastTime);
		} else {
			count = 1;
			dura = scanTimeInterval;
			score[0] += 1;
			score[1] += dura;
			decay(1.0F,0,lastTime);
			decay((float)dura,1,lastTime);
			//adds up time stamps
			timeSeries.add(lastTime);
		}
		lastEncounterApId = locId;
		lastEncounterTime = lastTime;
		//now add to the location data
		if ((encLoc = locMap.get(locId)) == null) {
			encLoc = new EncLocation(locId);
			locMap.put(locId,encLoc);
		}	
		encLoc.addDuration(dura);
		encLoc.addCount(count);
		return 1;
	}	
	
	public int calLvScore(TreeMap<Integer, EncLocation> userMap, float sumCU2, float sumDU2) {  //score is cal wrt userMap
		EncLocation u1 = null, u2=null;
		float sumCU1=0, sumDU1=0,prodC=0, prodD=0;
		Collection c = locMap.values();
		Iterator itr = c.iterator();
		while(itr.hasNext()) {
			u1 = (EncLocation) itr.next();
			//Log.i(TAG, "calLvScore for user :" + this.Name + "Location id "+ u1.locId + "duration and count " + u1.duration + u1.count );
			if((u2 = userMap.get(u1.getLocId()))==null) {
				Log.e(TAG, "EncUser Check the userMap.. it is missing values present in locMap.. impossible");
				return -1;
			}
			sumCU1 += (float)u1.getCount() * (float)u1.getCount(); 	
			sumDU1 += (float)u1.getDuration()* (float)u1.getDuration();
			prodC += (float)u1.getCount() * (float)u2.getCount();
			prodD += (float)u1.getDuration() * (float)u2.getDuration();
		}
		score[2] = (float) (prodC / (Math.sqrt(sumCU1*sumCU2)));
		score[3] = (float) (prodD / (Math.sqrt(sumDU1*sumDU2)));		
		return 0;
	}
	
	public int calCombScore(int MaxFE, int MaxDE, int count,int dura, int lvC,int lvD) { //need to normalize the score before combining them.
		score[4] = score[0]/MaxFE *count + score[1]/MaxDE *dura + score[2] *lvC + score[3] *lvD;
		score[4] = score[4]/(count+dura+lvC+lvD);
		return 0;	
	}
	
	public float getScore(int index) {
		if(index<5 && index >=0)
			return score[index];
		else 
			return 0;
	}
	
	public void printAll() {
		//Log.i(TAG, "EncUser" + Name +" " + Mac + " FE:" + score[0]+" DE:"+score[1] +" LV-C:" +score[2]+" LV-D:" +score[3]+ " Combined: "+score[4]);
		Log.i(TAG,  Mac + ";"+ Name +";" + score[0]+";"+score[1] +";" +score[2]+";" +score[3]+ ";"+score[4]+ ";" + trusted);
		//Log.i(TAG,  Mac + ";decay;"+ Name +";" + decayScore[0]+";"+decayScore[1] +";" +decayScore[2]+";" +decayScore[3]+ ";"+decayScore[4]+ ";" );

	}
	
	public void printOne(int index) {
		Log.i(TAG, "EncUser" + Name +" " + Mac + "Requested score: " + score[index]);
	}
	
	public void setTrust(int trust) {
		trusted = trust;
	}
}
	
