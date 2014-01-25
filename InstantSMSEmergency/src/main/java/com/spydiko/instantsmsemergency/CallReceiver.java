package com.spydiko.instantsmsemergency;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by spiros on 1/25/14.
 */
public class CallReceiver extends BroadcastReceiver {

	private static final String TAG = CallReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {

		if (InstantSMSemergensy.debugging) Log.d(TAG, "onReceived");

		InstantSMSemergensy instantSMSemergensy = (InstantSMSemergensy) context.getApplicationContext();
		MyPhoneStateListener phoneListener = new MyPhoneStateListener(instantSMSemergensy);
		TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

	}

	static class MyPhoneStateListener extends PhoneStateListener {


		private static String callState = "";
		private final InstantSMSemergensy instantSMSemergensy;

		public MyPhoneStateListener(InstantSMSemergensy iSMS) {
			 instantSMSemergensy = iSMS;

		}

		public void onCallStateChanged(int state, String incomingNumber) {

			if (instantSMSemergensy.isServiceRunning()) {
				switch (state) {
					case TelephonyManager.CALL_STATE_IDLE:

						callState = "IDLE";
						if (InstantSMSemergensy.debugging) Log.d(TAG, callState);
						instantSMSemergensy.setPhoneRinging(false);
						break;

					case TelephonyManager.CALL_STATE_OFFHOOK:

						callState = "OFFHOOK";
						if (InstantSMSemergensy.debugging) Log.d(TAG, callState);
						instantSMSemergensy.setPhoneRinging(false);
						break;

					case TelephonyManager.CALL_STATE_RINGING:

						callState = "RINGING";
						if (InstantSMSemergensy.debugging) Log.d(TAG, callState);
						instantSMSemergensy.setPhoneRinging(true);
						break;
					default:
						callState = "DEFAULT";
						instantSMSemergensy.setPhoneRinging(false);
						break;
				}
			}
		}
	}
}