package de.bht.mmi.ema.data;

import android.annotation.TargetApi;
import android.os.Build;
import android.provider.CalendarContract.Reminders;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MQReminder {
	public static final String[] FIELDS = {
		Reminders._ID,
		Reminders.EVENT_ID,
		Reminders.MINUTES,
		Reminders.METHOD };
	
	private long ID;
	private long eventID;
	private int minutes;
	private String method;
	
	
	
	public MQReminder() {
		
	}
	
	

	public void setID(long iD) {
		ID = iD;
	}

	public long getID() {
		return ID;
	}
	
	public void setEventID(long eventID) {
		this.eventID = eventID;
	}

	public long getEventID() {
		return eventID;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}
	
}