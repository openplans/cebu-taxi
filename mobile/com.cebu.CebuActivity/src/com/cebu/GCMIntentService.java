package com.cebu;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.i("onMessage gcm", arg1);
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
		Log.i("onMessage gcm", arg1.getExtras().get("message").toString());
		String message =  arg1.getExtras().get("message").toString();
		
		Intent intent = new Intent(CebuActivity.DISPLAY_MESSAGE_ACTION);
        intent.putExtra("message", message);
        arg0.sendBroadcast(intent);
	}	

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		
		String postUrl = CebuActivity.apiRequestUrl + "/api/registerGcm";
		HttpClient client = Utils.getNewHttpClient();
		HttpPost request = new HttpPost(postUrl);
		
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
				
			nameValuePairs.add(new BasicNameValuePair("gcmKey", arg1));
			nameValuePairs.add(new BasicNameValuePair("imei", Utils.imeino(getApplicationContext())));
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = client.execute(request);
		
		}
		catch(Exception e)
		{
			Log.i("failed to register gcm key", e.toString());
		}
		
		
		Log.i("onRegistered gcm", arg1);

	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		Log.i("onUnregistered gcm", arg1);

	}

}
