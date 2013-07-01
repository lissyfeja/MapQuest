package de.bht.mmi.ema.data;

import android.database.Cursor;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

public class CursorTransformer {

	private CursorTransformer() {

	}
	
	
	
	public static MQCalendar cursorToCalendar(final Cursor cursor) {
		MQCalendar calendar = null;
		if (cursor != null) {
			calendar = new MQCalendar();
			calendar.setAccountName(cursor.getString(cursor.getColumnIndex(Calendars.ACCOUNT_NAME)));
			calendar.setName(cursor.getString(cursor.getColumnIndex(Calendars.NAME)));
			calendar.setDisplayName(cursor.getString(cursor.getColumnIndex(Calendars.CALENDAR_DISPLAY_NAME)));
			calendar.setVisible(cursor.getInt(cursor.getColumnIndex(Calendars.VISIBLE)) == 1);
			calendar.setColor(cursor.getInt(cursor.getColumnIndex(Calendars.CALENDAR_COLOR)));
		}
		return calendar;
	}
	
	public static CalendarEvent cursorToEvent(final Cursor cursor) {
		CalendarEvent event = null;
		if (cursor != null) {
			event = new CalendarEvent();
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

}