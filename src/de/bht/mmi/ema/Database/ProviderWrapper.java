package de.bht.mmi.ema.Database;

import java.util.List;
import java.util.TimeZone;

import de.bht.mmi.ema.Geofence.SimpleGeofence;
import de.bht.mmi.ema.Geofence.SimpleGeofenceStore;
import de.bht.mmi.ema.data.CursorTransformer;
import de.bht.mmi.ema.data.MQCalendarEvent;
import de.bht.mmi.ema.data.MQReminder;
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
import android.provider.CalendarContract.Reminders;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ProviderWrapper {
	
	private ProviderWrapper() {
		
	}
	
	
	
	public static void insertEvent(final Context context, final MQCalendarEvent event) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				
				List<MQReminder> reminders = event.getReminders();
				SimpleGeofence geofence = event.getGeofenceReminder();
				
				ContentValues values = CursorTransformer.eventToValues(event);
//				if (geofence != null) {
//					values.put(Events.HAS_ALARM, 0);
//				}	
				
				
				// it is mandatory to set a TimeZone when inserting new events
				TimeZone timeZone = TimeZone.getDefault();
				values.put(Events.EVENT_TIMEZONE, timeZone.getID());
				
				// new event has no id
				values.remove(Events._ID);
				
				Log.d("MapQuest", values.toString());
				Uri uri = context.getContentResolver().insert(Events.CONTENT_URI, values);
				
				// insert the reminder
				if (uri != null) {
					long eventID = Long.parseLong(uri.getLastPathSegment());
					
					if (reminders != null && reminders.size() > 0 && event.isHasAlarm()) {
						MQReminder reminder = event.getReminders().get(0);
						reminder.setEventID(eventID);
						
						ContentValues reminderValues = CursorTransformer.reminderToValues(reminder);
			            reminderValues.remove(Reminders._ID);

			            Uri reminderUri = context.getContentResolver().insert(Reminders.CONTENT_URI, reminderValues);
					}
					
					if (geofence != null) {
//						geofence.setId(Long.toString(eventID));
						
						SimpleGeofenceStore store = new SimpleGeofenceStore(context);
						store.setGeofence(Long.toString(eventID), geofence);
						
					}
				}
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
				
				if (rows > 0) {
					List<MQReminder> reminders = event.getReminders();
					if (reminders != null && reminders.size() > 0) {
						MQReminder reminder = reminders.get(0);
						if (reminder.getID() == 0) {
							// insert
							reminder.setEventID(event.getID());
							
							ContentValues reminderValues = CursorTransformer.reminderToValues(reminder);
				            reminderValues.remove(Reminders._ID);

				            Uri reminderUri = context.getContentResolver().insert(Reminders.CONTENT_URI, reminderValues);
						} else {
							// update
							ContentValues reminderValues = CursorTransformer.reminderToValues(reminder);

							Uri reminderUpdateUri = ContentUris.withAppendedId(Reminders.CONTENT_URI, reminder.getID());

							int reminderRows = context.getContentResolver().update(reminderUpdateUri, reminderValues, null, null);
						}
					} else {
						// try to delete reminder
						long id = event.getReminderToDelete();
						Uri reminderDeleteUri = ContentUris.withAppendedId(Reminders.CONTENT_URI, id);
						int deletedRows = context.getContentResolver().delete(reminderDeleteUri, null, null);
					}
					
					SimpleGeofence geofence = event.getGeofenceReminder();
					if (geofence != null) {
						SimpleGeofenceStore store = new SimpleGeofenceStore(context);
						store.setGeofence(Long.toString(event.getID()), geofence);
					} else {
						SimpleGeofenceStore store = new SimpleGeofenceStore(context);
						store.clearGeofence(Long.toString(event.getID()));
					}
				}
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
				
				//TODO: delete reminders? oder nich notwendig?
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