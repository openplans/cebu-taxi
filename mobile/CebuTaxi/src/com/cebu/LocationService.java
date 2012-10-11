package com.cebu;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import com.cebu.R;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationService extends Service {

	public static final String INACTIVE = "inactive";
	public static final String BREAK = "break";
	public static final String ACTIVE = "active";
	public static final String NOT_STARTED = "not started";
	private LocationServiceThread thread;
	private LocalBinder binder = new LocalBinder();
	private CebuActivity activity;
	public Location lastLocation;
	private int gpsInterval;
	private String imeiNumber;
	public String userVehicleID;
	private String userDriverNo;
	private String operator;
	private int operatorid;
	private int updateInterval;			        		
	private String driverName;
	public boolean UpdateSet=false;
	private boolean readPreferences=false;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		getPreferenceValues();
		if( (!imeiNumber.equals("")) && operatorid!=-1)
		{
			readPreferences=true;
			realStart(intent);
		}
		return START_REDELIVER_INTENT;
	}

	public void getPreferenceValues()
	{
		SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(getApplication());
		imeiNumber = preferences.getString("imeiNumber", "");		
		driverName = preferences.getString("driverName", "");
		userVehicleID =  preferences.getString("vehicleID", "");
		userDriverNo =  preferences.getString("driverID", "");		
		operator =  preferences.getString("operatorname", "");		
		updateInterval = preferences.getInt("pingInterval", 30);
		operatorid = preferences.getInt("operatorid",-1);	
		gpsInterval = preferences.getInt("gpsInterval", 5);				
	}
	public void realStart(Intent intent) {
		if(readPreferences==false)
			getPreferenceValues();
		Notification notification = new Notification(R.drawable.icon, "Cebu Traffic", System.currentTimeMillis());
		Intent appIntent = new Intent(this, CebuActivity.class);
		appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(this, 0, appIntent, 0);
		notification.setLatestEventInfo(this, "Cebu Traffic", "connected", pi);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		startForeground(66786, notification);
		thread = new LocationServiceThread(imeiNumber);
		setStatus(LocationService.ACTIVE, true);
		new Thread(thread).start();
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		thread.stop();
	}

	public class LocalBinder extends Binder {
		LocationService getService() {
			// Return this instance of LocalService so clients can call public methods
			return LocationService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public void setActivity(CebuActivity activity) {
		this.activity = activity;
	}

	public void setStatus(String status, boolean active) {
		thread.setActive(active);
		thread.setStatus(status);
	}

	public String getStatus() {
		if (thread == null) {
			return NOT_STARTED;
		}
		return thread.getStatus();
	}

	public boolean isActive() {
		if (thread == null) {
			return false;
		}
		return thread.isActive();
	}

	class LocationServiceThread implements LocationListener, Runnable {
		private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		private final String TAG = "LocationServiceThread";
		private String imeiNumber;
		private String status;
		private volatile boolean active;
		
		private LocationManager locationManager;
		private ArrayList<LocationUpdates> lastLocations = new ArrayList<LocationUpdates>();
		private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
		private Runnable sendToServerTask = new Runnable() {
			public void run() {
				if (active) 
				{
					try
					{
						sendToServer();
						Log.i(TAG, "SEND to Server");
					}
					catch(Exception e)
					{

					}
					//toast("Send to Server invoked");
				}
			}
		};

		public Location getLastLoc()
		{
			return lastLocation;
		}
		private Runnable addLocationtoArrayTask = new Runnable() {
			public void run() {
				if (active) 
				{
					try{
						addToLocationArray();
						Log.i(TAG, "add to Location");
					}catch(Exception e)
					{

					}
					//System.out.println("Add to Array invoked");
					//toast("Add to Array invoked");
				}
			}
		};

		public LocationServiceThread(String imeiNumber) {
			this.imeiNumber=imeiNumber;
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			active = true;
			scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
			setStatus(ACTIVE);
		}

		public void stop() {
			scheduledThreadPoolExecutor.shutdown();
		}


		private void addToLastlocation(Location loc)
		{
			if(loc!=null)
			{
				if(lastLocation==null)
					lastLocation=loc;
				else
					synchronized (lastLocation) 
					{
						try
						{
							lastLocation=loc;	
						}
						catch(Exception e)
						{
						}		
					}
			}			
		}

		private void addToLocationArray()
		{
			if(lastLocation!=null)
			{
				synchronized (lastLocation) 
				{
					try
					{
						List<LocationUpdates> syncedLocationList = Collections.synchronizedList(lastLocations);
						syncedLocationList.add(new LocationUpdates(lastLocation));
					}
					catch(Exception e)
					{
					}		
				}
			}			
		}

		private String getLocationURL()
		{
			String cebuStr="";
			try
			{				

				StringBuilder builder = new StringBuilder();
				List<LocationUpdates> syncedLocationList = Collections.synchronizedList(lastLocations);
				{
					int size=syncedLocationList.size();
					int i;
					if(size>0)
					{
						for(i=0;i<size;i++)
						{
							builder.append(syncedLocationList.get(i).toString());
							builder.append(System.getProperty("line.separator"));
						}
						syncedLocationList.clear();
					}
				}		
				cebuStr=builder.toString();
			}
			catch(Exception e)
			{
			}

			return cebuStr;
		}		

		private void sendToServer()
		{
			String locationStr=getLocationURL();
			if(locationStr.length()<=0)
				return;
			//Log.e(TAG, locationStr);
			//HttpClient client = Utils.getNewHttpClient();
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(CebuActivity.apiRequestUrl + "/api/location");
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("imei", imeiNumber));
				nameValuePairs.add(new BasicNameValuePair("content", locationStr));
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(request);
				if (response.getStatusLine().getStatusCode() == 200) 
				{
					Log.i(TAG, "Updated Server successfully ");
										
				}
				else
					Log.e(TAG, "Server Updation Error");
			} catch (ClientProtocolException e) {
				Log.e(TAG, "protocol exception sending ping", e);
			} catch (IOException e) {
				Log.e(TAG, "IO exception sending ping", e);
			} catch (Exception e) {
				Log.e(TAG, "some other problem sending ping", e);
			}
			return;
		}

		public void toast(String message) {
			if (!active) {
				return;
			}
			if (activity != null) {
				activity.toast(message);
			}
		}

		public void run() {

			Log.i(TAG,"GPS interval="+gpsInterval);
			Log.i(TAG,"Update interval="+updateInterval);
			scheduledThreadPoolExecutor.scheduleAtFixedRate(addLocationtoArrayTask, 0, gpsInterval, TimeUnit.SECONDS);
			scheduledThreadPoolExecutor.scheduleAtFixedRate(sendToServerTask, 0, updateInterval, TimeUnit.SECONDS);
		}

		public void setStatus(String status) {
			this.status = status;
			scheduledThreadPoolExecutor.execute(sendToServerTask);
			if (status.equals(INACTIVE)) {
				Log.i(TAG, "Shutting down thread, service marked as inactive");
				scheduledThreadPoolExecutor.remove(addLocationtoArrayTask);
				scheduledThreadPoolExecutor.remove(sendToServerTask);
				scheduledThreadPoolExecutor.shutdown();
			}
		}

		public String getStatus() {
			return status;
		}

		public void setActive(boolean active) {
			this.active = active;
			if (active)
				startReceivingLocation();
			else
				stopReceivingLocation();			
		}
		public void startReceivingLocation() {
			Log.i(TAG, "Requesting location updates with a minTime of 0s and min distance of 0m");			
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 0, this);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2, 0, this);
		}

		public void stopReceivingLocation() {
			Log.i(TAG, "No longer requesting location updates");
			locationManager.removeUpdates(this);
		}

		public boolean isActive() {
			return active;
		}

		public void onLocationChanged(Location location) 
		{
			if (!active) 
			{
				return;
			}
			addToLastlocation(location);
		}

		public void onProviderDisabled(String provider) {			
		}

		public void onProviderEnabled(String provider) {			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}


}