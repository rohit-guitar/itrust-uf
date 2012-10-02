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

 
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;
import org.achartengine.renderer.XYSeriesRenderer;



import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class encDetailActivity extends Activity {
	/** Called when the activity is first created. */
 	private static Context context;
	private static String TAG = "iTrust";
	boolean trustResult = false;
	int MaxTrust;
	String[] TrustArray = {"No Trust", "Low Trust", "No Information", "Some Trust", "High Trust"};
     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encdetail_activity);
        context = this.getApplicationContext();
        TextView tv =null;
        SeekBar sb=null;
        Button b = null;
        Bundle bundle = this.getIntent().getExtras();
        Log.i("TAG","encDetailActivity:onCreate,  Max Trust ="+getString(R.string.TrustSliderMax));
        MaxTrust = Integer.parseInt(getString(R.string.TrustSliderMax))/2;
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Okay clicked.
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
                }
            }
        };

        //builder.setMessage("Are you sure?").setPositiveButton("Okay", dialogClickListener);

        final EncUser encUser = (EncUser) bundle.getSerializable("userdata"); 
        final ArrayList<String> Address = (ArrayList<String>) bundle.getSerializable("useraddress");
        if(encUser == null) {
        	Log.i(TAG,"encDetailsActivity: parameter received was null");
        }
        //Name
        tv =  (TextView)  findViewById(R.id.encdetail_name);
        tv.setText(encUser.Name);
        //Mac
        tv =  (TextView)  findViewById(R.id.encdetail_mac);
        tv.setText(encUser.Mac);
        tv.setOnClickListener(new View.OnClickListener() {  //event listener to fetch more info abt a user.
			@Override
			public void onClick(View v) {
				iTrust.cd.write("Registration Lookup requested for " + encUser.Mac);
	        	Toast.makeText(encDetailActivity.this, "Please wait while we lookup the information", Toast.LENGTH_LONG).show();
				String data = getData(encUser.Mac).replace('\n', ' ').trim();
				String message = null;
				if (data.substring(0,5).compareToIgnoreCase("Error")==0) {
					message = "No more info available for this device";
				} else {
					data = data.replace('\'',' '); //remove single quotes
					data = data.replace(')', ' ').trim(); //remove closing brackets.
					StringTokenizer tok = new StringTokenizer(data.substring(1),",");
					message = "Name: "+tok.nextToken() + " " + tok.nextToken() + "\n" + "Email: " + tok.nextToken() + "\n" + "Profile: " + tok.nextToken();
				}
	        	//Toast.makeText(encDetailActivity.this, getData(encUser.Mac), Toast.LENGTH_LONG).show();
	        	builder.setMessage(message);
	        	builder.show();
			}
		});
        //lasttime
        tv =  (TextView)  findViewById(R.id.encdetail_lasttime);
        tv.setText((new java.util.Date((long)encUser.lastEncounterTime*1000)).toString());
        tv.setOnClickListener(new View.OnClickListener() {  //To generate graph
			@Override
			public void onClick(View v) {
			 	Intent intent = barchartIntent(encUser.timeSeries,encUser.Mac,encUser.Name);
		    	startActivity(intent);  
			}
		});
        
        
        
        //tv.setText(Integer.toString(encUser.lastEncounterTime)); 
        //FE
        tv =  (TextView)  findViewById(R.id.encdetail_FE);
        tv.setText(Float.toString(encUser.score[0]));
        tv =  (TextView)  findViewById(R.id.encdetail_decayFE);
        tv.setText(Float.toString(encUser.decayScore[0]*((float)Math.pow(.5,(float)(((float)System.currentTimeMillis()/1000)-(float)encUser.lastEncounterTime)/(float)15552000.0F))));
        //DE
        tv =  (TextView)  findViewById(R.id.encdetail_DE);
        tv.setText(Float.toString(encUser.score[1]));
        tv =  (TextView)  findViewById(R.id.encdetail_decayDE);
        tv.setText(Float.toString(encUser.decayScore[1] *((float)Math.pow(.5,(float)(((float)System.currentTimeMillis()/1000)-(float)encUser.lastEncounterTime)/(float)15552000.0F))));
        //LV-C
        tv =  (TextView)  findViewById(R.id.encdetail_LVC);
        tv.setText(Float.toString(encUser.score[2]));
        //LV-D
        tv =  (TextView)  findViewById(R.id.encdetail_LVD);
        tv.setText(Float.toString(encUser.score[3]));
        //combined 
        //FE
        tv =  (TextView)  findViewById(R.id.encdetail_comb);
        tv.setText(Float.toString(encUser.score[4]));
        
        //check the toggle button state
        
        sb = (SeekBar) findViewById(R.id.encdetail_trust);
        //since Seekbar cannot go into -ve values we scale -Max Value to Max Value
        sb.setProgress(encUser.trusted + MaxTrust);
        tv = (TextView) findViewById(R.id.CurrentTrustValue);
        tv.setText(TrustArray[encUser.trusted + MaxTrust]);
        
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            TextView tvcurrent = (TextView) findViewById(R.id.CurrentTrustValue);
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(fromUser == true) {
                 	Log.i(TAG,"encDetailsActivity: User changed value for Trust to " + progress);
					encUser.trusted = progress - MaxTrust;
					tvcurrent.setText(TrustArray[progress]);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
		}); 
        		
        
        
        b = (Button)  findViewById(R.id.encdetail_done);
        b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				iTrust.cd.write("Trust Value changed for this user :" + encUser.Mac + " to:" + encUser.trusted);
				Bundle bundle = new Bundle();
				Intent returnIntent = new Intent();
				bundle.putSerializable("Object",encUser);
				returnIntent.putExtras(bundle);
			    //returnIntent.putExtra("TrustSet",trustResult);
				encDetailActivity.this.setResult(Activity.RESULT_OK,returnIntent);    
			    finish();
			}
		});
        
        //show map
        tv = (TextView) findViewById(R.id.encdetail_map);
        tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				iTrust.cd.write("Map lookup for encounter user " + encUser.Mac);
				 Bundle bundle = new Bundle();
				 Intent newIntent = new Intent(context, map.class);
				 bundle.putSerializable("useraddress",Address);
				 newIntent.putExtras(bundle);
	    		 startActivity(newIntent);
			}
		});
        
        
    }
     
     public String getData(String mac) {
 	 	//Data should be verified before calling this code. no verification done here.
 	    // Create a new HttpClient and Post Header
 	    HttpClient httpclient = new DefaultHttpClient();
 	    HttpPost httppost = new HttpPost(getString(R.string.registryURL));
 	    try {
 	        // Add your data
 	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 	        nameValuePairs.add(new BasicNameValuePair("mac", mac));

 	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

 	        // Execute HTTP Post Request
 	        ResponseHandler<String> responseHandler=new BasicResponseHandler();
 	        String responseBody = httpclient.execute(httppost,responseHandler).replace('\n', ' ').trim();
 	        //JSONObject response=new JSONObject(responseBody);
 	        Log.i(TAG,"Registration Response: "+ responseBody);
 	        return responseBody;
 	        	
 	    } catch (ClientProtocolException e) {
 	        // TODO Auto-generated catch block
 	    } catch (IOException e) {
 	        // TODO Auto-generated catch block
 	    }
 	    return null;
 	}  
     
     
     
     public Intent barchartIntent(ArrayList<Integer> timeStamp, String mac, String macname) {
    	 int time;
    	 int HOUR = 3600;
    	 int DAY = HOUR * 24;
    	 int WEEK = DAY * 7;
    	 int MONTH = DAY * 30;
    	 int YEAR = WEEK * 52;
    	 int numBins;
    	 SimpleDateFormat format;
    	 String timeUnit;
    	 double [] set1,set2;
    	 int first;
    	 //to get the range
    	 int diff = timeStamp.get(timeStamp.size()-1) - timeStamp.get(0);
    	
    	 if(diff/YEAR > 1) {
    		 time = YEAR;
    		 timeUnit = "Year";
    		 format = new SimpleDateFormat("yyyy");
    		 first =   (int) ((float) timeStamp.get(0)/ (float) (86400.0F * (float) 365.25F)) * (int) (86400.0F * 365.25F) ;
    	 }
    	 else if(diff/MONTH > 1) {
    		 time = MONTH;
    		 timeUnit = "Month";
    		 format = new SimpleDateFormat("MMM-yyyy");
    	     first =   (int) ((float) timeStamp.get(0)/ (float) (86400.0F * (float) 365.25F /12.0F)) * (int) (86400.0F * 365.25F /12.0F) ;
    	 }
    	 else if (diff/WEEK > 1) {
    		 time = WEEK;
    		 timeUnit = "Week";
    		 format = new SimpleDateFormat("W 'Week'  MMM-yyyy");
    		 first =   ((int) ((float) timeStamp.get(0)/ (float) (86400.0F * (float) 365.25F /52.0F))) * (int) (86400.0F * 365.25F /52.0F) ;
    	 } 
    	 else if (diff/DAY > 1) {
    		 time = DAY;
    		 timeUnit = "Day";
    		 format = new SimpleDateFormat("EEE dd-MMM-yyyy");
    		 first =   (int) ((float) timeStamp.get(0)/ (float) (86400.0F)) * (int) (86400.0F) ;
    	 }	 
    	 else {
    		 time = HOUR;
    		 timeUnit = "Hour";
    		 format = new SimpleDateFormat("HH dd-MMM-yyyy");
    		 first =   (int) ((float) timeStamp.get(0)/ (float) (3600.0F)) * (int) (3600.0F) ;
    	 }			 
    	 
    	 numBins = (timeStamp.get(timeStamp.size()-1) - first)/time + 1;
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
    	 String[] titles = new String[] { "Encounters per "+timeUnit, " " };

         List<double[]> values = new ArrayList<double[]>();
         values.add(set1);
         values.add(set2);

         int[] colors = new int[] { Color.GREEN	, Color.BLACK };
         XYMultipleSeriesRenderer renderer = buildBarRenderer(colors);
         renderer.setOrientation(Orientation.HORIZONTAL);
         renderer.setBackgroundColor(Color.TRANSPARENT);
         setChartSettings(renderer, "Previous Encounters with \n" + mac + "("+macname+")", "Time in "+timeUnit+"s", " No. of Encounters ", 0,
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
     
     
     
     protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
    	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
    	    setRenderer(renderer, colors, styles);
    	    return renderer;
    	  }

     protected XYMultipleSeriesDataset buildDataset(String[] titles, List<double[]> xValues,
    	      List<double[]> yValues) {
    	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    	    addXYSeries(dataset, titles, xValues, yValues, 0);
    	    return dataset;
    	  }
     
     public void addXYSeries(XYMultipleSeriesDataset dataset, String[] titles, List<double[]> xValues,
    	      List<double[]> yValues, int scale) {
    	    int length = titles.length;
    	    for (int i = 0; i < length; i++) {
    	      XYSeries series = new XYSeries(titles[i], scale);
    	      double[] xV = xValues.get(i);
    	      double[] yV = yValues.get(i);
    	      int seriesLength = xV.length;
    	      for (int k = 0; k < seriesLength; k++) {
    	        series.add(xV[k], yV[k]);
    	      }
    	      dataset.addSeries(series);
    	    }
    	  }
     
     protected void setRenderer(XYMultipleSeriesRenderer renderer, int[] colors, PointStyle[] styles) {
    	    renderer.setAxisTitleTextSize(16);
    	    renderer.setChartTitleTextSize(20);
    	    renderer.setLabelsTextSize(15);
    	    renderer.setLegendTextSize(15);
    	    renderer.setPointSize(5f);
    	    renderer.setMargins(new int[] { 20, 30, 15, 20 });
    	    int length = colors.length;
    	    for (int i = 0; i < length; i++) {
    	      XYSeriesRenderer r = new XYSeriesRenderer();
    	      r.setColor(colors[i]);
    	      r.setPointStyle(styles[i]);
    	      renderer.addSeriesRenderer(r);
    	    }
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
     protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
    	      List<double[]> yValues) {
    	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    	    int length = titles.length;
    	    for (int i = 0; i < length; i++) {
    	      TimeSeries series = new TimeSeries(titles[i]);
    	      Date[] xV = xValues.get(i);
    	      double[] yV = yValues.get(i);
    	      int seriesLength = xV.length;
    	      for (int k = 0; k < seriesLength; k++) {
    	        series.add(xV[k], yV[k]);
    	      }
    	      dataset.addSeries(series);
    	    }
    	    return dataset;
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

}   
  



