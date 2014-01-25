package com.spydiko.instantsmsemergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jim on 4/11/2013.
 */
public class RemoteControlReceiver extends BroadcastReceiver {

	private static final String TAG = "RemoteBroadcastReceiver";
	static int countPowerOff = 0;
	static boolean sentSMS = false;
	private InstantSMSemergensy instantSMSemergensy;

	public RemoteControlReceiver(InstantSMSemergensy instantSMSemergensy) {
		this.instantSMSemergensy = instantSMSemergensy;
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		if (InstantSMSemergensy.debugging) Log.d("onReceive", "Power button is pressed. Sent Sms is " + sentSMS);
		int threshold = instantSMSemergensy.loadCounterClicks()/2;
		if (InstantSMSemergensy.debugging) Log.d(TAG,"threshold: "+threshold);
		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && instantSMSemergensy.getPhoneRinging()) {
			countPowerOff++;
		} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			if (countPowerOff >= threshold && !sentSMS) {
				if (instantSMSemergensy.isVibration()) MyService.v.vibrate(2000);
				instantSMSemergensy.sendSMS();
				if (InstantSMSemergensy.debugging) Log.d(TAG,"SENDSMS");
				sentSMS = true;
			}
		}

	}

}