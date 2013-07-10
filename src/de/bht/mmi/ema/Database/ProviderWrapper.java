package de.bht.mmi.ema.Database;

import java.util.TimeZone;

import de.bht.mmi.ema.data.CursorTransformer;
import de.bht.mmi.ema.data.MQCalendarEvent;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ProviderWrapper {
	
	private ProviderWrapper() {
		
	}
	
	
	
	public static void insertEvent(final Context context, final MQCalendarEvent event) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				
				ContentValues values = CursorTransformer.eventToValues(event);
				
				// it is mandatory to set a TimeZone when inserting new events
				TimeZone timeZone = TimeZone.getDefault();
				values.put(Events.EVENT_TIMEZONE, timeZone.getID());
				
				// new event has no id
				values.remove(Events._ID);
				
				Uri uri = context.getContentResolver().insert(Events.CONTENT_URI, values);
			}
		}).start();
	}
	
	public static void updateEvent(final Context context, final MQCalendarEvent event) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				
				ContentValues values = CursorTransformer.eventToValues(event);
				
				Uri updateUri = ContentUris.withAppendedId(Events.CONTENT_URI, event.getID());
				
				int rows = context.getContentResolver().update(updateUri, values, null, null);
			}
		}).start();
	}
	
	public static void deleteEvent(final Context context, final MQCalendarEvent event) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				
				Uri deleteUri = ContentUris.withAppendedId(Events.CONTENT_URI, event.getID());
				
				int rows = context.getContentResolver().delete(deleteUri, null, null);
			}
		}).start();
	}
	
	/**
	 * May be called from a Synchronize-button in the menu
	 */
	public static void startManualSync() {
		Bundle extras = new Bundle();
		extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		ContentResolver.requestSync(null, CalendarContract.Calendars.CONTENT_URI.getAuthority(), extras);
	}
	
	
	
}