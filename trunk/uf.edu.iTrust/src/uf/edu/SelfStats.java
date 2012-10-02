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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class SelfStats extends Activity {
	/** Called when the activity is first created. */
	private static String TAG = "iTrust";
	boolean trustResult = false;
	CheckBox cb = null;
	String fname,lname,email,privacy,profile,macaddress;
	Context con;
	TreeMap<String, EncUser> encUser;
	private SharedPreferences prefs ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selfstats);
		Button b = null;
		TextView tv = null;
		con = getApplicationContext();
		SimpleDateFormat format;
		//receive bundle from the calling activity
		
		encUser =  iTrust.encmanage.encUser;

		//add the total number of encounters
		tv = (TextView) findViewById(R.id.textview_total_Ecounters);
		tv.setText(Integer.toString(encUser.size()));
		
		tv = (TextView) findViewById(R.id.textview_encounter_timeperiod);
		format = new SimpleDateFormat("dd-MMM-yyyy");
		tv.setText(format.format(new Date((long)Integer.parseInt(getStartDate())*1000)));
		
		//add scan stats 
		prefs = this.getSharedPreferences("iTrust", 0);
		
		tv = (TextView) findViewById(R.id.textview_encounter_scansPerfomed);
		tv.setText(Integer.toString(prefs.getInt("ScansPerformed", 0)));
		
		tv = (TextView) findViewById(R.id.textview_encounter_scansSaved);
		tv.setText(Integer.toString(prefs.getInt("ScansSaved", 0)));

		
		
		//start the histogram data collection.
		b = (Button) findViewById(R.id.button_num_encounters_time);
		b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
					//Now iterate through the whole list and copy them on a data structure and send it for plotting
				EncUser user;
				ArrayList<Integer> Duration = new ArrayList<Integer>(400);
				for (Entry<String, EncUser> entry : encUser.entrySet()){
					user = (EncUser)entry.getValue();
					//user.printAll();
					// index 1 is Duration
					Duration.add((int)user.score[1]);
				}
				
				Intent intent = barchartIntent(Duration, "Histogram for Total \n Encounter Duration","Encounter time in Seconds", "No. of Users");
		    	startActivity(intent);  
			}
		});

		//start the histogram data collection.
				b = (Button) findViewById(R.id.button_num_encounters_device);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
							//Now iterate through the whole list and copy them on a data structure and send it for plotting
						EncUser user;
						ArrayList<Integer> Duration = new ArrayList<Integer>(400);
						for (Entry<String, EncUser> entry : encUser.entrySet()){
							user = (EncUser)entry.getValue();
							//user.printAll();
							// index 1 is Duration
							//Log.i(TAG,"SeflStat : " + (int)user.score[0]);
							Duration.add((int)user.score[0]);
						}
						
						Intent intent = barchartIntent(Duration, "Histogram for Total \n Encounters", "No. of encounters", "No. of Users");
				    	startActivity(intent);  
					}
				});
				
		//Start the histogram for encounter per month 
				b = (Button) findViewById(R.id.button_num_encounters_per_month);
				b.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
							//Now iterate through the whole list and copy them on a data structure and send it for plotting
						EncUser user;
						ArrayList<Integer> Duration = new ArrayList<Integer>(400);
						for (Entry<String, EncUser> entry : encUser.entrySet()){
							user = (EncUser)entry.getValue();
							Duration.addAll(user.timeSeries);
						}
						
						Intent intent = barchartIntentTemporal(Duration, "Encounters per Unit of time", "Time", "No. of Encounters");
				    	startActivity(intent);  
					}
				});

	}   


	
	

	public Intent barchartIntent(ArrayList<Integer> Data, String GraphTitle, String xTitle, String yTitle) {
		
		int Max =0, Min = 100000;
		int range;
		double [] set1,set2;
		//TODO: modify the number of bars if needed.
		int numBins = 10 ;
		set1 = new double[numBins+1];
		set2 = new double[numBins+1];
		for (int i=0;i<(numBins+1);i++) {
			set2[i] = 0.0;
			set1[i] = 0.0;
		}
		//find the min and max for the range
		for (int k: Data) {
			if(k > Max)
				Max = k;
			if(k < Min)
				Min = k;
		}
		range = (Max - Min + 1) / numBins;
		
		//binning 
		for(int k: Data) {
			set1[(int)k/range]++;
		}
		

		//find the max value to set xMax
		double yMax = 0.0;
		for (double k: set1) {
			if(k > yMax)
				yMax = k;
		}
		//increase the xmax by 10% to improve visibility
		yMax = yMax+2;
		String[] titles = new String[] { "", "" };

		List<double[]> values = new ArrayList<double[]>();
		values.add(set1);
		values.add(set2);

		int[] colors = new int[] { Color.GREEN	, Color.BLACK };
		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		renderer.setOrientation(Orientation.HORIZONTAL);
		renderer.setBackgroundColor(Color.TRANSPARENT);
		setChartSettings(renderer, GraphTitle, xTitle, yTitle, 0,
				numBins+1, 0, yMax, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(0);
		renderer.setYLabels(10);
		renderer.setXLabelsAngle(35.0F);

		//depending on the unit show the labels - year should show year, month should show month
	
		for (int i=0;i<(numBins+1);i++) {
			renderer.addXTextLabel(i+0.75, (range*i) + " - " + (range*(i+1)) );
		}

		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
			seriesRenderer.setDisplayChartValues(true);
		}
		return ChartFactory.getBarChartIntent(this, buildBarDataset(titles, values), renderer,
				Type.DEFAULT);
	}

public Intent logbarchartIntent(ArrayList<Integer> Data, String GraphTitle, String xTitle, String yTitle) {
		
		int Max =0, Min = 100000;
		int range;
		double [] set1,set2;
		//TODO: modify the number of bars if needed.
		int numBins = 10 ;
		set1 = new double[numBins+1];
		set2 = new double[numBins+1];
		for (int i=0;i<(numBins+1);i++) {
			set2[i] = 0.0;
			set1[i] = 0.0;
		}
		//find the min and max for the range
		for (int k: Data) {
			if(k > Max)
				Max = k;
			if(k < Min)
				Min = k;
		}
		range = (Max  * Max ) / numBins;
		
		//binning 
		for(int k: Data) {
			Log.i(TAG, Integer.toString((int)(k*k)/range));
			//set1[(int) (Math.log((double)k)/range)]++;
		}
		

		//find the max value to set xMax
		double yMax = 0.0;
		for (double k: set1) {
			if(k > yMax)
				yMax = k;
		}
		//increase the xmax by 10% to improve visibility
		yMax = yMax+2;
		String[] titles = new String[] { "", "" };

		List<double[]> values = new ArrayList<double[]>();
		values.add(set1);
		values.add(set2);

		int[] colors = new int[] { Color.GREEN	, Color.BLACK };
		XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
		renderer.setOrientation(Orientation.HORIZONTAL);
		renderer.setBackgroundColor(Color.TRANSPARENT);
		setChartSettings(renderer, GraphTitle, xTitle, yTitle, 0,
				numBins+1, 0, yMax, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(0);
		renderer.setYLabels(10);
		renderer.setXLabelsAngle(35.0F);

		//depending on the unit show the labels - year should show year, month should show month
	
		for (int i=0;i<(numBins+1);i++) {
			renderer.addXTextLabel(i+0.75, (range*i) + " - " + (range*(i+1)) );
		}

		int length = renderer.getSeriesRendererCount();
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
			seriesRenderer.setDisplayChartValues(true);
		}
		return ChartFactory.getBarChartIntent(this, buildBarDataset(titles, values), renderer,
				Type.DEFAULT);
	}

	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
			String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor,
			int labelsColor) {
		// sets lots of default values for this renderer
		renderer.setChartTitle(title);
		renderer.setXTitle(xTitle);
		renderer.setYTitle(yTitle);
		renderer.setXAxisMin(xMin);
		renderer.setXAxisMax(xMax);
		renderer.setYAxisMin(yMin);
		renderer.setYAxisMax(yMax);
		renderer.setAxesColor(axesColor);
		renderer.setLabelsColor(labelsColor);
	}

	protected XYMultipleSeriesDataset buildBarDataset(String[] titles, List<double[]> values) {
		// adds the axis titles and values into the dataset
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			CategorySeries series = new CategorySeries(titles[i]);
			double[] v = values.get(i);
			int seriesLength = v.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(v[k]);
			}
			dataset.addSeries(series.toXYSeries());
		}
		return dataset;
	}

	protected XYMultipleSeriesRenderer buildBarRenderer(int[] colors) {
		// creates a SeriesRenderer and initializes it with useful default values as well as colors
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		int length = colors.length;
		for (int i = 0; i < length; i++) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(colors[i]);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}

	 public Intent barchartIntentTemporal(ArrayList<Integer> timeStamp, String GraphTitle, String xTitle, String yTitle) {
    	 int time;
    	 int HOUR = 3600;
    	 int DAY = HOUR * 24;
    	 int WEEK = DAY * 7;
    	 int MONTH = DAY * 30;
    	 int YEAR = WEEK * 52;
    	 int numBins, Min =2147483647,Max=0;
    	 SimpleDateFormat format;
    	 String timeUnit=null;
    	 double [] set1,set2;
    	 int first;
    	 //to get the range
    	 for (int k: timeStamp) {
 			if(k > Max)
 				Max = k;
 			if(k < Min)
 				Min = k;
 		}
    	 int diff =  Max - Min;
    	
    	 if(diff/YEAR > 1) {
    		 time = YEAR;
    		 timeUnit = "Year";
    		 format = new SimpleDateFormat("yyyy");
    		 first =   (int) ((float) Min/ (float) (86400.0F * (float) 365.25F)) * (int) (86400.0F * 365.25F) ;
    	 }
    	 else if(diff/MONTH > 1) {
    		 time = MONTH;
    		 timeUnit = "Month";
    		 format = new SimpleDateFormat("MMM-yyyy");
    	     first =   (int) ((float) Min/ (float) (86400.0F * (float) 365.25F /12.0F)) * (int) (86400.0F * 365.25F /12.0F) ;
    	 }
    	 else if (diff/WEEK > 1) {
    		 time = WEEK;
    		 timeUnit = "Week";
    		 format = new SimpleDateFormat("W 'Week'  MMM-yyyy");
    		 first =   ((int) ((float) Min/ (float) (86400.0F * (float) 365.25F /52.0F))) * (int) (86400.0F * 365.25F /52.0F) ;
    	 } 
    	 else if (diff/DAY > 1) {
    		 time = DAY;
    		 timeUnit = "Day";
    		 format = new SimpleDateFormat("EEE dd-MMM-yyyy");
    		 first =   (int) ((float) Min/ (float) (86400.0F)) * (int) (86400.0F) ;
    	 }	 
    	 else {
    		 time = HOUR;
    		 timeUnit = "Hour";
    		 format = new SimpleDateFormat("HH dd-MMM-yyyy");
    		 first =   (int) ((float) Min/ (float) (3600.0F)) * (int) (3600.0F) ;
    	 }			 
    	 
    	 numBins = (Max - first)/time + 1;
    	 set1 = new double[numBins];
    	 set2 = new double[numBins];
    	 for (int i=0;i<numBins;i++) {
    		 set2[i] = 0.0;
    	 }
    	
    	//now Iterate over the arrayList and do bining
    	 
    	 
    	for (int i: timeStamp) {
    		int tmp = (i-first)/time;
    		set1[tmp]++;
    		//Log.e(TAG,Integer.toString(i));
    	}
    	 
    	//find the max value to set xMax
    	double yMax = 0.0;
    	for (double k: set1) {
    		if(k > yMax)
    			yMax = k;
    	}
    	//increase the xmax by 10% to improve visibility
    	 yMax = yMax+2;
    	 String[] titles = new String[] { "", " " };

         List<double[]> values = new ArrayList<double[]>();
         values.add(set1);
         values.add(set2);

         int[] colors = new int[] { Color.GREEN	, Color.BLACK };
         XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
         renderer.setOrientation(Orientation.HORIZONTAL);
         renderer.setBackgroundColor(Color.TRANSPARENT);
         setChartSettings(renderer,GraphTitle, xTitle, yTitle, 0,
             numBins+1, 0, yMax, Color.GRAY, Color.LTGRAY);
         renderer.setXLabels(0);
         renderer.setYLabels(10);
         renderer.setXLabelsAngle(35.0F);
         
         //depending on the unit show the labels - year should show year, month should show month
         Date date;
         String prev = null;
         for (int i=0;i<numBins;i++) {
        	 date = new Date((long)(first + time*(i+1))*1000);
        	 renderer.addXTextLabel(i+0.75, format.format(date));
        	 //dirty hack :(
        	 if( prev !=null && prev.compareTo(format.format(date)) == 0) {
        		 renderer.addXTextLabel(i+ 0.75 - 1, format.format(new Date ((long)(first + (i-1)*time)*1000)));
        	 }
        	 
        	 prev = format.format(date);
         }

         int length = renderer.getSeriesRendererCount();
         for (int i = 0; i < length; i++) {
           SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
           seriesRenderer.setDisplayChartValues(true);
         }
         return ChartFactory.getBarChartIntent(this, buildBarDataset(titles, values), renderer,
             Type.DEFAULT);
     }
	
	 String getStartDate() {
			String Date = null;
			BufferedReader blueIn = null;
			String blueData;
			
			File fb = new File(Environment.getExternalStorageDirectory(), con.getString(R.string.DataPath)+"/ZIPscannedDataW");
			try {
				blueIn = new BufferedReader(new FileReader(fb));
				if((blueData = blueIn.readLine())==null) {
					Date=null;
				} else {
					Date = new StringTokenizer(blueData, ";").nextToken();
				}
				
				blueIn.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Date;
		}
}   




