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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
//***ListAdapter***
public class ListAdapter extends ArrayAdapter<String> { // --
															// CloneChangeRequired
	private ArrayList<String> mList; // --CloneChangeRequired
	private Context mContext;

	public ListAdapter(Context context, int textViewResourceId,
			ArrayList<String> list) { // --CloneChangeRequired
		super(context, textViewResourceId, list);
		this.mList = list;
		this.mContext = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		String Name ;
		String Mac;
		try {
			if (view == null) {
				LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.list_view, null); // --
																// CloneChangeRequired
																// (
																// list_item
																// )
			}
			final String listItem = mList.get(position); // --
															// CloneChangeRequired
			
			if (listItem != null) {
				// setting list_item views
				Mac = listItem.toString().substring(0, 17);
				Name = listItem.toString().substring(18);
				
				
				//checks if trusted and recently encountered.
				switch (listItem.toString().substring(17,18).charAt(0)) {
				case '+' :
					((TextView) view.findViewById(R.id.tv_description)).setTextColor(Color.BLUE);
					((TextView) view.findViewById(R.id.tv_name)).setTextColor(Color.BLUE);
					((ImageView)view.findViewById(R.id.iv_encounter)).setVisibility(View.VISIBLE);
					break;
				case '-' :
					((TextView) view.findViewById(R.id.tv_description)).setTextColor(Color.BLUE);
					((TextView) view.findViewById(R.id.tv_name)).setTextColor(Color.BLUE);
					((ImageView)view.findViewById(R.id.iv_encounter)).setVisibility(View.INVISIBLE);
					break;
				case '/' :
					((TextView) view.findViewById(R.id.tv_description)).setTextColor(Color.BLACK);
					((TextView) view.findViewById(R.id.tv_name)).setTextColor(Color.BLACK);
					((ImageView)view.findViewById(R.id.iv_encounter)).setVisibility(View.VISIBLE);
					break;
				case '|':
					((TextView) view.findViewById(R.id.tv_description)).setTextColor(Color.BLACK);
					((TextView) view.findViewById(R.id.tv_name)).setTextColor(Color.BLACK);
					((ImageView)view.findViewById(R.id.iv_encounter)).setVisibility(View.INVISIBLE);
					break;		
				case '*':	
					((TextView) view.findViewById(R.id.tv_description)).setTextColor(Color.RED);
					((TextView) view.findViewById(R.id.tv_name)).setTextColor(Color.RED);
					((ImageView)view.findViewById(R.id.iv_encounter)).setVisibility(View.VISIBLE);
					break;
				case '&':
					((TextView) view.findViewById(R.id.tv_description)).setTextColor(Color.RED);
					((TextView) view.findViewById(R.id.tv_name)).setTextColor(Color.RED);
					((ImageView)view.findViewById(R.id.iv_encounter)).setVisibility(View.INVISIBLE);
					break;
				default: 
					Log.e("iTrust","Check displayUserInOrder as it is setting the divider with " + listItem.toString().substring(17,18).charAt(0) );
				}
				
				//set name and MAC address
				((TextView) view.findViewById(R.id.tv_description)).setText(Mac);
				((TextView) view.findViewById(R.id.tv_name)).setText(Name);
				((TextView) view.findViewById(R.id.tv_rank)).setText(Integer.toString(position+1));
			}
		} catch (Exception e) {
			Log.i("iTrust", e.getMessage());
		}
		return view;
	}
}
