package com.cebu;



import java.io.IOException;
import com.google.android.gcm.GCMRegistrar;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.datatype.DatatypeConstants.Field;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import com.cebu.R;
import com.cebu.LocationService.LocalBinder;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PorterDuff;


public class CebuActivity extends Activity {
	String TAG = "CebuActivity";

	static final String DISPLAY_MESSAGE_ACTION =
            "com.cebu.	";
	
	public static String apiRequestUrl = "http://cebutraffic.org";
	
	public static String mapboxTilesUrl = "http://a.tiles.mapbox.com/v3/openplans.map-g4j0dszr/";
	
	public static String overlayTilesUrl = "http://cebutraffic.org/tiles/";

	private Boolean panicMode = false;

	private ProgressBar spinner;
	public LocationService locationService;
	private AutoCompleteTextView vehicleField;
	private TextView driverField;
	private EditText msgSub;
	//private String imeiNumber;
	private String userVehicleID;
	private String userDriverNo;
	private String operator;
	private int operatorid;
	private int gpsInterval;
	private int updateInterval;			        		
	private String driverName;
	ProgressDialog dialog;
	private MapView myMap;
	private MyLocationOverlay myLocOverlay;
	private TilesOverlay mTilesOverlay;
	private MapTileProviderBasic mProvider;
	private MapTileProviderBasic mOverlayProvider;
	private ITileSource mCustomTileSource;
	private ITileSource mOverlayTileSource;
	private GeoPoint startLocation;
	private int loginScreeninvoker=0;//0--not known --//1--first time login ---//2--from settings screen
	LocationListener mlocListener;
	ArrayList<Operators> Operatorsarrylist = new ArrayList<Operators>();
	ArrayList<String> oprators;
	String promptOper="";
	public static Context context;
	private LocationServiceConnection serviceConnection;
	
	private Handler mHandler = new Handler();
	
	/*final int mapOverlayUpdatePeriod = 30000;
	final Runnable messageRetrieveTask = new Runnable() {
	    public void run() { 
	    	
	    	//mOverlayProvider.clearTileCache();
	        //myMap.invalidate();	    	
	    	//mHandler.postDelayed(this, mapOverlayUpdatePeriod);
	    	
	    	String url= apiRequestUrl + "/api/messages?imei=" + Utils.imeino(getApplicationContext());

	    	try 
	    	{  	HttpClient httpclient = new DefaultHttpClient();				
				HttpGet httpgetrqst = new HttpGet(url);
				HttpResponse response;			
				response = httpclient.execute(httpgetrqst);
				HttpEntity entity = response.getEntity();
				int rspCode=response.getStatusLine().getStatusCode();
				
				if(rspCode==200)
				{
					String json = EntityUtils.toString(entity);
					JSONArray jArray2;
					try
					{
						String message = "";
						jArray2 = new JSONArray(json);
						for(int i=0;i<jArray2.length();i++)
						{
							try{
							JSONObject json_data = jArray2.getJSONObject(i);
								//if (json_data.getString("type").equalsIgnoreCase("dispatch"))
								{	
									message +="---"+json_data.getString("body");
									message += json_data.getString("timestamp"); 								
									
								}
							
							}catch(Exception e){
							}
							message += "\n\n\n";
							
						}
						
						
						if (message != "")
						{
							final Dialog dialog = new Dialog(CebuActivity.this);
						
							dialog.setContentView(R.layout.message_activity);
							dialog.setTitle("Message Received");
							dialog.setCancelable(false);
							Button dialogButton = (Button) dialog.findViewById(R.id.messageOk);
							
							TextView text = (TextView) dialog.findViewById(R.id.textView1);
							text.setText(message);
						
							dialogButton.setOnClickListener(new OnClickListener() {
								public void onClick(View v) {
									dialog.dismiss();
								}
							});
							dialog.show();
						}
						}
					catch(Exception e)
					{
						// json parse exception
						e.printStackTrace();
					}
				}
				else
				{
					toast("error response from server.");
				}			
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
	    	mHandler.postDelayed(messageRetrieveTask, mapOverlayUpdatePeriod);

	    }
	};*/
	
	private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
      
        	AlertDialog.Builder builder = new AlertDialog.Builder(CebuActivity.this);
    		AlertDialog alert = builder.setMessage(intent.getExtras().get("message").toString()).setTitle("Message").setNegativeButton("Close", null).create();
    		alert.show();
        }
    };

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = this;
		
		registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
		
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
		  GCMRegistrar.register(this, "380262927280");
		} else {
		    Log.v(TAG, "Already registered");
		  	String postUrl = CebuActivity.apiRequestUrl + "/api/registerGcm";
			HttpClient client = Utils.getNewHttpClient();
			HttpPost request = new HttpPost(postUrl);
			
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
				
				nameValuePairs.add(new BasicNameValuePair("gcmKey", regId));
				nameValuePairs.add(new BasicNameValuePair("imei", Utils.imeino(getApplicationContext())));
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = client.execute(request);
			
			}
			catch(Exception e)
			{
				Log.i("failed to register gcm key", e.toString());
			}
		}
		
		//imeiNumber=Utils.imeino(getApplicationContext());	
		
		Intent intent = new Intent(CebuActivity.this, LocationService.class);
		startService(intent);
		serviceConnection = new LocationServiceConnection();
		bindService(intent, serviceConnection, 0);
		dialog = ProgressDialog.show(this, "", "Registering with server.", true);
	}


	private class LoginTask extends AsyncTask<Void, Void, String> {

		protected String doInBackground(Void... params) {
			// make login request, which really is just a GET request for
			// the ping URL

			String postUrl = apiRequestUrl + "/api/login";
			HttpClient client = Utils.getNewHttpClient();
			HttpPost request = new HttpPost(postUrl);

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
				String vehicleID = vehicleField.getText().toString();
				String driverNumber = driverField.getText().toString();
				nameValuePairs.add(new BasicNameValuePair("vehicle", vehicleID));
				nameValuePairs.add(new BasicNameValuePair("driver", driverNumber));
				nameValuePairs.add(new BasicNameValuePair("imei", Utils.imeino(getApplicationContext())));
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				userVehicleID = vehicleField.getText().toString();
				userDriverNo = driverField.getText().toString();

				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();

				String json = EntityUtils.toString(entity);
				if (!json.startsWith("{")) {
					// not really json
					toast("Got an unexpected response from the server.  Are you sure you have the URL right?");
					return null;
				}

				JSONTokener tokener = new JSONTokener(json);
				JSONObject obj = (JSONObject) tokener.nextValue();
				operator=obj.getString("name");
				operatorid=obj.getInt("id");
				gpsInterval=obj.getInt("gpsInterval");
				updateInterval=obj.getInt("updateInterval");			        		
				driverName=obj.getString("driverName");
				return "PARSE_COMPLETE";
			} catch (ClientProtocolException e) {
				Log.e(TAG, "exception logging in", e);
				toast("Error logging in.  Try again.");
			} catch (IOException e) {
				Log.e(TAG, "exception logging in", e);
				toast("Error logging in.  Try again.");
			} catch (JSONException e) {
				if(e.getMessage().contains("driverName"))
				{
					return "PARSE_COMPLETE";
				}
				Log.e(TAG, "exception logging in", e);
				toast("Error logging in.  Check your password and try again.");
			} catch (Exception e) {
				Log.e(TAG, "exception logging in", e);
				toast("Error logging in.");
			}

			return null;
		}

		protected void onPostExecute(String result) {
			if (result == null) {
				spinner.setIndeterminate(false);
				spinner.setVisibility(View.INVISIBLE);
				toast("Server Authentication failed.");
				return;
				//gpsInterval=5;
				//updateInterval=30;
				
			}
			if (driverName==null || driverName.equals(""))
			{
				UpdatePreferences("driverName", "");
			}
			else
			{
				UpdatePreferences("driverName", driverName);
			}

			UpdatePreferences("imeiNumber", Utils.imeino(getApplicationContext()));
			UpdatePreferences("vehicleID", userVehicleID);
			UpdatePreferences("driverID", userDriverNo);
			UpdatePreferences("pingInterval", updateInterval);
			UpdatePreferences("gpsInterval",gpsInterval);
			UpdatePreferences("operatorid",operatorid);
			UpdatePreferences("operatorname",operator);
			loggedIn();
			if (loginScreeninvoker==1)
			{
				Intent intent = new Intent(CebuActivity.this, LocationService.class);
				locationService.realStart(intent);				
			}
		}
	}

	void UpdatePreferences(String name,String value)
	{
		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplication());
		Editor editor = preferences.edit();
		editor.putString(name, value);
		editor.commit();		
	}

	void UpdatePreferences(String name,int value)
	{
		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplication());
		Editor editor = preferences.edit();
		editor.putInt(name, value);
		editor.commit();		
	}

	void setupUI() {		
				
		LocationManager mlocManager;
		mlocManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) 
		{
			if (locationService == null || locationService.getStatus().equals(LocationService.INACTIVE) || locationService.getStatus().equals(LocationService.NOT_STARTED)) {
				//the service is inactive or will shut down
				loginScreeninvoker=1;
				switchToLogin();
			} else {
				switchToRunning();
			}
		}
		else
			showGPSDisabledAlertToUser();
	}

	// this is called from the LocationService after its thread has been initialized and started.
	// The location update request has to be started from the UI thread (I am not sure why),
	// or no location updates are delivered to the LocationServiceThread instance!
	/*
	public void startLocation() {
		runOnUiThread(new Thread() {
			public void run() {
				locationService.startReceivingLocation();
			}
		});
	}
	 */



	class LocationServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get
			// LocalService instance
			LocalBinder binder = (LocalBinder) service;
			locationService = binder.getService();
			locationService.setActivity(CebuActivity.this);

			if (locationService == null || locationService.getStatus().equals(LocationService.INACTIVE) || locationService.getStatus().equals(LocationService.NOT_STARTED)) 
			{
				RegisterCebu tsk = new RegisterCebu();
				tsk.execute(Utils.imeino(getApplicationContext()));
			}
			else
			{
				dialog.dismiss();
				userVehicleID=locationService.userVehicleID;
				//this happens a bit after startup, so we need to call a callback to set up the UI
				runOnUiThread(new Thread() {
					public void run() {
						setupUI();
					}
				});				
			}

		}

		public void onServiceDisconnected(ComponentName arg0) {
			// notify the user
			toast("Location service disconnected");
		}

	}

	public void toast(final String text) {
		runOnUiThread(new Runnable() {
			public void run() {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
			}
		});
	}

	public void loggedIn() {
		spinner.setIndeterminate(false);
		spinner.setVisibility(View.INVISIBLE);

		/*
		 * Handle adding the user's email address to the frequently used email
		 * address list
		 */

		/*
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		String vehicleIDs = preferences.getString("vehicleids", null);
		userVehicleID = vehicleField.getText().toString();
		if (vehicleIDs == null) {
			vehicleIDs = userVehicleID;
		} else {
			String[] past_vehicles = vehicleIDs.split(",");
			if (!Arrays.asList(past_vehicles).contains(userVehicleID)) {
				vehicleIDs = vehicleIDs + "," + userVehicleID;
			}
		}
		Editor editor = preferences.edit();
		editor.putString("vehicleids", vehicleIDs);
		editor.commit();
		 */
		if(loginScreeninvoker==1)
		toast("Logged in");
		switchToRunning();
	}


	private class RegisterCebu extends AsyncTask<String, Void, String> 
	{
		boolean error;
		int rspCode;

		@Override
		protected String doInBackground(String... imeiNum) 
		{
			rspCode=-1;
			oprators=new ArrayList<String>();
			error=false;
			try 
			{
				HttpClient httpclient = new DefaultHttpClient();				
				String SITEURL = apiRequestUrl + "/api/operator?imei="+imeiNum[0];
				HttpGet httpgetrqst = new HttpGet(SITEURL);
				HttpResponse response = httpclient.execute(httpgetrqst);
				HttpEntity entity = response.getEntity();
				rspCode=response.getStatusLine().getStatusCode();
				if(rspCode==200)
				{
					if (entity != null) 
					{
						String json = EntityUtils.toString(entity);
					}
					promptOper="";
				}
				else if (rspCode ==401)
				{
					promptOper="YES";
					getOperatorsList();
				}

			} 
			catch (Exception e) 
			{
				error=true;
				e.printStackTrace();
			}				
			return "";
		}

		@Override
		protected void onPostExecute(String result) 
		{	
			dialog.dismiss();
			if(error==false && promptOper.length()>0)
			{
				switchToOperators();
			}
			else if(error==false && promptOper.length()<=0)
			{				
				setupUI();
			}
		}
	}

	// get list of operators for unkown IMEIs
	public void getOperatorsList() 
	{

		try 
		{
			HttpClient httpclient = new DefaultHttpClient();        
			HttpGet httpget = new HttpGet(apiRequestUrl + "/api/register"); 
			HttpResponse response;	        	
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) 
			{ 

				String json = EntityUtils.toString(entity);				
				JSONArray jArray2 = new JSONArray(json);
				oprators.add("Select an Operator...");
				for(int i=0;i<jArray2.length();i++)
				{
					JSONObject json_data = jArray2.getJSONObject(i);
					Operators oprt = new Operators(json_data.getInt("id"),json_data.getString("name"));
					oprators.add(oprt.getOperator_name());
					Operatorsarrylist.add(oprt);
				}	                                
			}

		} 
		catch(Exception e)
		{        	
		}		
	}


	private class UpdateOperator extends AsyncTask<String, Void, String> 
	{
		boolean error;
		int rspCode;

		@Override
		protected String doInBackground(String... url) 
		{
			rspCode=-1;
			error=false;				
			try 
			{
				String regURL;
				regURL= apiRequestUrl + "/api/register?operator="+(url[0])+"&imei="+ Utils.imeino(getApplicationContext());
				HttpClient httpclient = new DefaultHttpClient();        
				HttpPost httppost = new HttpPost(regURL); 
				HttpResponse response = httpclient.execute(httppost);	            
				regURL = apiRequestUrl + "/api/operator?imei="+Utils.imeino(getApplicationContext());
				HttpGet httpgetrqst = new HttpGet(regURL);
				response = httpclient.execute(httpgetrqst);
				rspCode=response.getStatusLine().getStatusCode();
			}
			catch(Exception e)
			{
				error=true;
				e.printStackTrace();

			}									
			return "";
		}

		@Override
		protected void onPostExecute(String result) 
		{			
			dialog.dismiss();
			if(rspCode==200 && error==false)
			{
				//success case
				setupUI();
			}			
			else 
			{
				Toast.makeText(getBaseContext(),"Registration failed, contact administrator.",Toast.LENGTH_LONG).show();
				finish();
			}
		}


	}


	private void switchToOperators() {
		setContentView(R.layout.operator_dialog);
		Spinner spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item,oprators);
		spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down vieww
		spinner.setAdapter(spinnerArrayAdapter);

		View confirmButton = findViewById(R.id.saveButton);
		confirmButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				Spinner spinner = (Spinner) findViewById(R.id.spinner);
				int pos=spinner.getSelectedItemPosition();
				if (pos ==0)
				{
					Toast.makeText(getBaseContext(),"Select a valid operator to proceed.",Toast.LENGTH_LONG).show();					
				}
				else
				{
					try{
						UpdateOperator tsk= new UpdateOperator();
						String opId;
						opId=""+(Operatorsarrylist.get(pos-1).getOperator_id());
						dialog=ProgressDialog.show(v.getRootView().getContext(), "", "Update Operator details.");
						tsk.execute(opId);						
					}
					catch(Exception e){
						e.printStackTrace();						
					}
				}			
			}
		});

		View cacelButton = findViewById(R.id.cancelButton);
		cacelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}



	private void switchToLogin() {
		setContentView(R.layout.login);

		String[] past_vehicles  = new String[0];
		vehicleField = (AutoCompleteTextView) findViewById(R.id.vehicleField);
		driverField = (TextView) findViewById(R.id.driverIdField);


		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list_item, past_vehicles);
		vehicleField.setAdapter(adapter);
		spinner = (ProgressBar) findViewById(R.id.loginProgressBar);

		if(loginScreeninvoker==2)
		{
			SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplication());
			userVehicleID =  preferences.getString("vehicleID", "");
			userDriverNo =  preferences.getString("driverID", "");
			vehicleField.setText(userVehicleID);
			driverField.setText(userDriverNo);
		}

		View loginButton = findViewById(R.id.loginButton);
		if(loginScreeninvoker==2)
		{
			Button testButton = (Button) loginButton;
			testButton.setText("Update");
		}		
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(validatefields(vehicleField.getText().toString(),driverField.getText().toString()))
				{
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(vehicleField.getWindowToken(), 0);
					imm.hideSoftInputFromWindow(driverField.getWindowToken(), 0);											
					spinner.setIndeterminate(true);
					spinner.setVisibility(View.VISIBLE);
					new LoginTask().execute();
				}
			}
		});

		View configButton = findViewById(R.id.clearButton);
		if(loginScreeninvoker==2)
		{
			Button testButton = (Button) configButton;
			testButton.setText("Back");
		}
		configButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(vehicleField.getWindowToken(), 0);
				imm.hideSoftInputFromWindow(driverField.getWindowToken(), 0);
				vehicleField.setText("");
				driverField.setText("");
				if(loginScreeninvoker==2)
				{
					loggedIn();
				}
			}
		});

		/*		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(true);
		criteria.setSpeedRequired(true);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_HIGH);			
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		String bestProvider= locationManager.getBestProvider(criteria,true);			 			
		mlocListener = new MyLocation(getApplicationContext());
		locationManager.requestLocationUpdates(bestProvider, 0, 0, mlocListener);
		int gpsinmillisec=gpsInterval*1000;
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, gpsinmillisec, 0, mlocListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsinmillisec, 0, mlocListener);		
		 */		

	}

	// Code to validate fields
	private boolean validatefields(String vehicleid, String driverid) {
		if (vehicleid.length() == 0) {
			toast("Please enter Vehicle Body Number ");
			return false;
		}
		if (driverid.length() == 0) {
			toast("Please enter Driver ID");
			return false;
		}

		return true;
	}

	private void switchToRunning() {
		setContentView(R.layout.running);
		
		if(userVehicleID==null || userVehicleID.length()<=0)
		{
			SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplication());
			userVehicleID =  preferences.getString("vehicleID", "");			
		}
		
		TextView userField = (TextView) findViewById(R.id.loggedInAsLabel);
		userField.setText("Logged in as " + userVehicleID);
		toast("Logged in as " + userVehicleID);
		try{
			setMap();
			initMyLocation();
			/*
			final Button logoutButton = (Button) findViewById(R.id.logoutButton);
			logoutButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					locationService.setStatus(LocationService.INACTIVE, false);
					//stopService(new Intent(CebuActivity.this, LocationService.class));
					finish();
				}
			});		
			*/
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public void info(View v) 
	{
		loginScreeninvoker=2;
		switchToLogin();
	}

	
	public void centreMap(View v)
	{
		try{
			myMap.getController().setCenter(myLocOverlay.getMyLocation());
		}catch(Exception e){
			
		}
	}
	
	public void alert(View v) 
	{
		try {
			String url= apiRequestUrl + "/api/alerts?imei="+Utils.imeino(getApplicationContext());
			HttpClient httpclient = new DefaultHttpClient();				
			HttpGet httpgetrqst = new HttpGet(url);
			HttpResponse response;			
			response = httpclient.execute(httpgetrqst);
			HttpEntity entity = response.getEntity();
			int rspCode=response.getStatusLine().getStatusCode();
			if(rspCode==200)
			{
				String json = EntityUtils.toString(entity);
				JSONArray jArray2;
				try {
					String message = "";
					jArray2 = new JSONArray(json);
					for(int i=0;i<jArray2.length();i++)
					{
						try{
						JSONObject json_data = jArray2.getJSONObject(i);
							//if (json_data.getString("type").equalsIgnoreCase("dispatch"))
							{	
								message += json_data.getString("title"); 
								message +="---"+json_data.getString("timestamp");
								message +="---"+json_data.getString("description");
								message += "\n\n\n";
							}
						
						}catch(Exception e){
						}
						
					}
					// custom dialog
					final Dialog dialog = new Dialog(context);
					dialog.setContentView(R.layout.custom);
					dialog.setTitle("Alerts");
					dialog.setCancelable(false);
					// set the custom dialog components - text, image and button
					TextView text = (TextView) dialog.findViewById(R.id.text);
					text.setText(message);
					Button dialogButton = (Button) dialog.findViewById(R.id.ok);
					// if button is clicked, close the custom dialog
					dialogButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					dialog.show();													
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			else
			{
				toast("error response from server.");
			}			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//toast("Alert Clicked");
	}

	public void panicToggle(View v) 
	{
		View panicButton = findViewById(R.id.panicButton);
		
		if(panicMode)
		{
			panicMode = false;
			
			runOnUiThread(new Runnable() {
		         public void run() {

		        	String url= apiRequestUrl + "/api/panic?imei="+Utils.imeino(getApplicationContext())+"&panic=false";
		 			HttpClient httpclient = new DefaultHttpClient();        
		 			HttpPost httppost = new HttpPost(url); 
		 			try
		 			{
		 				HttpResponse response = httpclient.execute(httppost);	
		 			
		 				
		 				toast("Panic alert deactivated.");
		 			}	
		 			catch(Exception e)
		 			{
		 				// failed panic update
		 			}

		        	 
		         }});
			
			panicButton.getBackground().clearColorFilter();
						
			
		}
		else
		{
			panicMode = true;
			
			runOnUiThread(new Runnable() {
		         public void run() {

		        	String url= apiRequestUrl + "/api/panic?imei="+Utils.imeino(getApplicationContext())+"&panic=true";
		 			HttpClient httpclient = new DefaultHttpClient();        
		 			HttpPost httppost = new HttpPost(url); 
		 			try
		 			{
		 				HttpResponse response = httpclient.execute(httppost);	
		 			
		 				
		 				toast("Panic alert activated.");
		 			}	
		 			catch(Exception e)
		 			{
		 				// failed panic update
		 			}

		        	 
		         }});
			
			panicButton.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
		
		}
	}
	
	public void message(View v) 
	{
		try {
			
					// custom dialog
					final Dialog dialog = new Dialog(context);
					
					//Intent intent = new Intent(Intent.ACTION_VIEW);
					//intent.setClassName(this, MessageActivity.class.getName());
					//this.startActivity(intent);
					dialog.setContentView(R.layout.message);
					dialog.setTitle("Send Message");
					dialog.setCancelable(false);
					// set the custom dialog components - text, image and button
					//TextView text = (TextView) dialog.findViewById(R.id.messageText);
					//text.setText(message);
					Button dialogButton = (Button) dialog.findViewById(R.id.cancelMessage);
					// if button is clicked, close the custom dialog
					dialogButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
		 
					Button submitButton = (Button) dialog.findViewById(R.id.sendMessageButton);
					// if button is clicked, send message and close

					msgSub = (EditText)dialog.findViewById(R.id.messageText);
					submitButton.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							
							try{								
								if (msgSub.getText().toString().length() == 0) {
									toast("Please enter a message t submit ");
								}								
							}catch(Exception e){
								e.printStackTrace();
							}
							if (msgSub.getText().toString().length() == 0) {
								toast("Please enter a message t submit ");
							}
							else
							{
								try{
								//////////////////
								String postUrl = apiRequestUrl + "/api/messages";
								HttpClient client = Utils.getNewHttpClient();
								HttpPost request = new HttpPost(postUrl);
								List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
								nameValuePairs.add(new BasicNameValuePair("imei",Utils.imeino(getApplicationContext())));
								//nameValuePairs.add(new BasicNameValuePair("imei", imeiNumber));
								String lat="";
								String lon="";								
								if(locationService!=null)
								{
									Location loc=locationService.lastLocation;
									if(loc!=null){
										lat=""+loc.getLatitude();
										lon=""+loc.getLongitude();
										nameValuePairs.add(new BasicNameValuePair("lat",lat));
										nameValuePairs.add(new BasicNameValuePair("lon", lon));										
									}
								}
								nameValuePairs.add(new BasicNameValuePair("content", msgSub.getText().toString()));
								request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
								HttpResponse response = client.execute(request);
								HttpEntity entity = response.getEntity();
								toast("Message sent.");
								
								msgSub.setText("");
								dialog.dismiss();
								}catch(Exception e){
									e.printStackTrace();
								}
								
								
							}

						}
					});

					dialog.show();													
					
				} catch (Exception e) {
					e.printStackTrace();
			
				}
		
		
				//toast("Alert Clicked");
	}

	public void flood(View v) 
	{
		try {
			String url=  apiRequestUrl + "/api/alerts?imei="+Utils.imeino(getApplicationContext());
			HttpClient httpclient = new DefaultHttpClient();				
			HttpGet httpgetrqst = new HttpGet(url);
			HttpResponse response;			
			response = httpclient.execute(httpgetrqst);
			HttpEntity entity = response.getEntity();
			int rspCode=response.getStatusLine().getStatusCode();
			if(rspCode==200)
			{
				String json = EntityUtils.toString(entity);
				JSONArray jArray2;
				try {									
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			else
			{
				toast("error response from server.");
			}			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		//toast("Alert Clicked");
	}

	public void incident(View v) 
	{
		try {
			String url= apiRequestUrl + "/api/alerts?imei="+Utils.imeino(getApplicationContext());
			HttpClient httpclient = new DefaultHttpClient();				
			HttpGet httpgetrqst = new HttpGet(url);
			HttpResponse response;			
			response = httpclient.execute(httpgetrqst);
			HttpEntity entity = response.getEntity();
			int rspCode=response.getStatusLine().getStatusCode();
			if(rspCode==200)
			{
				String json = EntityUtils.toString(entity);
				JSONArray jArray2;
			}
			else
			{
				toast("error response from server.");
			}			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		//toast("Alert Clicked");
	}

	
	
	public static double round(double unrounded, int precision, int roundingMode)
	{
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}

	private void initMyLocation() {
		
        
        //this.mCustomTileSource = new XYTileSource("FietsRegionaal", null, 3, 18, 256, ".png",
        //                "http://overlay.openstreetmap.nl/openfietskaart-rcn/");
		try{
		
		this.mOverlayProvider = new MapTileProviderBasic(getApplicationContext());
        
			
		this.mCustomTileSource = new XYTileSource("Openplans", null, 0, 20, 256, ".png",
                mapboxTilesUrl);
        myMap.setTileSource(mCustomTileSource);	
        
        //this.mOverlayTileSource = new XYTileSource("Overlay", null, 0, 20, 256, ".png",
        //		 overlayTilesUrl);
        
        // this.mOverlayProvider.setTileSource(this.mOverlayTileSource);
        //mOverlayProvider.clearTileCache();   
        //this.mTilesOverlay = new TilesOverlay(mOverlayProvider, this.getBaseContext());
        
        //myMap.getOverlays().add(this.mTilesOverlay);
     
    	//mHandler.postDelayed(messageRetrieveTask, mapOverlayUpdatePeriod);
        
        myLocOverlay = new MyLocationOverlay(this, myMap);
        myLocOverlay.enableMyLocation();
        
        //myLocOverlay.enableFollowLocation();
        //myLocOverlay.disableFollowLocation();
        
		myMap.getOverlays().add(myLocOverlay);
		
		runOnUiThread(new Runnable() {
	         public void run() {
	        	 myMap.getController().animateTo(myLocOverlay.getMyLocation());
	        	 myMap.getController().setCenter(myLocOverlay.getMyLocation());
	            } 
	        });
        
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	

	
	public void setMap() 
	{
		MapView map = (MapView) findViewById(R.id.mapview);
		//map.setTileSource(TileSourceFactory.MAPNIK);// MAPNIK
		map.setBuiltInZoomControls(true);
		map.getController().setZoom(14);
		LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location;
		
		
		Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
         criteria.setAltitudeRequired(false);
         criteria.setBearingRequired(false);
         criteria.setCostAllowed(true);
         String strLocationProvider = mlocManager.getBestProvider(criteria, true);		
        location = mlocManager.getLastKnownLocation(strLocationProvider);
		//location = mlocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		try
		{

			if(location==null)
			{
				location=mlocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if(location!=null)
				{
					double lat=round(((double) (location.getLatitude())),5,BigDecimal.ROUND_HALF_UP);
					double lon=round(((double) (location.getLongitude())),5,BigDecimal.ROUND_HALF_UP);
					startLocation=new GeoPoint(lat,lon);
					//startLocation=new GeoPoint(11.23730,-236.01090);
					//startLocation=new GeoPoint(10.315699,123.885437);
					map.getController().setCenter(startLocation);						
				}
			}
			else
			{
				double lat=round(((double) (location.getLatitude())),5,BigDecimal.ROUND_HALF_UP);
				double lon=round(((double) (location.getLongitude())),5,BigDecimal.ROUND_HALF_UP);
				startLocation=new GeoPoint(lat,lon);
				//startLocation=new GeoPoint(10.315699,123.885437);
				//startLocation=new GeoPoint(11.23730,-236.01090);
				map.getController().setCenter(startLocation);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		myMap = map;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}



	/* configuration dialog callbacks */
	public String getServerUrl() {
		return apiRequestUrl;
	}

	public int getPingInterval() {
		return updateInterval;
	}

	public void setConfig(String serverUrl, int pingInterval) {
		apiRequestUrl = serverUrl;
		this.updateInterval = pingInterval;
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString("apiRequestUrl", apiRequestUrl);
		editor.putInt("pingInterval", pingInterval);
		editor.commit();
	}

	private void showGPSDisabledAlertToUser() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder
		.setMessage(
				"GPS is disabled in your device. Would you like to enable it?")
				.setCancelable(false)
				.setPositiveButton("Goto Settings Page To Enable GPS",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent callGPSSettingIntent = new Intent(
								android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(callGPSSettingIntent);
						finish();
					}
				});
		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();	
	}

	public class Operators {

		int operator_id;
		String Operator_name;

		public Operators(int operatorId,String OperatorName)
		{
			this.operator_id = operatorId;
			this.Operator_name = OperatorName;
		}

		public int getOperator_id() {
			return operator_id;
		}

		public void setOperator_id(int operator_id) {
			this.operator_id = operator_id;
		}

		public String getOperator_name() {
			return Operator_name;
		}

		public void setOperator_name(String operator_name) {
			Operator_name = operator_name;
		}
	}
}