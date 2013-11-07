package com.spydiko.instantsmsemergency;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by jim on 6/11/2013.
 */
public class PrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = PrefsActivity.class.getSimpleName();
	ListPreference screenOffCounterList;
	CheckBoxPreference vibratePref, locationPref;
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
		setScreenOffCounterSum();
		setVibrationSum();
		setLocationSum();
	}

	private void setScreenOffCounterSum() {
		screenOffCounterList = (ListPreference) findPreference("screen_off_counter");
		screenOffCounterList.setSummary(getResources().getString(R.string.screen_off_counter) + " " + (screenOffCounterList.getEntry()) + " clicks");
	}

	private void setVibrationSum() {
		vibratePref = (CheckBoxPreference) findPreference("vibrate_pref");
		if (vibratePref.isChecked()) vibratePref.setSummary(getResources().getString(R.string.vibrate_on));
		else vibratePref.setSummary(getResources().getString(R.string.vibrate_off));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		if (InstantSMSemergensy.debugging) Log.d(TAG, "onSharedPreferenceChanged: " + s);
		if (s.equals("screen_off_counter")) setScreenOffCounterSum();
		if (s.equals("vibrate_pref")) setVibrationSum();
		if (s.equals("location_pref")) setLocationSum();
	}

	private void setLocationSum() {
		locationPref = (CheckBoxPreference) findPreference("location_pref");
		if (locationPref.isChecked()) locationPref.setSummary(getResources().getString(R.string.location_on));
		else locationPref.setSummary(getResources().getString(R.string.location_off));
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
