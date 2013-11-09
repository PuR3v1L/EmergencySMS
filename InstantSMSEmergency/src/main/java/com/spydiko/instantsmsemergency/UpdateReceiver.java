package com.spydiko.instantsmsemergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jim on 8/11/2013.
 */
public class UpdateReceiver extends BroadcastReceiver {
	private static final String TAG = "UpdateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		InstantSMSemergensy instantSMSemergensy = (InstantSMSemergensy) context.getApplicationContext();
		if (instantSMSemergensy.debugging) Log.d(TAG, "mpika");
		if (instantSMSemergensy.isServiceRunning()){
			context.startService(new Intent(context, MyService.class));
		}
	}
}