package de.bht.mmi.ema.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.v4.content.CursorLoader;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CalendarProviderWrapper {
	
	private CalendarProviderWrapper() {
		
	}
	
	

	public static List<MQCalendar> getCalendars(final Context context) {
		List<MQCalendar> calendars = new ArrayList<MQCalendar>();
		Cursor cursor = context.getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, MQCalendar.FIELDS, null, null, null);
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				MQCalendar calendar = CursorTransformer.cursorToCalendar(cursor);
				if (calendar != null) {
					calendars.add(calendar);
				}
			}
		}
		return calendars;
	}
	
	/**
	 * Get all calendar events of all accounts whose title is not null
	 */
	public static CursorLoader getAllEvents(final Context context) {
		return getEvents(context, null, null);
	}
	
	/**
	 * Get all events that start today.
	 * @param context
	 * @return
	 */
	public static CursorLoader getTodaysEvents(final Context context) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		long from = cal.getTimeInMillis();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
		long to = cal.getTimeInMillis();
		
		return getEventsFromTo(context, from, to);
	}
	
	/**
	 * Get all calendar events of all accounts whose title is not null and whose starting date lies between from and to.
	 * @param context
	 * @param from date in milliseconds
	 * @param to date in milliseconds
	 * @return
	 */
	public static CursorLoader getEventsFromTo(final Context context, long from, long to) {
		String selection = Events.DTSTART + " > ? AND " + Events.DTSTART + " < ?";
		String[] selectionArgs = { Long.toString(from), Long.toString(to) };
		return getEvents(context, selection, selectionArgs);
	}
	
	private static CursorLoader getEvents(final Context context, String selection, String[] selectionArgs) {
		String s = Events.TITLE + " IS NOT null";
		if (selection != null) {
			s += " AND " + selection;
		}
		String sortOrder = Events.DTSTART + " ASC";
		return new CursorLoader(context, CalendarContract.Events.CONTENT_URI, CalendarEvent.FIELDS, s, selectionArgs, sortOrder);
	}
	
	
	
//	private static CursorLoader getInstances(final Context context, long from, long to) {
//		String s = Events.TITLE + " IS NOT null";
//		if (selection != null) {
//			s += " AND " + selection;
//		}
//		String sortOrder = Events.DTSTART + " ASC";
//		return new CursorLoader(context, CalendarContract.Instances.CONTENT_URI, CalendarEvent.FIELDS, s, selectionArgs, sortOrder);
//	}
	
	
	
}