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

 



import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class Settings extends Activity {
	/** Called when the activity is first created. */
	private static String TAG = "iTrust";
	boolean trustResult = false;
    CheckBox cb = null;
	String fname,lname,email,privacy,profile,macaddress;
	Context con;
     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        con = getApplicationContext();
        SharedPreferences pres;
    	pres = this.getSharedPreferences("iTrust", 0); 
    	
    	//for Auto upload settings
    	
    	cb = (CheckBox) findViewById(R.id.settings_checkBox_upload);
    	cb.setChecked(pres.getBoolean("AutoUpload", true));
    	cb.setClickable(true);
    	 
        cb.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				//Whatever the state is .. save it to the temp files. 
		        SharedPreferences pres = getSharedPreferences("iTrust", 0);
				SharedPreferences.Editor ed = pres.edit();

				if (((CheckBox) v).isChecked()) {
			    	ed.putBoolean("AutoUpload", true);	 // This default is also set in autoupload function in iTrust class
			    	Log.i(TAG,"Settings: autoupload selected");
				} else {
					ed.putBoolean("AutoUpload", false);
				}				
		   	    ed.commit();				
			}
		});
        
        
        //for Energy Efficiency settings
        cb = (CheckBox) findViewById(R.id.settings_checkBox_energy);
    	cb.setChecked(pres.getBoolean("EnergyEfficient", false));
    	cb.setClickable(true);
    	 
        cb.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				//Whatever the state is .. save it to the temp files. 
		        SharedPreferences pres = getSharedPreferences("iTrust", 0);
				SharedPreferences.Editor ed = pres.edit();

				if (((CheckBox) v).isChecked()) {
			    	ed.putBoolean("EnergyEfficient", true);	
			    	Log.i(TAG,"Settings: energy efficient scanning  selected");
				} else {
					ed.putBoolean("EnergyEfficient", false);
				}				
		   	    ed.commit();	
		    	Toast.makeText(con, "Please restart the scanning!", Toast.LENGTH_LONG).show();

			}
		});
        
        //for Weight Settings
        NumberPicker np = (NumberPicker) findViewById(R.id.Settings_numberPicker_numperpage);
        np.setValue(pres.getInt("showItems", 20));
        
        np.setOnValueChangedListener(new NumberPicker.OnValueChangedListener() {
			@Override
			public void onValueChanged(int Value) {
				if(Value > 0) {
				 SharedPreferences pres = getSharedPreferences("iTrust", 0);
				 SharedPreferences.Editor ed = pres.edit();
				 ed.putInt("showItems", Value);
				 ed.commit();
				} else {
			    	Toast.makeText(con, "Number of devices to show cannot be less than 1!", Toast.LENGTH_LONG).show();

				}
			}
		});
    }
     
    
}   
  



