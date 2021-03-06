package com.spydiko.instantsmsemergency;

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
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ActionItemTarget;
import com.espian.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends ActionBarActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, TextWatcher {

	private static final String TAG = "MainActivity";
	private static final int PICK_CONTACT = 1;
	private InstantSMSemergensy instantSMSemergensy;
	private EditText textToBeSent;
	private ImageView addContact;
	private ListView listView;
	private TextView serviceState;
	private CheckBox serviceStateCheckbox;
	private LinearLayout spydiko;
	private ArrayList<Hashtable<String, String>> listContacts;
	private SimpleAdapter simpleAdapter;
	private final String CONTACT_NAME = "cname", CONTACT_NUMBER = "cnumber";
	private int svCounter;
	private ShowcaseView sv;
	private ActionItemTarget actionTarget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getSupportActionBar().setTitle(R.string.app_name);
		instantSMSemergensy = (InstantSMSemergensy) getApplication();
		serviceState = (TextView) findViewById(R.id.serviceState);
		spydiko = (LinearLayout) findViewById(R.id.spydiko);
		spydiko.setOnClickListener(this);
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
		//		phoneNumber = (EditText) findViewById(R.id.phoneNumber);
		listView = (ListView) findViewById(R.id.list_contacts);
		listContacts = new ArrayList<Hashtable<String, String>>();
		textToBeSent = (EditText) findViewById(R.id.textToBeSent);
		textToBeSent.setText(instantSMSemergensy.getTextToBeSent());
		//		phoneNumber.setText(instantSMSemergensy.getPhoneNumber());
		//		phoneNumber.addTextChangedListener(this);
		textToBeSent.addTextChangedListener(this);
		//		buttonSave = (Button) findViewById(R.id.buttonSave);
		//		buttonSave.setOnClickListener(this);
		addContact = (ImageView) findViewById(R.id.add_contact);
		addContact.setOnClickListener(this);
		loadContacts();
		simpleAdapter = new SimpleAdapter(this, listContacts, R.layout.contact_row, new String[]{CONTACT_NAME, CONTACT_NUMBER}, new int[]{R.id.contact_name, R.id.contact_number}) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View row = super.getView(position, convertView, parent);
				ImageView removeContact = (ImageView) row.findViewById(R.id.remove_contact);
				removeContact.setTag(position);
				removeContact.setOnClickListener(MainActivity.this);
				TextView contactName = (TextView) row.findViewById(R.id.contact_name);
				contactName.setTag(position);
				removeContact.setTag(position);
				return row;
			}
		};
		listView.setAdapter(simpleAdapter);
		if (instantSMSemergensy.isTutorial()) {
			instantSMSemergensy.setTutorial(false);
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
			co.hideOnClickOutside = false;
			co.block = true;
			actionTarget = new ActionItemTarget(this, R.id.preferences);
			co.fadeInDuration = 1000;
			co.fadeOutDuration = 1000;
			co.shotType = ShowcaseView.TYPE_ONE_SHOT;
			ViewTarget target = new ViewTarget(R.id.add_contact, this);
			//		sv = ShowcaseView.insertShowcaseView(new ViewTarget(findViewById(R.id.serviceState)), this);
			sv = ShowcaseView.insertShowcaseView(target, this, R.string.showcase_title, R.string.showcase_message, co);
			sv.overrideButtonClick(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					switch (svCounter) {
						case 0:
							sv.setShowcase(new ViewTarget(textToBeSent), true);

							sv.setText(R.string.showcase_title_2, R.string.showcase_message_2);
							break;

						case 1:
							sv.setShowcase(new ViewTarget(serviceStateCheckbox), true);

							sv.setText(R.string.showcase_title_3, R.string.showcase_message_3);
							break;

						case 2:

							sv.setShowcase(actionTarget, true);
							sv.setText(R.string.showcase_title_4, R.string.showcase_message_4);
							break;

						case 3:
							sv.setShowcase(ShowcaseView.NONE);
							sv.setText(R.string.showcase_title_5, R.string.showcase_message_5);
							break;

						case 4:
							sv.hide();
							break;
					}
					svCounter++;
				}
			});
		}
	}


	private void loadContacts() {
		String numbers = instantSMSemergensy.getPhoneNumber();
		Log.d(TAG, numbers);
		numbers = numbers.trim();
		if (numbers.contains("#")) {
			String[] phoneNumbers = numbers.split("#");
			for (String phone : phoneNumbers) {
				if (!phone.equals("")) {
					Hashtable<String, String> temp = new Hashtable<String, String>();
					String[] data = phone.split("_name_:");
					temp.put(CONTACT_NAME, data[1]);
					temp.put(CONTACT_NUMBER, data[0]);
					listContacts.add(temp);
					if (instantSMSemergensy.debugging) Log.d(TAG, "multiple phone number to add to list: " + phone);
				}
			}
		} else {
			if (!numbers.equals("")) {
				if (instantSMSemergensy.debugging) Log.d(TAG, "simple phone number to add to list: " + numbers);
			}
		}
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
			case R.id.license:
				startActivity(new Intent(this, Licence.class));
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
			case (R.id.add_contact):
				//				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				//				intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
				//				startActivityForResult(intent, PICK_CONTACT);
				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(contactPickerIntent, PICK_CONTACT);
				break;
			case (R.id.remove_contact):
				Hashtable<String, String> temp = (Hashtable<String, String>) simpleAdapter.getItem((Integer) v.getTag());
				String numberToRemove = temp.get(CONTACT_NUMBER);
				String nameToRemove = temp.get(CONTACT_NAME);
				String phones = instantSMSemergensy.getPhoneNumber();
				int lastIndex = 0;
				int count = 0;
				while (lastIndex != -1) {
					lastIndex = phones.indexOf(numberToRemove, lastIndex);
					if (lastIndex != -1) {
						count++;
						lastIndex += numberToRemove.length();
					}
				}
				//				Log.d(TAG,""+count);
				//				Log.d(TAG,"BEFORE : "+phones);
				if (phones.contains(numberToRemove)) {
					instantSMSemergensy.setPhoneNumber(phones.replace(numberToRemove + "_name_:" + nameToRemove, ""));
				}
				phones = instantSMSemergensy.getPhoneNumber();
				//				Log.d(TAG,"BETWEEN : "+phones);
				while (count > 1) {
					instantSMSemergensy.setPhoneNumber(phones.concat("#" + numberToRemove.concat("_name_:" + nameToRemove)));
					phones = instantSMSemergensy.getPhoneNumber();
					count--;
				}
				//				Log.d(TAG,"AFTER : "+phones);
				int position = ((Integer) v.getTag()).intValue();
				Hashtable<String, String> removed = listContacts.remove(position);
				simpleAdapter.notifyDataSetChanged();
				if (InstantSMSemergensy.debugging) Log.d(TAG, phones + " " + numberToRemove + " " + v.getTag());
				for (Hashtable<String, String> item : listContacts) {
					if (InstantSMSemergensy.debugging) Log.d(TAG, item.get(CONTACT_NUMBER) + " " + item.get(CONTACT_NAME));
				}
				if (InstantSMSemergensy.debugging) Log.d(TAG, removed.get(CONTACT_NAME) + " " + removed.get(CONTACT_NUMBER));
				break;
			case (R.id.spydiko):
				// PLAY STORE *****
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				// Add the buttons
				builder.setTitle(R.string.spydiko);
				builder.setMessage(R.string.other_apps);
				builder.setIcon(R.drawable.icon);
				builder.setPositiveButton(R.string.playStore, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("market://search?q=pub:Spydiko"));
						startActivity(intent);
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User cancelled the dialog
					}
				});
				// Set other dialog properties

				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
				// ****************
				break;
			default:
				break;
		}
	}

	/*public void manageContact(String number) {
		Toast.makeText(this, "Phone number: " + number, Toast.LENGTH_SHORT).show();
		if (instantSMSemergensy.getPhoneNumber().equals("")) instantSMSemergensy.setPhoneNumber(number);
		else instantSMSemergensy.setPhoneNumber(instantSMSemergensy.getPhoneNumber().concat("#" + number));
		phoneNumber.removeTextChangedListener(this);
		phoneNumber.setText(instantSMSemergensy.getPhoneNumber());
		phoneNumber.addTextChangedListener(this);
	}*/

	public void manageContact(String number, String name) {
		if (instantSMSemergensy.getPhoneNumber().equals("")) instantSMSemergensy.setPhoneNumber(number + "_name_:" + name);
		else instantSMSemergensy.setPhoneNumber(instantSMSemergensy.getPhoneNumber().concat("#" + number).concat("_name_:" + name));
		Hashtable<String, String> temp = new Hashtable<String, String>();
		temp.put(CONTACT_NAME, name);
		temp.put(CONTACT_NUMBER, number);
		//		Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
		listContacts.add(temp);
		simpleAdapter.notifyDataSetChanged();
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
					int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
					if (cursor.getCount() > 1) { // contact has multiple phone numbers
						final CharSequence[] numbers = new CharSequence[cursor.getCount()];
						final CharSequence[] names = new CharSequence[cursor.getCount()];
						int i = 0;
						if (cursor.moveToFirst()) {
							while (!cursor.isAfterLast()) { // for each phone number, add it to the numbers array
								String type = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(this.getResources(), cursor.getInt(phoneType), ""); // insert a type string in front of the number
								String number = type + ": " + cursor.getString(phoneIdx);
								names[i] = cursor.getString(nameIdx);
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
									String name = (String) names[item];
									int index = number.indexOf(":");
									number = number.substring(index + 2);
									manageContact(number, name);
								}
							});
							AlertDialog alert = builder.create();
							alert.setOwnerActivity(this);
							alert.show();

						} else if (InstantSMSemergensy.debugging) Log.d(TAG, "No results");
					} else if (cursor.getCount() == 1) {
						if (cursor.moveToFirst()) {
							String number = cursor.getString(phoneIdx);
							String name = cursor.getString(nameIdx);
							if (InstantSMSemergensy.debugging) Log.d(TAG, "else if " + number);
							manageContact(number, name);
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

		try {
			editText = (EditText) getCurrentFocus();

			switch (editText.getId()) {
				case (R.id.textToBeSent):
					instantSMSemergensy.setTextToBeSent(textToBeSent.getText().toString());
					break;
				default:
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
