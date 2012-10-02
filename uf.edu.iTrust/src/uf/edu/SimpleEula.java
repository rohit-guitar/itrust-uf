/*******************************************************************************
 * Copyright (c) 2011 DONN FELKER.
 * All rights reserved. 
 * 
 * This file is part of iTrust application for android.
 * iTrust is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. 
 * 
 * iTrust is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with iTrust.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 ******************************************************************************/
package uf.edu;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

public class SimpleEula {

	private String EULA_PREFIX = "eula_";
	private Activity mActivity;

	public SimpleEula(Activity context) {
		mActivity = context;
	}

	private PackageInfo getPackageInfo() {
		PackageInfo pi = null;
		try {
			pi = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), PackageManager.GET_ACTIVITIES);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return pi;
	}

	public void show() {
		PackageInfo versionInfo = getPackageInfo();

		// the eulaKey changes every time you increment the version number in the AndroidManifest.xml
		final String eulaKey = EULA_PREFIX + versionInfo.versionCode;
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		boolean hasBeenShown = prefs.getBoolean(eulaKey, false);
		if(hasBeenShown == false){

			// Show the Eula
			String title = mActivity.getString(R.string.app_name) + " v" + versionInfo.versionName;

			//Includes the updates as well so users know what changed.
			String message = mActivity.getString(R.string.updates) + "\n\n" + mActivity.getString(R.string.eula);

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity)
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					// Mark this version as read.
					SharedPreferences.Editor editor = prefs.edit();
					editor.putBoolean(eulaKey, true);
					editor.commit();
					dialogInterface.dismiss();
				}
			})
			.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Close the activity as they have declined the EULA
					mActivity.finish();
				}

			});
			builder.create().show();
		}
	}

}
