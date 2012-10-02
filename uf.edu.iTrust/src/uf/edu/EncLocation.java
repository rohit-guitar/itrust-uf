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

public class EncLocation implements Serializable {
	int locId;
	int duration;
	int count;
	public EncLocation (int locId) {
		this.locId = locId;
		duration = 0;
		count = 0;
	}
	public int getDuration() {
		return duration;
	}
	public int getCount() {
		return count;
	}
	public int getLocId() {
		return locId;
	}
	public void addDuration(int duration) {
		this.duration += duration;
	}	
	public void addCount(int count) {
		this.count += count;
	}	
}
