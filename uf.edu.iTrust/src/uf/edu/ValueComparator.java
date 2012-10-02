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

import java.util.Comparator;
import java.util.Map;

class ValueComparator implements Comparator {

  Map base;
  int index;
  public ValueComparator(Map base, int index) {
      this.base = base;
      this.index=index;
  }

  public int compare(Object a, Object b) {

	if(index>=0 && index <=4) {
		if((float)(((EncUser) base.get(a))).getScore(index) < (float)(((EncUser) base.get(b)).getScore(index))) {
			return 1;
		} else if((float)(((EncUser) base.get(a))).getScore(index) == (float)(((EncUser) base.get(b)).getScore(index))) {	
			return -1;
		} else {
			return -1;
		}
	} else if (index == 5) { //sort is to be done by last encounter time 
		if((int)(((EncUser) base.get(a))).lastEncounterTime < (int)(((EncUser) base.get(b)).lastEncounterTime)) {
			return 1;
		} else if((int)(((EncUser) base.get(a))).lastEncounterTime == (int)(((EncUser) base.get(b)).lastEncounterTime)) {	
			return -1;
		} else {
			return -1;
		}		
	} else {
		return -1;
	}
  }	
}
