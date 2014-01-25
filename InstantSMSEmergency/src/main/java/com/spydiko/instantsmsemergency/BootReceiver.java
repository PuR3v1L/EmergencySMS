package com.spydiko.instantsmsemergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by spiros on 1/25/14.
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = BootReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if(InstantSMSemergensy.debugging) Log.d(TAG, "onReceive");
		InstantSMSemergensy instantSMSemergensy = (InstantSMSemergensy) context.getApplicationContext();
		if(instantSMSemergensy.isServiceRunning()) instantSMSemergensy.startService(new Intent(instantSMSemergensy, MyService.class));

	}
}