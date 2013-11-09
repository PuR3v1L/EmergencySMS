package com.spydiko.instantsmsemergency;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.NavUtils;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by jim on 6/11/2013.
 */
public class PrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = PrefsActivity.class.getSimpleName();
//	ListPreference screenOffCounterList;
	CheckBoxPreference vibratePref, locationPref, lastKnownLocation, notification;
	SwitchPreference vibratePrefSwitch, locationPrefSwitch, lastKnownLocationSwitch, notificationSwitch;
	InstantSMSemergensy instantSMSemergensy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs);
		instantSMSemergensy = (InstantSMSemergensy) this.getApplication();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			getActionBar().setDisplayHomeAsUpEnabled(true);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		setSummaries();
	}

	private void setSummaries() {
//		setScreenOffCounterSum();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			setVibrationSwitchSum();
			setLocationSwitchSum();
			setLastKnownLocationSwitchSum();
			setNotificationSwitchSum();
		}else{
			setVibrationSum();
			setLocationSum();
			setLastKnownLocationSum();
			setNotificationSum();
		}
	}

	@SuppressLint("NewApi")
	private void setLocationSwitchSum() {
		locationPrefSwitch = (SwitchPreference) findPreference("location_pref");
		if (locationPrefSwitch.isChecked()) locationPrefSwitch.setSummary(getResources().getString(R.string.location_on));
		else locationPrefSwitch.setSummary(getResources().getString(R.string.location_off));
	}

	private void setLocationSum() {
		locationPref = (CheckBoxPreference) findPreference("location_pref");
		if (locationPref.isChecked()) locationPref.setSummary(getResources().getString(R.string.location_on));
		else locationPref.setSummary(getResources().getString(R.string.location_off));
	}
	@SuppressLint("NewApi")
	private void setLastKnownLocationSwitchSum() {
		lastKnownLocationSwitch = (SwitchPreference) findPreference("last_known_location");
		if (lastKnownLocationSwitch.isChecked()) lastKnownLocationSwitch.setSummary(getResources().getString(R.string.last_known_location_on));
		else lastKnownLocationSwitch.setSummary(getResources().getString(R.string.last_known_location_off));
	}


	private void setLastKnownLocationSum() {
		lastKnownLocation = (CheckBoxPreference) findPreference("last_known_location");
		if (lastKnownLocation.isChecked()) lastKnownLocation.setSummary(getResources().getString(R.string.last_known_location_on));
		else lastKnownLocation.setSummary(getResources().getString(R.string.last_known_location_off));
	}

	@SuppressLint("NewApi")
	private void setVibrationSwitchSum() {
		vibratePrefSwitch = (SwitchPreference) findPreference("vibrate_pref");
		if (vibratePrefSwitch.isChecked()) vibratePrefSwitch.setSummary(getResources().getString(R.string.vibrate_on));
		else vibratePrefSwitch.setSummary(getResources().getString(R.string.vibrate_off));
	}

//	private void setScreenOffCounterSum() {
//		screenOffCounterList = (ListPreference) findPreference("screen_off_counter");
//		screenOffCounterList.setSummary(getResources().getString(R.string.screen_off_counter) + " " + (screenOffCounterList.getEntry()) + " clicks");
//	}

	private void setVibrationSum() {
		vibratePref = (CheckBoxPreference) findPreference("vibrate_pref");
		if (vibratePref.isChecked()) vibratePref.setSummary(getResources().getString(R.string.vibrate_on));
		else vibratePref.setSummary(getResources().getString(R.string.vibrate_off));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		if (InstantSMSemergensy.debugging) Log.d(TAG, "onSharedPreferenceChanged: " + s);
//		if (s.equals("screen_off_counter")) setScreenOffCounterSum();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			if (s.equals("vibrate_pref")) setVibrationSwitchSum();
			if (s.equals("location_pref")) setLocationSwitchSum();
			if (s.equals("last_known_location")) setLastKnownLocationSwitchSum();
			if (s.equals("permanent_notification")) setNotificationSwitchSum();
		}else{
			if (s.equals("vibrate_pref")) setVibrationSum();
			if (s.equals("location_pref")) setLocationSum();
			if (s.equals("last_known_location")) setLastKnownLocationSum();
			if (s.equals("permanent_notification")) setNotificationSum();
		}
		if ((s.equals("location_pref") || s.equals("permanent_notification")) && instantSMSemergensy.isServiceRunning()) startService(new Intent(this, MyService.class));
	}
	@SuppressLint("NewApi")
	private void setNotificationSwitchSum() {
		notificationSwitch = (SwitchPreference) findPreference("permanent_notification");
		if (notificationSwitch.isChecked()) notificationSwitch.setSummary(getResources().getString(R.string.permanent_notification_summary_on));
		else notificationSwitch.setSummary(getResources().getString(R.string.permanent_notification_summary_off));
	}

	private void setNotificationSum() {
		notification = (CheckBoxPreference) findPreference("permanent_notification");
		if (notification.isChecked()) notification.setSummary(getResources().getString(R.string.permanent_notification_summary_on));
		else notification.setSummary(getResources().getString(R.string.permanent_notification_summary_off));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
