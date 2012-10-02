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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class map extends MapActivity {
	@Override
	protected boolean isRouteDisplayed() {
	    return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.map);
	    Bundle bundle = this.getIntent().getExtras();
        @SuppressWarnings("unchecked")
		final ArrayList<String> Address = (ArrayList<String>) bundle.getSerializable("useraddress");
	    
	    MapView mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
	    drawable.setBounds(0,0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
	    MapOverlay itemizedoverlay = new MapOverlay(drawable,this);
	    
	    /*GeoPoint point = new GeoPoint(19240000,-99120000);
	    OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
	    
	    itemizedoverlay.addOverlay(overlayitem);
	    mapOverlays.add(itemizedoverlay);
	    */
		StringTokenizer st;
		GeoPoint point;
	    OverlayItem overlayitem;

	    Iterator<String> itr = Address.iterator();
	    while (itr.hasNext()) {
	    	st = new StringTokenizer((String) itr.next(),",");
	    	double lat = Double.parseDouble(st.nextToken());
	        double lng = Double.parseDouble(st.nextToken());
	    	point = new GeoPoint( (int)(lat *1e6),(int) ( lng * 1e6));
	    	overlayitem = new OverlayItem(point, null, null);
	    	itemizedoverlay.addOverlay(overlayitem);
		    mapOverlays.add(itemizedoverlay);
	    }
	    
	}
	
}
