package edu.fsu.cs.facebookcalendarsync;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Reminders;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class MainActivity extends Activity {

	ListView listView1;
	private LoginButton loginButton;
	private GraphUser user;
	private Button submitbutton;
	private UiLifecycleHelper uiHelper;
	private Session session;

	/////////////////////////////
	//Search Constant
	/////////////////////////////
	private static final String[] COLS = new String[] {
			CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART,
			CalendarContract.Events.DESCRIPTION,
			CalendarContract.Events.CALENDAR_ID, CalendarContract.Events._ID };
	/////////////////////////////
	//end Search Constant
	/////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		loginButton = (LoginButton) findViewById(R.id.login);
		loginButton.setReadPermissions(Arrays.asList("friends_birthday"));
		loginButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						MainActivity.this.user = user;

						// It's possible that we were waiting for this.user to
						// be populated in order to post a
						// status update.
					}
				});

		// **** Populate this string array with all names! ****
		// note: a method to loop and add "name, date" dynamically will be
		// needed
		String[] listItems = { "James Kobayashi, 07-19", "Guanyu Tian, 07-20" };

		// Initialize ListView
		listView1 = (ListView) findViewById(R.id.listView);
		listView1.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, listItems));
		listView1.setItemsCanFocus(false);
		listView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		// Check or Uncheck the box on click
		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				CheckedTextView ctv = (CheckedTextView) arg1;
			}
		});

		// *** Submit Button ***
		submitbutton = (Button) findViewById(R.id.button2);
		submitbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Array of checked items and their positions taken
				SparseBooleanArray checkedItems = listView1
						.getCheckedItemPositions();

				if (checkedItems != null) {
					for (int i = 0; i < checkedItems.size(); i++) {
						if (checkedItems.valueAt(i)) {

							// If the item is checked, grab the string
							String friendInfo = listView1.getAdapter()
									.getItem(checkedItems.keyAt(i)).toString();

							// Parse the string for name and date
							String[] info = friendInfo.split(",");

							// Enter into calendar (Default 10 minutes)
							createEvent(info[0].trim(), info[1].trim(), 10);
						}
					}
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void createEvent(String name, String birthday, int reminder_minutes) {

		String[] calendarIds = new String[] { "_id", "name" };
		Uri calendars = Uri.parse("content://com.android.calendar/calendars");

		Cursor mCursor = managedQuery(calendars, calendarIds, null, null, null);
		if (mCursor.moveToFirst()) {
			String calName;
			String calId;
			int nameColumn = mCursor.getColumnIndex("name");
			int idColumn = mCursor.getColumnIndex("_id");
			// do {
			calName = mCursor.getString(nameColumn);
			calId = mCursor.getString(idColumn);

			long theDate = 0;
			try {
				Date date = new SimpleDateFormat("MM-dd").parse(birthday);
				theDate = date.getTime();
			} catch (Exception e) {
			}

			// //////////////////////////////////////
			// search for event
			// //////////////////////////////////////
			String mSelectionClause = CalendarContract.Events.TITLE
					+ " = ? AND " + CalendarContract.Events.DESCRIPTION
					+ " = ?";// AND " + CalendarContract.Events.DTSTART + " = ?
								// AND " + CalendarContract.Events.CALENDAR_ID;
			String[] mSelectionArgs = new String[] { name + "'s Birthday",
					"Facebook Birthday", };// birthday, calId};
			String mOrderBy;
			int[] mListItems;

			mCursor = getContentResolver().query(
					CalendarContract.Events.CONTENT_URI, COLS,
					mSelectionClause, mSelectionArgs, null);
			mCursor.moveToFirst();
			String title = "N/A";
			Long start = 0L;
			Format df = DateFormat.getDateFormat(this);
			Format tf = DateFormat.getTimeFormat(this);
			try {
				title = mCursor.getString(0);
				start = mCursor.getLong(1);
			} catch (Exception e) {
				// ignore
			}
			Log.i("find event",
					title + " on " + df.format(start) + " at "
							+ tf.format(start));
			// //////////////////////////////////////
			// end search for event
			// //////////////////////////////////////

			////////////////////////////////////////
			// add event
			///////////////////////////////////////
			if (title.equals("N/A")) {
				// new birthday
			
				ContentResolver cr = this.getContentResolver();
				ContentValues event = new ContentValues();

				event.put(CalendarContract.Events.CALENDAR_ID, calId);
				event.put(CalendarContract.Events.TITLE, name + "'s Birthday");
				event.put(CalendarContract.Events.DESCRIPTION,
						"Facebook Birthday");
				event.put(CalendarContract.Events.DTSTART, theDate);
				event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone
						.getDefault().getID());
				event.put(CalendarContract.Events.DURATION, "P1D");
				event.put(CalendarContract.Events.ALL_DAY, 1);
				event.put(CalendarContract.Events.RRULE, "FREQ=YEARLY");
				event.put(CalendarContract.Events.HAS_ALARM, 1);

				// set a 10 minute reminder
				ContentValues reminders = new ContentValues();
				Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, event);
				long eventID = Long.parseLong(uri.getLastPathSegment());
				reminders.put(Reminders.EVENT_ID, eventID);
				reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
				reminders.put(Reminders.MINUTES, reminder_minutes);
				Uri uri2 = cr.insert(Reminders.CONTENT_URI, reminders);
			} else {
				//birthday already exists
			}
			////////////////////////////////////////
			// end add event
			///////////////////////////////////////
		}
		mCursor.close();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show();

		String fqlQuery = "SELECT uid, name, birthday_date FROM user WHERE uid IN"
				+ "(SELECT uid2 FROM friends WHERE uid1 = me()";

		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		session = Session.getActiveSession();
		Request request = new Request(session, "/fql", params, HttpMethod.GET,
				new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						Log.d("Facebook", "here 1");
						// TODO Auto-generated method stub
						FacebookRequestError error = response.getError();
						Log.d("Facebook", "Result: " + response.toString());
					}
				});
		Request.executeBatchAsync(request);

	}

	/*
	 * Might be used later. private void updateUI() { Session session =
	 * Session.getActiveSession(); boolean enableButtons = (session != null &&
	 * session.isOpened()); //submitbutton.setEnabled(enableButtons); }
	 */
}
