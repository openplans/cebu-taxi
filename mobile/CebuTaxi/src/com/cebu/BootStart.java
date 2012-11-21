package com.cebu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootStart extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtil.appendLog("LOGGER"+"Boot Message received");
        Intent startServiceIntent = new Intent(context, LocationService.class);
        startServiceIntent.setAction("BootStart");
        context.startService(startServiceIntent);
	}
}
