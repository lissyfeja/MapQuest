package de.bht.mmi.ema.data;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CursorTransformer {

	private CursorTransformer() {

	}
	
	
	
	public static MQCalendar cursorToCalendar(final Cursor cursor) {
		MQCalendar calendar = null;
		if (cursor != null) {
			calendar = new MQCalendar();
			calendar.setCalendarID(cursor.getString(cursor.getColumnIndex(Calendars._ID)));
			calendar.setAccountName(cursor.getString(cursor.getColumnIndex(Calendars.ACCOUNT_NAME)));
			calendar.setName(cursor.getString(cursor.getColumnIndex(Calendars.NAME)));
			calendar.setDisplayName(cursor.getString(cursor.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME)));
			calendar.setVisible(cursor.getInt(cursor.getColumnIndex(Calendars.VISIBLE)) == 1);
			calendar.setColor(cursor.getInt(cursor.getColumnIndex(Calendars.CALENDAR_COLOR)));
		}
		return calendar;
	}
	
	public static MQCalendarEvent cursorToEvent(final Cursor cursor) {
		MQCalendarEvent event = null;
		if (cursor != null) {
			event = new MQCalendarEvent();
			event.setCalendarID(cursor.getString(cursor.getColumnIndex(Events.CALENDAR_ID)));
			event.setCalendarColor(cursor.getInt(cursor.getColumnIndex(Events.CALENDAR_COLOR)));
			event.setCalendarDisplayName(cursor.getString(cursor.getColumnIndex(Events.CALENDAR_DISPLAY_NAME)));
			event.setTitle(cursor.getString(cursor.getColumnIndex(Events.TITLE)));
			event.setDescription(cursor.getString(cursor.getColumnIndex(Events.DESCRIPTION)));
			event.setDtStart(cursor.getLong(cursor.getColumnIndex(Events.DTSTART)));
			event.setDtEnd(cursor.getLong(cursor.getColumnIndex(Events.DTEND)));
			event.setLocation(cursor.getString(cursor.getColumnIndex(Events.EVENT_LOCATION)));
			event.setHasAlarm(cursor.getInt(cursor.getColumnIndex(Events.HAS_ALARM)) == 1);
		}
		return event;
	}
	
	public static ContentValues eventToValues(MQCalendarEvent event) {
		ContentValues values = new ContentValues();
		values.put(Events.CALENDAR_ID, event.getCalendarID());
		values.put(Events.TITLE, event.getTitle());
		values.put(Events.DESCRIPTION, event.getDescription());
		values.put(Events.DTSTART, event.getDtStart());
		values.put(Events.DTEND, event.getDtEnd());
		values.put(Events.EVENT_LOCATION, event.getLocation());
		values.put(Events.HAS_ALARM, (event.isHasAlarm()) ? 1 : 0);
		return values;
	}

}