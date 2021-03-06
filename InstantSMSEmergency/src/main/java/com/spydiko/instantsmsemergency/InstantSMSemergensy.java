package com.spydiko.instantsmsemergency;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jim on 4/11/2013.
 */
public class InstantSMSemergensy extends Application {

	private static final String TAG = "InstantSMSemergency";
	private String phoneNumber, textToBeSent;
	public static final Boolean debugging = true;
	private LocationManager locationManager;
	private Location currentBestLocation;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int MINIMUM_TIME_INTERVAL = 10 * 60 * 1000;
	private static final int MINIMUM_CHANGE_IN_DISTANCE = 2000;
	private boolean serviceRunning;
	private static SharedPreferences prefs;
	private static SharedPreferences.Editor editor;
	private boolean phoneRinging = false;
	private String address = "";


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
		if (isGetLocation()) {
			getBestCurrentLocation();
		}
	}

	public void getBestCurrentLocation() {
		for (String s : locationManager.getAllProviders()) {
			if (locationManager.getLastKnownLocation(s) != null) {
				if (isBetterLocation(locationManager.getLastKnownLocation(s), currentBestLocation))
					currentBestLocation = locationManager.getLastKnownLocation(s);
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

	public boolean isTutorial() {
		return prefs.getBoolean("showTutorial", true);
	}

	public void setTutorial(boolean showTutorial) {
		editor.putBoolean("showTutorial", showTutorial);
		editor.commit();
	}

	public void savePreferences() {
		if (debugging) Log.d(TAG, "savePreferences");
		// Check
		if (phoneNumber.contains("#")) {
			String[] phoneNumbers = phoneNumber.split("#");
			phoneNumber = "";
			for (String phone : phoneNumbers) {
				if (!phone.equals("")) {
					phoneNumber = phoneNumber.concat(phone + "#");
				}
			}
		}
		if (debugging) Log.d(TAG, phoneNumber);
		editor.putString("phoneNumber", phoneNumber);
		editor.putString("textToBeSent", textToBeSent);
		editor.putBoolean("serviceRunning", serviceRunning);
		editor.commit();
	}

	public int loadCounterClicks() {
		return prefs.getInt("screen_off_counter", 8);
	}

	public boolean isGetLocation() {
		return prefs.getBoolean("location_pref", true);
	}

	public boolean isVibration() {
		return prefs.getBoolean("vibrate_pref", true);
	}

	public boolean isLastKnownLocation() {
		return prefs.getBoolean("last_known_location", true);
	}

	public void setPhoneRinging(boolean b) {
		phoneRinging = b;
	}

	public boolean getPhoneRinging() {
		return phoneRinging;
	}


	public void sendSMS() {
		String phoneNumber = getPhoneNumber().trim();
		String message = getTextToBeSent();
		if (isGetLocation()) {
			getBestCurrentLocation();
			double lat = currentBestLocation.getLatitude();
			double lont = currentBestLocation.getLongitude();
			long time_cur = currentBestLocation.getTime();
			if (currentBestLocation != null) {
				if (debugging) Log.d(TAG, "Difference:" + (System.currentTimeMillis() - time_cur));
				if (System.currentTimeMillis() - time_cur < TWO_MINUTES) {
					String http = "http://maps.google.com/?q=" + String.valueOf(lat) + "," + String.valueOf(lont + ", ");
					message = http.concat(message);
				} else if (isLastKnownLocation()) {
					String http = "http://maps.google.com/?q=" + String.valueOf(lat) + "," + String.valueOf(lont + ", ");
					message = http.concat(message);
				}
				message = message.concat(getAddress());

			}
		}
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> parts = smsManager.divideMessage(message);
		if (debugging) Log.d(TAG, "MESSAGE: " + message);
		if (phoneNumber.contains("#")) {
			String[] phoneNumbers = phoneNumber.split("#");
			for (String phone : phoneNumbers) {
				if (!phone.equals("")) {
					String[] phoneToSend = phone.split("_name_:");
					//					if (!message.equals("")) smsManager.sendTextMessage(phoneToSend[0], null, messageUTF.toString(), null, null);
					if (!message.equals("")) smsManager.sendMultipartTextMessage(phoneToSend[0], null, parts, null, null);
					if (debugging) Log.d(TAG, "multiple phone number to text: " + phone);
				}
			}
		} else {
			if (!phoneNumber.equals("")) {
				String[] phoneToSend = phoneNumber.split("_name_:");
				//				if (!message.equals("")) smsManager.sendTextMessage(phoneToSend[0], null, messageUTF.toString(), null, null);
				if (!message.equals("")) smsManager.sendMultipartTextMessage(phoneToSend[0], null, parts, null, null);
				if (debugging) Log.d(TAG, "simple phone number to text: " + phoneNumber);
			}
		}
	}
	public String getAddress (){
		return address;
	}
	public void setAddress(String address){
		this.address = address;
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
			if (debugging) Log.d(TAG, "New Location!");
			if (isBetterLocation(location, currentBestLocation)) {
				currentBestLocation = location;
				double lat = currentBestLocation.getLatitude();
				double lont = currentBestLocation.getLongitude();
				Geocoder myLocation = new Geocoder(getApplicationContext(), Locale.getDefault());
				try {
					List<Address> myList = myLocation.getFromLocation(lat, lont, 1);
					for (Address address : myList) {
						Log.d(TAG, address.toString());
						String read_address = "";
						int i = 0;
						while (true) {
							String temp = address.getAddressLine(i);
							if (temp == null) break;
							read_address = read_address.concat(", " + temp);
							i++;
						}
						Log.d(TAG, read_address);
//						message = message.concat(read_address);
						setAddress(read_address);

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onProviderEnabled(String provider) {}

		public void onProviderDisabled(String provider) {}
	};

	public boolean isNotification() {
		return prefs.getBoolean("permanent_notification", false);
	}

	public Resources getMyResources() {
		return getResources();
	}


	// ----------------------------------------****************************---------------------------------------------------------------------


}
