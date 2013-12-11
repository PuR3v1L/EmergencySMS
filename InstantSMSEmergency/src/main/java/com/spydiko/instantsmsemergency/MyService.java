package com.spydiko.instantsmsemergency;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Created by jim on 4/11/2013.
 */
public class MyService extends Service {
	private static final String TAG = "MyService";
	private RemoteControlReceiver mReceiver = null;
	private IntentFilter filter;
	private InstantSMSemergensy instantSMSemergensy;
	private boolean getLocation,isNotification;
	public static Vibrator v;
	//	private int count;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		instantSMSemergensy = (InstantSMSemergensy) getApplication();
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new RemoteControlReceiver(instantSMSemergensy);
		registerReceiver(mReceiver, filter);
		if (InstantSMSemergensy.debugging) Log.d("service", "end of start");
		RemoteControlReceiver.countPowerOff = 4;
		instantSMSemergensy.setServiceRunning(true);
		if (instantSMSemergensy.isGetLocation()){
			instantSMSemergensy.localizeClient();
		}
		if (instantSMSemergensy.isNotification()){
			createAndStartNotification();
		}
		getLocation = instantSMSemergensy.isGetLocation();
		isNotification = instantSMSemergensy.isNotification();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			new MyAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
		else new MyAsyncTask().execute((Void[]) null);
	}

	private void createAndStartNotification() {
		Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.nothing).setTicker(getResources().getString(R.string.notification_text))
				.setContentTitle(getResources().getString(R.string.notification_title)).setContentText(getResources().getString(R.string.notification_context_text))
				.setWhen(0).setPriority(NotificationCompat.PRIORITY_MIN).setLargeIcon(image);
		Intent resultIntent = new Intent(this, MainActivity.class);
		// Because clicking the notification opens a new ("special") activity, there's
		// no need to create an artificial back stack.
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		Notification notification = mBuilder.build();
		startForeground(1337, notification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (getLocation != instantSMSemergensy.isGetLocation()) {
			if (InstantSMSemergensy.debugging) Log.d(TAG, "Get Location Changed!");
			if (instantSMSemergensy.isGetLocation()) {
				instantSMSemergensy.localizeClient();
			} else {
				instantSMSemergensy.delocalizeClient();
			}
			getLocation = instantSMSemergensy.isGetLocation();
		}
		if (isNotification != instantSMSemergensy.isNotification()){
			if (InstantSMSemergensy.debugging) Log.d(TAG, "Notification Changed");
			if (instantSMSemergensy.isNotification()) {
				createAndStartNotification();
			} else {
				stopForeground(true);
			}
			isNotification = instantSMSemergensy.isNotification();
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		unregisterReceiver(mReceiver);
		instantSMSemergensy.delocalizeClient();
		instantSMSemergensy.setServiceRunning(false);
	}

	public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

		int count;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			count = 0;
			RemoteControlReceiver.countPowerOff = 0;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

		}

		@Override
		protected Void doInBackground(Void... params) {
			while (instantSMSemergensy.isServiceRunning()) {
				try {
					Thread.sleep(6000);
					if (InstantSMSemergensy.debugging) Log.d("MyService", "CountPowerOff = " + RemoteControlReceiver.countPowerOff + " MyCount = " + count);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (RemoteControlReceiver.sentSMS) {
					// Vibrate for 500 milliseconds
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					RemoteControlReceiver.countPowerOff = 0;
					count = 0;
				}
				if (count == RemoteControlReceiver.countPowerOff || RemoteControlReceiver.countPowerOff == count + 1) {
					count = 0;
					RemoteControlReceiver.countPowerOff = 0;
					RemoteControlReceiver.sentSMS = false;
					if (InstantSMSemergensy.debugging) Log.d("MyService", "IF");
				} else {
					count = RemoteControlReceiver.countPowerOff;
					if (InstantSMSemergensy.debugging) Log.d("MyService", "ELSE");
				}
			}
			return null;
		}
	}

}
