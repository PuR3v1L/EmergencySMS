package com.spydiko.instantsmsemergency;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

	private static final String TAG = "MainActivity";
	private static final int PICK_CONTACT = 1;
	private InstantSMSemergensy instantSMSemergensy;
	private EditText phoneNumber, textToBeSent;
	private ImageButton buttonContacts;
	private TextView serviceState;
	private CheckBox serviceStateCheckbox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setTitle(R.string.app_name);
		instantSMSemergensy = (InstantSMSemergensy) getApplication();
		serviceState = (TextView) findViewById(R.id.serviceState);
		serviceStateCheckbox = (CheckBox) findViewById(R.id.checkBoxService);
		if (instantSMSemergensy.isServiceRunning()) {
			serviceState.setText(R.string.service_running);
			serviceStateCheckbox.setChecked(true);
			serviceState.setTextColor(getResources().getColor(R.color.Green));
		} else {
			serviceState.setText(R.string.service_not_running);
			serviceStateCheckbox.setChecked(false);
			serviceState.setTextColor(getResources().getColor(R.color.Red));
		}
		serviceStateCheckbox.setOnCheckedChangeListener(this);
		phoneNumber = (EditText) findViewById(R.id.phoneNumber);
		textToBeSent = (EditText) findViewById(R.id.textToBeSent);
		textToBeSent.setText(instantSMSemergensy.getTextToBeSent());
		phoneNumber.setText(instantSMSemergensy.getPhoneNumber());
		phoneNumber.addTextChangedListener(this);
		textToBeSent.addTextChangedListener(this);
//		buttonSave = (Button) findViewById(R.id.buttonSave);
//		buttonSave.setOnClickListener(this);
		buttonContacts = (ImageButton) findViewById(R.id.buttonContacts);
		buttonContacts.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			case R.id.preferences:
				startActivity(new Intent(this, PrefsActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		instantSMSemergensy.savePreferences();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			//			case (R.id.buttonSave):
			//				if (phoneNumber.getText().toString().matches("") || textToBeSent.getText().toString().matches("")) {
			//					Toast.makeText(this, "Enter correct", Toast.LENGTH_SHORT).show();
			//				}
			//				instantSMSemergensy.setPhoneNumber(phoneNumber.getText().toString());
			//				instantSMSemergensy.setTextToBeSent(textToBeSent.getText().toString());
			//				break;
			case (R.id.buttonContacts):
				//				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				//				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
				//				startActivityForResult(intent, PICK_CONTACT);
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(contactPickerIntent, PICK_CONTACT);
			default:
				break;
		}
	}

	public void manageContact(String number) {
		Toast.makeText(this, "Phone number: " + number, Toast.LENGTH_SHORT).show();
		if (instantSMSemergensy.getPhoneNumber().equals("")) instantSMSemergensy.setPhoneNumber(number);
		else instantSMSemergensy.setPhoneNumber(instantSMSemergensy.getPhoneNumber().concat("#" + number));
		phoneNumber.removeTextChangedListener(this);
		phoneNumber.setText(instantSMSemergensy.getPhoneNumber());
		phoneNumber.addTextChangedListener(this);
	}

	private void showToastError() {
		Toast.makeText(this, "The person doesn't have phone number", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			serviceState.setText(R.string.service_running);
			serviceState.setTextColor(getResources().getColor(R.color.Green));
			startService(new Intent(this, MyService.class));
		} else {
			serviceState.setText(R.string.service_not_running);
			serviceState.setTextColor(getResources().getColor(R.color.Red));
			stopService(new Intent(this, MyService.class));
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
			case (PICK_CONTACT):
				Cursor cursor = null;
				int phoneType;
				try {
					Uri result = data.getData();
					String id = result.getLastPathSegment();
					cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);

					int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
					phoneType = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
					if (cursor.getCount() > 1) { // contact has multiple phone numbers
						final CharSequence[] numbers = new CharSequence[cursor.getCount()];
						int i = 0;
						if (cursor.moveToFirst()) {
							while (!cursor.isAfterLast()) { // for each phone number, add it to the numbers array
								String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(), cursor.getInt(phoneType), ""); // insert a type string in front of the number
								String number = type + ": " + cursor.getString(phoneIdx);
								numbers[i++] = number;
								cursor.moveToNext();
							}
							// build and show a simple dialog that allows the user to select a number
							AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle(R.string.select_contact_phone_number_and_type);
							builder.setItems(numbers, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int item) {
									String number = (String) numbers[item];
									int index = number.indexOf(":");
									number = number.substring(index + 2);
									manageContact(number);
								}
							});
							AlertDialog alert = builder.create();
							alert.setOwnerActivity(this);
							alert.show();

						} else if(InstantSMSemergensy.debugging) Log.d(TAG, "No results");
					} else if (cursor.getCount() == 1) {
						if (cursor.moveToFirst()) {
							String number = cursor.getString(phoneIdx);
							if (InstantSMSemergensy.debugging) Log.d(TAG, "else if " + number);
							manageContact(number);
						}
						// contact has a single phone number, so there's no need to display a second dialog
					} else {
						showToastError();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
				break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if (InstantSMSemergensy.debugging) Log.d(TAG, "afterTextChanged entered");
		EditText editText;

		try{
			editText = (EditText) getCurrentFocus();

			switch (editText.getId()){
				case (R.id.phoneNumber):
					instantSMSemergensy.setPhoneNumber(phoneNumber.getText().toString());
					break;
				case (R.id.textToBeSent):
					instantSMSemergensy.setTextToBeSent(textToBeSent.getText().toString());
					break;
				default:
					break;
			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}
}
