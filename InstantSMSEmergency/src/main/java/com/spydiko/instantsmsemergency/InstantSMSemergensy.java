package com.spydiko.instantsmsemergency;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

/**
 * Created by jim on 4/11/2013.
 */
public class InstantSMSemergensy extends Application {

	private static final String TAG = "InstantSMSemergency";
	private String phoneNumber, textToBeSent;
	public static final Boolean debugging = false;
	private LocationManager locationManager;
	private Location currentBestLocation;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int MINIMUM_TIME_INTERVAL = 5 * 60 * 1000;
	private static final int MINIMUM_CHANGE_IN_DISTANCE = 1000;
	private boolean serviceRunning;
	private static SharedPreferences prefs;
	private static SharedPreferences.Editor editor;


	public boolean isServiceRunning() {
		return serviceRunning;
	}

	public void setServiceRunning(boolean serviceRunning) {
		this.serviceRunning = serviceRunning;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = prefs.edit();
		loadPreferences();
		if (loadGetLocation()) {
			getBestCurrentLocation();
		}
	}

	public void getBestCurrentLocation() {
		for (String s : locationManager.getAllProviders()) {
			if (locationManager.getLastKnownLocation(s) != null) {
				if (currentBestLocation==null) currentBestLocation = locationManager.getLastKnownLocation(s);
				else if (isBetterLocation(locationManager.getLastKnownLocation(s),currentBestLocation)) currentBestLocation = locationManager.getLastKnownLocation(s);
			}
		}
	}

	public String getTextToBeSent() {
		return textToBeSent;
	}

	public void setTextToBeSent(String textToBeSent) {
		this.textToBeSent = textToBeSent;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void loadPreferences() {
		if (debugging) Log.d(TAG, "loadPreferences");
		phoneNumber = prefs.getString("phoneNumber", "");
		textToBeSent = prefs.getString("textToBeSent", "");
		serviceRunning = prefs.getBoolean("serviceRunning", false);
	}

	public void savePreferences() {
		if (debugging) Log.d(TAG, "savePreferences");
		editor.putString("phoneNumber", phoneNumber);
		editor.putString("textToBeSent", textToBeSent);
		editor.putBoolean("serviceRunning", serviceRunning);
		editor.commit();
	}

	public int loadCounterClicks() {
		return Integer.valueOf(prefs.getString("screen_off_counter", "8"));
	}

	public boolean loadGetLocation() {
		return prefs.getBoolean("location_pref", true);
	}

	public boolean loadVibration() {
		return prefs.getBoolean("vibrate_pref", true);
	}

	public void sendSMS() {
		String phoneNumber = getPhoneNumber().trim();
		String message = getTextToBeSent();
		if (loadGetLocation()) {
			getBestCurrentLocation();
			if (currentBestLocation!=null){
				String http = "http://maps.google.com/?q="+String.valueOf(currentBestLocation.getLatitude())+","+String.valueOf(currentBestLocation.getLongitude()+" ");
				message = http.concat(message);
			}
		}
		SmsManager smsManager = SmsManager.getDefault();
		if (phoneNumber.contains("#")) {
			String[] phoneNumbers = phoneNumber.split("#");
			for (String phone : phoneNumbers) {
				if (!phone.equals("")) {
					smsManager.sendTextMessage(phone, null, message, null, null);
					if (debugging) Log.d(TAG, "multiple phone number to text: " + phone);
				}
			}
		} else {
			if (!phoneNumber.equals("")) {
				smsManager.sendTextMessage(phoneNumber, null, message, null, null);
				if (debugging) Log.d(TAG, "simple phone number to text: " + phoneNumber);
			}
		}
	}

	// ----------------------------------------****************************---------------------------------------------------------------------
	// -------------------------------------------LOCALIZATION FUNCTIONS------------------------------------------------------------------------


	/**
	 * Determines whether one Location reading is better than the current Location fix
	 *
	 * @param location            The new Location that you want to evaluate
	 * @param currentBestLocation The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether two providers are the same
	 */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public void localizeClient() {
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL, MINIMUM_CHANGE_IN_DISTANCE, locationListener);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MINIMUM_TIME_INTERVAL, MINIMUM_CHANGE_IN_DISTANCE, locationListener);
	}

	public void delocalizeClient() {
		locationManager.removeUpdates(locationListener);
	}

	// Define a listener that responds to location updates
	LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found by the network location provider.
			// TODO
			if (isBetterLocation(location,currentBestLocation))	currentBestLocation = location;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};

	// ----------------------------------------****************************---------------------------------------------------------------------


}
