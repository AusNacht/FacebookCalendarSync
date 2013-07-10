package edu.fsu.cs.facebookcalendarsync;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
 
public class MainActivity extends Activity {

	ListView listView1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* To create an event on the user's calendar(s), call the following:
		 *  
		 * createEvent("James Kobayashi", "2013-07-20");
		 * 
		 * This is to create a single event on all calendars
		 *  titled: "James Kobayashi's Birthday"
		 *  date: July 20th 2013 
		 *  
		 * Notes:
		 * 1) The event will re-occur yearly, you can see
		 * 2) Using a calendar widget that syncs multiple calendars will display multiple birthdays
		 * 3) Currently, no duplicates are being sighted fortunately for a single calendar
		 */
		
		
		// **** Populate this string array with all names! ****
		// note: a method to loop and add dynamically will be needed
		String[] listItems = { "I'm on", "the list!" };

		// Initialize ListView
		listView1 = (ListView)findViewById(R.id.listView);
		listView1.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, listItems));
		listView1.setItemsCanFocus(false);
		listView1.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
		// Check or Uncheck the box on click
		listView1.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			CheckedTextView ctv = (CheckedTextView)arg1;
			
			// Any additional stuff to do onClick goes here
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void createEvent(String name, String birthday) {
		
		String[] calendarIds = new String[] { "_id", "name" };
		Uri calendars = Uri.parse("content://com.android.calendar/calendars");
		     
		Cursor mCursor = managedQuery(calendars, calendarIds, null, null, null);
		if (mCursor.moveToFirst( )) {
			String calName; 
			String calId; 
			int nameColumn = mCursor.getColumnIndex("name"); 
			int idColumn = mCursor.getColumnIndex("_id");
			do {
				calName = mCursor.getString(nameColumn);
				calId = mCursor.getString(idColumn);
				
				long theDate = 0;
			    try {
			        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(birthday);
			        theDate = date.getTime();
			    }
			    catch(Exception e){ }
			    
			    ContentValues event = new ContentValues( );
				event.put(CalendarContract.Events.CALENDAR_ID, calId);
				event.put(CalendarContract.Events.TITLE, name + "'s Birthday");
				event.put(CalendarContract.Events.DESCRIPTION, "Birthday");
				event.put(CalendarContract.Events.DTSTART, theDate);
				event.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault( ).getID( ));
				event.put(CalendarContract.Events.DURATION, "P1D");
				event.put(CalendarContract.Events.ALL_DAY, 1);
				event.put(CalendarContract.Events.RRULE, "FREQ=YEARLY");
				  
				Uri eventuri;
				if (Integer.parseInt(Build.VERSION.SDK) >= 8)
				    eventuri = getContentResolver( ).insert(Uri.parse("content://com.android.calendar/events"), event);
				else
				    eventuri = getContentResolver( ).insert(Uri.parse("content://calendar/events"), event);
			} while (mCursor.moveToNext());
		}
		mCursor.close();
	}
}
