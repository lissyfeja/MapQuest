package de.bht.mmi.ema.data;

import android.annotation.TargetApi;
import android.os.Build;
import android.provider.CalendarContract.Calendars;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MQCalendar {
	public static final String[] FIELDS = {
		Calendars._ID,
		Calendars.ACCOUNT_NAME,
		Calendars.NAME,
		Calendars.CALENDAR_DISPLAY_NAME,
		Calendars.CALENDAR_COLOR,
		Calendars.VISIBLE
		};
	
	private long calendarID;
	private String accountName;
	private String name;
	private String displayName;
	private boolean visible;
	private int color;

	
	
	public MQCalendar() {

	}

	public MQCalendar(String accountName, String name, String displayName, boolean visible, int color) {
		this.accountName = accountName;
		this.name = name;
		this.displayName = displayName;
		this.visible = visible;
		this.color = color;
	}
	
	
	
	public void setCalendarID(long calendarID) {
		this.calendarID = calendarID;
	}

	public long getCalendarID() {
		return calendarID;
	}
	
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountName() {
		return accountName;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

}