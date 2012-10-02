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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class iTrust extends Activity {
	/** Called when the activity is first created. */
	private static ListView lvmain;
	private static ArrayList<String> lv_arr = new ArrayList<String>();//[] = {"Android","iPhone","BlackBerry","AndroidPeople"};
	private static Context context;
	private static String TAG = "iTrust";
	private static APLocParser alp;
	public static EncManager encmanage;
	private static int SortingIndex=0;
	private static int[] filterWeight = new int[] {1,1,1,1};
	private SharedPreferences prefs ;
	private static int  RecentEncounterTimeLimit = 300;  //change this to increase last encounter time limit
	private static ArrayList<String> RecentEncounterMAC = new ArrayList<String>(10);
  	Dialog weightDialog;
  	private static int defaultItemsToShow = 20;// default number of devices to show.
  	private static int showItems ;
  	private static boolean toSave = false;
  	private static iTrust instance_;
  	private boolean refreshInProgress = false;
  	private int scoringInProgress =0;
  	private int locationUpdateInProgress =0;
	final CharSequence[] items = { "FE Score", "DE Score", "LV-C Score", "LV-D Score", "Combined Score","Last Encounter","Around Me"};
	AlertDialog.Builder  builder;
	AlertDialog alert;
	public static ClickData cd;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        new SimpleEula(this).show();
        context = this.getApplicationContext();
        instance_ = this;
        cd = new ClickData(context);
        View footer = getLayoutInflater().inflate(R.layout.footerlayout, null);
		lvmain = (ListView)  findViewById(R.id.listViewMain);
		lvmain.addFooterView(footer, null, false);
    	alp = new APLocParser(context, "ap_loc.tmp");
		encmanage = new EncManager(context,"encDetails.tmp",600);
		lvmain.setClickable(true);
		prefs = getPreferences(0); 
		defaultItemsToShow = prefs.getInt("showItems", 20);
		showItems = defaultItemsToShow;
		//encmanage.printAll();
		
		  //TODO: remove the line below; only for debugging.
   	      // alp.printIDAddress();
		
		//starts another activity to show the detail results for a Device
		lvmain.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
	    		  String Mac  = ((String)lvmain.getItemAtPosition(pos)).substring(0, 17);
	    		  Log.i(TAG, "OnItemClickListener you clicked position " + pos +" and the contect was "+ lvmain.getItemAtPosition(pos) + "  and mac is "+Mac);
	    		  cd.write("OnItemClickListener you clicked position " + pos +" and the contect was "+ lvmain.getItemAtPosition(pos) + "  and mac is "+Mac);
	    		  Bundle bundle = new Bundle();
	    		  bundle.putSerializable("userdata", encmanage.getEncUserDetail(Mac));
	    		  
	    		  //this block is added to send gps coordinates to the map via encDetailActivity.
	    		  Collection<EncLocation> c =  ((EncUser) encmanage.getEncUserDetail((String)Mac)).locMap.values();
	    		  Iterator<EncLocation> itr = c.iterator();
	    		  ArrayList<String> address = new ArrayList<String>();
	    		  //reads the loc id from EncLocation and then get the address from APLocParser.
	    		  while(itr.hasNext()) {
	    			int locid =   ((EncLocation)itr.next()).getLocId();
	    			String add = alp.getAddress(locid);
	    			if(add==null)
	    				continue;
	    			address.add(add);
	    		  }
	    		  bundle.putSerializable("useraddress", address);
	    		  
	    		  Intent newIntent = new Intent(context, encDetailActivity.class);
	    		  newIntent.putExtras(bundle);
	    		  startActivityForResult(newIntent,0);
			}
		}) ;
		
		//create a event on footer to show more items.
		
		footer.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
	    		Log.i(TAG,"Request received for more items");
	    		showItems += defaultItemsToShow;  //show 10 more times.
	    		displayUsers();
	    		lvmain.setSelection(showItems-11);
	   // 		cd.write("ShowItem went to " + showItems);

	    }
		});
		
   	    SortingIndex = prefs.getInt("SortingIndex", 0);
   	    Log.i(TAG,"Sorting Index " + SortingIndex);
   	    
		displayUserInOrder(SortingIndex);
		builder = new AlertDialog.Builder(this);
   	    builder.setTitle("Choose the Sorting Key");
   	   

   	    //Weight set Dialog's custom layout creation
   	    weightDialog = new Dialog(iTrust.this);
   	    weightDialog.setContentView(R.layout.weight);
   	    weightDialog.setTitle("Set weights for Combined filter");
   	    weightDialog.setCancelable(true);   
   	    loadWeights();
   	    setEventHandlerWeightSet();
   	    
   	  
   	    checkForAutoRefresh();
   	    checkForUpload();
   	  
   	    //Also store the number of scans in the ClickData file, each time the app is started.
   	    cd.write("Scans Saved :" + prefs.getInt("ScansSaved", 0));
   	    cd.write("Scans Performed :" + prefs.getInt("ScansPerformed", 0));
    }
    
    
    
    
    private void checkForAutoRefresh() {
    	//autorefresh is based on two parameters -> size of scanned file and last refresh
    	
    	//constants needed: size and number of days. 
    	if(refreshInProgress == true)
    		return;
    	int[] conditions = getResources().getIntArray(R.array.refreshCond); //0 for size & 1 for days
    	File file1 = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/"+ "scannedDataW");
    	long lastRefreshDate = prefs.getLong("scoreRefreshDate", 0);
    	long curdate = System.currentTimeMillis()/1000;
    	if ((file1.length() > conditions[0]) || ((curdate-lastRefreshDate) > (conditions[1]*86400))) {  //data is in Days
    		///refresh now.
	    	Toast.makeText(context, "Refreshing scores!", Toast.LENGTH_LONG).show();
	    	refreshInProgress  = true;
  		  	new RefreshScore().execute();
	    	
    	} else {
    		Log.i(TAG,"Score Refresh not needed");
    	}
    	
	}

    private void checkForUpload() {
    	//check if autoupload is checked
    	if(prefs.getBoolean("AutoUpload", true) == false)  //defaults
    		return; 	
    	 if(isServiceRunning(context,"uf.edu.UploadService")) {
   		  	return;
    	 }  
    	 
    	 //this stops uploads when app is used for the first time
     	File file1 = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/"+ "ZIPscannedDataW");     	
     	if(file1.exists() == false) {
     		return;
     	}
    	 
    	//if the upload doesnot takes place within uploadPeriodDays .. do it. 
    	int uploadPeriodDays = Integer.parseInt(getResources().getString(R.string.uploadPeriodDays)); //in days
    	long lastUploadDate = prefs.getLong("LastUploadTime", 0);
    	long curdate = System.currentTimeMillis()/1000;
    	if (((long)(curdate-lastUploadDate)/86400) > uploadPeriodDays ) {  //data is in Days
  		  startService(new Intent(this, UploadService.class));
    	} else {
    		Log.i(TAG,"Upload not needed");
    	}
    	
	}

    
    

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();	
		  //for showing current encounters
   	    IntentFilter filter = new IntentFilter(ScanService.CUSTOM_INTENT);
   	    this.registerReceiver(ScanResults, filter);
   	    defaultItemsToShow = prefs.getInt("showItems", 20);
		showItems = defaultItemsToShow;
		
	}




	void getCurrentEncounters () {
    	String msg = prefs.getString("LatestScan", null);
    	StringTokenizer st;
    	int ts = 0;
    	RecentEncounterMAC.clear();
    	if (msg == null) { //there was nothing set in the shared preferences.
    		Log.e(TAG,"No encounter information in the shared file");
    		return;
    	}
    	//check time and then parse them into a data structure
    	st = new StringTokenizer(msg,";");
    	ts = Integer.parseInt(st.nextToken());
    	if((ts+RecentEncounterTimeLimit) < (System.currentTimeMillis()/1000)){
    		return;
    	}
    	while(st.hasMoreTokens()){
    		String mac= st.nextToken();
    		Log.d(TAG,"REcently encountered MAC " + mac);
    		RecentEncounterMAC.add(mac);
    	}
    }
    
    void setEventHandlerWeightSet() {
    	NumberPicker np = (NumberPicker) weightDialog.findViewById(R.id.WeightFE);
    	np.setOnValueChangedListener(new NumberPicker.OnValueChangedListener() {
			@Override
			public void onValueChanged(int Value) {
				filterWeight[0]=Value;
		    	//Toast.makeText(context, "Calculating New Score and Refreshing the list..", Toast.LENGTH_LONG).show();
				encmanage.calCombScore( filterWeight[0], filterWeight[1], filterWeight[2], filterWeight[3]);
		    	cd.write("Weights Set : "+ filterWeight[0] +","+ filterWeight[1] +","+ filterWeight[2] +","+ filterWeight[3]);
				if(SortingIndex == 4)
					displayUserInOrder(SortingIndex);
				saveWeights();
				toSave = true;

			}
		});
    	np = (NumberPicker) weightDialog.findViewById(R.id.WeightDE);
    	np.setOnValueChangedListener(new NumberPicker.OnValueChangedListener() {
			@Override
			public void onValueChanged(int Value) {
				filterWeight[1]=Value;
		    	//Toast.makeText(context, "Calculating New Score and Refreshing the list..", Toast.LENGTH_LONG).show();
				encmanage.calCombScore( filterWeight[0], filterWeight[1], filterWeight[2], filterWeight[3]);
		    	cd.write("Weights Set : "+ filterWeight[0] +","+ filterWeight[1] +","+ filterWeight[2] +","+ filterWeight[3]);
				if(SortingIndex == 4)
					displayUserInOrder(SortingIndex);
				saveWeights();
				toSave = true;

			}
		});
    	np = (NumberPicker) weightDialog.findViewById(R.id.WeightLVC);
    	np.setOnValueChangedListener(new NumberPicker.OnValueChangedListener() {
			@Override
			public void onValueChanged(int Value) {
				filterWeight[2]=Value;		    	
				//Toast.makeText(context, "Calculating New Score and Refreshing the list..", Toast.LENGTH_LONG).show();
				encmanage.calCombScore( filterWeight[0], filterWeight[1], filterWeight[2], filterWeight[3]);
		    	cd.write("Weights Set : "+ filterWeight[0] +","+ filterWeight[1] +","+ filterWeight[2] +","+ filterWeight[3]);
				if(SortingIndex == 4)
					displayUserInOrder(SortingIndex);
				saveWeights();				
				toSave = true;
			}
		});
    	np = (NumberPicker) weightDialog.findViewById(R.id.WeightLVD);
    	np.setOnValueChangedListener(new NumberPicker.OnValueChangedListener() {
			@Override
			public void onValueChanged(int Value) {
				filterWeight[3]=Value;
		    	//Toast.makeText(context, "Calculating New Score and Refreshing the list..", Toast.LENGTH_LONG).show();
				encmanage.calCombScore( filterWeight[0], filterWeight[1], filterWeight[2], filterWeight[3]);
		    	cd.write("Weights Set : "+ filterWeight[0] +","+ filterWeight[1] +","+ filterWeight[2] +","+ filterWeight[3]);
				if(SortingIndex == 4)
					displayUserInOrder(SortingIndex);
				saveWeights();
				toSave = true;
			}
		});
    	
    }
    void loadWeights() {
    	if (prefs==null)
        	Log.i(TAG,"prefs is null" );

    	NumberPicker np = (NumberPicker) weightDialog.findViewById(R.id.WeightFE);
    	Log.i(TAG,"object returned" + prefs.getInt("WeightFE", 1) );
    	
   	    np.setValue(prefs.getInt("WeightFE", 1));
   	    filterWeight[0]= np.getValue();
   	    np = (NumberPicker) weightDialog.findViewById(R.id.WeightDE);
   	    np.setValue(prefs.getInt("WeightDE", 1));
   	    filterWeight[1]= np.getValue();
	    np = (NumberPicker) weightDialog.findViewById(R.id.WeightLVC);
   	    np.setValue(prefs.getInt("WeightLVC", 1));
   	    filterWeight[2]= np.getValue();
   	    np = (NumberPicker) weightDialog.findViewById(R.id.WeightLVD);
   	    np.setValue(prefs.getInt("WeightLVD", 1));
   	    filterWeight[3]= np.getValue();
    }
    
    void saveWeights() {
    	SharedPreferences.Editor ed = prefs.edit();
    	NumberPicker np = (NumberPicker) weightDialog.findViewById(R.id.WeightFE);
    	ed.putInt("WeightFE", np.getValue());
   	    np = (NumberPicker) weightDialog.findViewById(R.id.WeightDE);
   	    ed.putInt("WeightDE", np.getValue());
	    np = (NumberPicker) weightDialog.findViewById(R.id.WeightLVC);
	    ed.putInt("WeightLVC", np.getValue());
   	    np = (NumberPicker) weightDialog.findViewById(R.id.WeightLVD);
   	    ed.putInt("WeightLVD", np.getValue());
   	    ed.commit();
    }
    
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		  Log.i(TAG, "Result returned resultcode = "+ resultCode + " Intent data ="+data);
		  Bundle b= new Bundle();
		  switch(requestCode) {
		  case 0:
 			  if (resultCode == RESULT_OK) {
    		  Log.i(TAG, "Activity Result returned: OK");
    		  b = data.getExtras();
              EncUser e = (EncUser) b.getSerializable("Object");
    		  Log.i(TAG, "Trust Value returned = " + e.trusted);
    		  //now set the trust value back to the original structure.
    		  EncUser orig = encmanage.getEncUserDetail(e.Mac);
    		  
    		  //if the user has set this user to be trusted we want to update the color on the list view
    		  if(orig.trusted != e.trusted) {
        		  //toSave = true;
        		  if(orig.trusted !=0){
        			  trustedUser.removeMacfromTrust(context, e.Mac);
        		  }
        		  
        		  if(e.trusted != 0) {
        			  trustedUser.appendMacToTrust(context, e.Mac,e.trusted);
        		  } else {
        			  trustedUser.removeMacfromTrust(context, e.Mac);
        		  }
        		  orig.trusted = e.trusted;
        		  displayUsers();
    		  }	
    		  
              break;
			  }
		  }
    }  


    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        Log.i(TAG,"Menu Created");	
        return true;
    }
    
    /*
     * Used to act upon menu item selected. 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      Log.i(TAG,"buttonn selected. Item ID :" + item.getItemId());	
      
      switch (item.getItemId()) {
      case R.id.start_scan:
    	  //checks if it is already running.
    	  cd.write("Service Started");
    	  if(isServiceRunning(context,"uf.edu.ScanService")) {
    		  stopService(new Intent(this, ScanService.class));
    		  return true;
    	  } else if(isServiceRunning(context,"uf.edu.EnergyEfficientScanService")) {
    		  stopService(new Intent(this, EnergyEfficientScanService.class));
    		  return true;
    	  }
    	  else {
    		  //Log.i(TAG,"Starting Service");	

    		  if(prefs.getBoolean("EnergyEfficient", false))
    			  startService(new Intent(this, EnergyEfficientScanService.class));
    		  else
    			  startService(new Intent(this, ScanService.class));

    		  return true;
    	  }
      case R.id.refresh:
    	  		//call all the functions from trace parsing now.
    	  cd.write("Score Refreshed");
    	  if (locationUpdateInProgress == 1) {
    		  Toast.makeText(this, "Locations are being currently updated. Please try refreshing the scores later.", Toast.LENGTH_LONG).show();

    	  } else {
    		  if(refreshInProgress == true ) {
    			  Toast.makeText(this, "Score refresh in Progress", Toast.LENGTH_LONG).show();
    		  } else {
    			  Toast.makeText(this, "Refreshing the scores. It will take some time.", Toast.LENGTH_LONG).show();
    			  refreshInProgress  = true;
    			  new RefreshScore().execute();
    		  }
    	  }
    	  /*    	  
  		  startScoring();
  		  Log.i(TAG,"Scoring Done.");
  		  showItems = defaultItemsToShow;
  		  displayUserInOrder(SortingIndex);
  		  */
    	  return true; 
    	  
      case R.id.setWeighted:
    	  Toast.makeText(this, "Set Weight", Toast.LENGTH_LONG).show(); 
    	  weightDialog.show();
    	  showItems = defaultItemsToShow;
    	  return true;
    	  
    	  
      case R.id.sort:
    	  Log.i(TAG,"Sort menu clicked Sorting Index set at " + SortingIndex);
    	  builder.setSingleChoiceItems(items, SortingIndex, new DialogInterface.OnClickListener() {
       	      public void onClick(DialogInterface dialog, int item) {
       	          //Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
       	    	  SortingIndex = item;
       	    	  cd.write("Sorting Key Selected :" + item);
       	    	  Log.i(TAG,"Sorting Index selected:  " + SortingIndex);
       	          showItems = defaultItemsToShow;
       	          displayUserInOrder(item); 
       	          saveSortingIndex();
       	          alert.dismiss();
       	      }
       	    });
      	  alert = builder.create();
    	  alert.show();
    	  return true;
    	  
    	  
      case R.id.location:
    	  cd.write("Location updated");
    	  
    	  if(!isOnline()) {	
          	  Toast.makeText(context, "Please connect to internet !", Toast.LENGTH_LONG).show();
          	  return true;
        	}
    	  if(scoringInProgress == 1) {
        	  Toast.makeText(this, "Ecounter scores are being calculated currently, Please try location update later", Toast.LENGTH_LONG).show();
    	  } else {
    		  Toast.makeText(this, "Starting location updates.. will take some time", Toast.LENGTH_LONG).show();
    		  new updateLocations().execute();
    		  //updateLocation();
    	  }	  
    	  return true;
    	  
      case R.id.uploadEnc:  //for upload of data 

    	  if(isServiceRunning(context,"uf.edu.UploadService")) {
    		  Toast.makeText(this, "Uploading in Progress", Toast.LENGTH_LONG).show();
    		  return true;
    	  }	  
		  startService(new Intent(this, UploadService.class));

    	  
/*    	  if(onGoingUpload == false) {    	  
    		  onGoingUpload =true;
    		  new uploadData().execute();
    	  } else {
    		  Toast.makeText(this, "Upload in progress. please wait", Toast.LENGTH_LONG).show();
    	  }
    	 
  */  	  
    	  
    	  //AndroidPowerCollector apc = new AndroidPowerCollector();
    	  //apc.processApplicationUsage(TAG);
    	  return true;
      case R.id.register:
    	  //Start the activity.
    	  cd.write("Registration screen opened");
    	  Intent newIntent = new Intent(context, RegisterUser.class);
		  startActivity(newIntent);
		  return true;
		  
      case R.id.settings:
    	  cd.write("Settings screen opened");
    	  newIntent = new Intent(context, Settings.class);
		  startActivity(newIntent);
		  return true;
    	  
      case R.id.about:
    	  Intent newInt = new Intent(context, AboutiTrust.class);
    	  startActivity(newInt);
    	  return true;
    	  
      case R.id.selfstat:
    	   
    	  newIntent = new Intent(context, SelfStats.class);
    	  startActivity(newIntent);
    	  return true;

      case R.id.help:
    	  Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://128.227.176.22:8182/iTrust.html"));
    	  startActivity(browserIntent);
      default:
    	  return super.onContextItemSelected(item);
      }
    }

    public void saveSortingIndex() {
    	SharedPreferences.Editor ed = prefs.edit();
    	ed.putInt("SortingIndex", SortingIndex);
    	ed.commit();
    }
    public void updateLocation() {    	
    	alp.updateAddress();
    	Log.i(TAG,"Locations updated");
    	alp.printMapping();
    	alp.saveCurrent();
    }
    
    public boolean isOnline() {
    	 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	  if ( cm.getActiveNetworkInfo() == null)  {
    		  	return false;
    	  }	  	
    	
    	  try {
              HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
              urlc.setRequestProperty("User-Agent", "Test");
              urlc.setRequestProperty("Connection", "close");
              urlc.setConnectTimeout(2500); 
              urlc.connect();
              return (urlc.getResponseCode() == 200);
          } catch (IOException e) {
              Log.e(TAG, "Error checking internet connection", e);
          }
    	  return false;
    	}

    
    public void startScoring() {
    	
    	//check if sd card is available or not 
    	
    	//TODO: remove all system.out.println to Log.i kind of debug output.
    	//TODO: read scan time interval from the configuration file.
    	//TODO: read file names & ratio from the configuration file
    	//now create the objects
    	File file1 = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/"+ "parsedDataW");
    	File file2 = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/"+ "parsedDataBW");
    	try {
    		file1.delete();
    		file2.delete();
    	} catch (Exception e) {
    		Log.i(TAG,"startScoring exception "+ e);
    	}
    	//TODO: 
    	
    	Log.i(TAG,"Starting parsing the Wireless file");
		try{
			alp.parseNew("scannedDataW","parsedDataW",0,20); //only wifi trace - parsed
		}
		catch (Exception e) {
			Log.i(TAG, "APTest exception:" + e);
		}
		Log.i(TAG,"Starting parsing the Bluetooth file");
		//merge bluetooth file with wireless parsed data.
		try{
			MergeBlueWifi.mergeBlueWifi(context, "scannedDataB","parsedDataW","parsedDataBW",300); //blue and wifi both parsed
		}
		catch (Exception e) {
			System.out.println("mergebluewifi exception:" + e);
		}

		Log.i(TAG,"Starting to update encounters");
		encmanage.updateEncounters("parsedDataBW","parsedDataW", 0);
		
		encmanage.calCombScore( filterWeight[0], filterWeight[1], filterWeight[2], filterWeight[3]);
		toSave = true;
		Log.i(TAG,"Scores combined");
		//encmanage.printAll();
		moveFilesToZIP();	
    }
    
    //move already send files to backup.
    void moveFilesToBackup() {
		String[] file = new String[] {"ZIPscannedDataW","ZIPscannedDataB"};
		for (int j=0;j<2;j++) {
			File filei = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/" + file[j]);
			File fileo = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/"+"Old" + file[j].substring(3));
			FileChannel fos;
			FileChannel fis;
			try {
				fos = new FileOutputStream(fileo,true).getChannel();
				fis = new FileInputStream(filei).getChannel();
				fos.transferFrom(fis, fos.size(), fis.size());
				fis.close();
				fos.close();
				filei.delete();

			} catch (Exception e) {
				Log.i(TAG, "Exception thrown in back up file handler creation" + e);
			}
		}
    }
    //already processed files are to be send out 
    void moveFilesToZIP() {
    	int running = 0;
		if(isServiceRunning(context,"uf.edu.ScanService")) {
            stopService(new Intent(this, ScanService.class));
  		  	running = 1;
    	}
		String[] file = new String[] {"scannedDataW","scannedDataB"};
		for (int j=0;j<2;j++) {
			File filei = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/" + file[j]);
			File fileo = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/"+"ZIP" + file[j]);
			FileChannel fos;
			FileChannel fis;
			try {
				fos = new FileOutputStream(fileo,true).getChannel();
				fis = new FileInputStream(filei).getChannel();
				fos.transferFrom(fis, fos.size(), fis.size());
				fis.close();
				fos.close();
				filei.delete();

			} catch (Exception e) {
				Log.i(TAG, "Exception thrown in back up file handler creation" + e);
			}
		}
		//move the file
		if(running == 1) {
  		  startService(new Intent(this, ScanService.class));
		}
    }
    
    /*
     * This method should be used as default to fill the list view.
     */
    public void displayUserInOrder(int Index) {    	
    	//clear the list 
		lv_arr = new ArrayList<String>();
		//TODO: Do we need to sort everytime? what if we have not refreshed the scores and sorting index is also the same? 
		encmanage.sort(Index);
		displayUsers();
    }
    
    public void displayUsers() {    	
    	//clear the list 
		lv_arr = new ArrayList<String>();
		if (SortingIndex == 6)  { // show current encounters
			if(encmanage.sorted_encUser == null)
				encmanage.sort(0);
			for (Entry<String, EncUser> entry : encmanage.sorted_encUser.entrySet()){
				//if user is trusted put + else put /
				if(((EncUser)entry.getValue()).trusted > 0) {    
					if(RecentEncounterMAC.contains((String)entry.getKey()) == true) { //if recently encountered
						lv_arr.add(((String)entry.getKey() + "+" + ((EncUser)entry.getValue()).Name));
						//Log.i(TAG,((String)entry.getKey() + "+" + ((EncUser)entry.getValue()).Name));
					}
				} else if  (((EncUser)entry.getValue()).trusted < 0) {    
					if(RecentEncounterMAC.contains((String)entry.getKey()) == true) { //if recently encountered
						lv_arr.add(((String)entry.getKey() + "*" + ((EncUser)entry.getValue()).Name));
						//Log.i(TAG,((String)entry.getKey() + "+" + ((EncUser)entry.getValue()).Name));
					}
				} else {  //for non-trusted users.
					if(RecentEncounterMAC.contains((String)entry.getKey()) == true) { //if recently encountered
						lv_arr.add(((String)entry.getKey() + "/" + ((EncUser)entry.getValue()).Name));
						//Log.i(TAG,((String)entry.getKey() + "/" + ((EncUser)entry.getValue()).Name));
					} 
				
				} 
			}
		} else { // for Index 0 to 5
			int counter=0;    	
			//EncUser user = null;
			for (Entry<String, EncUser> entry : encmanage.sorted_encUser.entrySet()){
				//if user is trusted put + else put /
				if(((EncUser)entry.getValue()).trusted > 0) {    //trusted 
					if(RecentEncounterMAC.contains((String)entry.getKey()) == true) { //if recently encountered
						lv_arr.add(((String)entry.getKey() + "+" + ((EncUser)entry.getValue()).Name));
						//Log.i(TAG,((String)entry.getKey() + "+" + ((EncUser)entry.getValue()).Name));
					} else {
						lv_arr.add(((String)entry.getKey() + "-" + ((EncUser)entry.getValue()).Name)); //trusted but not encountered recently
					}
				} else if (((EncUser)entry.getValue()).trusted < 0) {    //untrusted
					if(RecentEncounterMAC.contains((String)entry.getKey()) == true) { //if recently encountered
						lv_arr.add(((String)entry.getKey() + "*" + ((EncUser)entry.getValue()).Name));
						//Log.i(TAG,((String)entry.getKey() + "+" + ((EncUser)entry.getValue()).Name));
					} else {
						lv_arr.add(((String)entry.getKey() + "&" + ((EncUser)entry.getValue()).Name)); //trusted but not encountered recently
					}
				}
				else {  //for trusted unknown.
					if(RecentEncounterMAC.contains((String)entry.getKey()) == true) { //if recently encountered
						lv_arr.add(((String)entry.getKey() + "/" + ((EncUser)entry.getValue()).Name));
						//Log.i(TAG,((String)entry.getKey() + "/" + ((EncUser)entry.getValue()).Name));
					} else {
						lv_arr.add(((String)entry.getKey() + "|" + ((EncUser)entry.getValue()).Name)); //not trusted not encountered
						//Log.i(TAG,((String)entry.getKey() + "|" + ((EncUser)entry.getValue()).Name));
					}

				}
				//Log.i(TAG,"MAC: "+(String)entry.getKey());
				counter ++;
				if(counter >=showItems) {
					break;
				}
			}
		}
		lvmain.setAdapter(new ListAdapter(context, R.layout.list_view,lv_arr));
    } 
    
  
    
    
    /*
     * Checks if the scan service is running by reading the list of all running applications.
     */
    public static boolean isServiceRunning(Context context, String service) {

	    Log.i(TAG, "Checking if service is running");

	    ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

	    List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

	    boolean isServiceFound = false;

	    for (int i = 0; i < services.size(); i++) {
	    	//Log.i(TAG, "service found: "+services.get(i).service.getClassName());
	        
	            if (service.equals(services.get(i).service.getClassName())){
	                isServiceFound = true;
	            }	        
	    }

	    Log.i(TAG, "Service was" + (isServiceFound ? "" : " not") + " running");

	    return isServiceFound;

	}
    

    @Override
    protected void onPause() {
    	super.onPause();
    	//alp.saveCurrent();
    	if(toSave) {
    		encmanage.save();
    		toSave = false;
    	}	
		saveWeights();
		saveSortingIndex();
		this.unregisterReceiver(ScanResults);
   	    
    }
	@Override
	protected void onStop() {
		super.onStop();
		//alp.saveCurrent();
		if(toSave) {
    		encmanage.save();
    		toSave = false;
    	}	
		saveWeights();
		saveSortingIndex();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cd.closeFile();
		if(toSave) {
    		encmanage.save();
    		toSave = false;
    	}	
		saveWeights();
		saveSortingIndex();
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

	    // Check current message count
		MenuItem mi = (MenuItem) menu.findItem(R.id.start_scan);
		
		if(isServiceRunning(context,"uf.edu.ScanService") || isServiceRunning(context,"uf.edu.EnergyEfficientScanService")) {
			 mi.setIcon(R.drawable.ic_menu_stop);
		     mi.setTitle(R.string.stopScanning);
     	} else {
     		mi.setIcon(R.drawable.ic_menu_start);
    		mi.setTitle(R.string.startScanning);
     	}
		
		mi = (MenuItem) menu.findItem(R.id.refresh);
		if (refreshInProgress==true) {
			 mi.setTitle(R.string.Refreshing);
		} else {
			mi.setTitle(R.string.refresh);
		}
	    return super.onPrepareOptionsMenu(menu);
	}

	private final BroadcastReceiver ScanResults = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent){
			if (intent.getAction().equals(ScanService.CUSTOM_INTENT)) {
				//TODO: check if the list of encountered users has changed. only then refresh the list.
				//TODO: instead of using shared preferences use bundle to transfer latest encounter data between the scan service and itrust
				Log.d(TAG,"ScanResult. intent received");
		    	getCurrentEncounters();
	    	    displayUsers();		
	       }
				
			
		}
	};
	
	
	public static iTrust getInstance() {
		return instance_;
	}
	
	
	// to refresh score in background
	private class RefreshScore extends AsyncTask<Object, Object, Object> {
	    /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected Object doInBackground(Object... arg0) {
	    		scoringInProgress=1;
	    		startScoring();
	  		  return arg0;
	    }
	    
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Object abc) {
	    	  Log.i(TAG,"Scoring Done.");
	  		  showItems = defaultItemsToShow;
	  		  displayUserInOrder(SortingIndex);
	  		  scoringInProgress=0;
	  		  refreshInProgress = false;
	  		  SharedPreferences.Editor ed = prefs.edit();
	  		  ed.putLong("scoreRefreshDate", System.currentTimeMillis()/1000 );
	  		  ed.commit();
	    	  Log.i(TAG,"Scores Updated");
	    }
	}
	
	// to refresh locations in background
	private class updateLocations extends AsyncTask<Object, Object, Object> {
	    /** The system calls this to perform work in a worker thread and
	      * delivers it the parameters given to AsyncTask.execute() */
	    protected Object doInBackground(Object... arg0) {
	    	  locationUpdateInProgress =1 ;
	    	  updateLocation();
	  		  return arg0;
	    }
	    /** The system calls this to perform work in the UI thread and delivers
	      * the result from doInBackground() */
	    protected void onPostExecute(Object abc) {
	    	  locationUpdateInProgress =0 ;
	    	  Log.i(TAG,"Locations Updated");
	    }
	    
	  
	}
	
//	// to upload data to remote servers in background
//		private class uploadData extends AsyncTask<Object, Object, Boolean> {
//		    /** The system calls this to perform work in a worker thread and
//		      * delivers it the parameters given to AsyncTask.execute() */
//			protected Boolean doInBackground(Object... arg0) {
//				//TODO: algo 1. Zip, 2. upload, 3. if upload successful.. move the contents to the backup file
//				boolean uploadSuccess;
//				String address = prefs.getString("BLUEMAC", "UNKNOWN");
//				String filename = address.replace(':', '_')+"-"+System.currentTimeMillis()+".zip";    	        		
//				zip z = new zip (context);
//				z.zipFolder(filename);
//				uploadTraces ut = new uploadTraces(context);
//				uploadSuccess = ut.sendFile(Environment.getExternalStorageDirectory()+"/"+getString(R.string.DataPath)+"/"+filename); 
//				File filei = new File(Environment.getExternalStorageDirectory(), getString(R.string.DataPath)+"/" + filename);
//				filei.delete();
//		    	Log.i(TAG,"Upload successful="+uploadSuccess);
//				return uploadSuccess;
//		    }
//		    /** The system calls this to perform work in the UI thread and delivers
//		      * the result from doInBackground() */
//		    protected void onPostExecute(Boolean success) {		    		
//		    	  if (success == true) {
//		          	  Toast.makeText(context, "Upload successful", Toast.LENGTH_LONG).show(); 
//			    	  Log.i(TAG,"Upload successful");
//
//		    	  } else {
//		          	  Toast.makeText(context, "Upload Failed!", Toast.LENGTH_LONG).show(); 
//			    	  Log.i(TAG,"Upload failed");
//
//		    	  }	  
//		    	  onGoingUpload = false;
//		    }
//		    
//		  
//		}
//		
//	
//TODO:remove debug option from manifest file.    
//TODO: create a first startup file that opens the bluetooth device and writes the mac address to the shared preferences	
}   
  



