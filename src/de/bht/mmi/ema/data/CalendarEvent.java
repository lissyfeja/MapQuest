package de.bht.mmi.ema.data;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.provider.CalendarContract.Events;

public class CalendarEvent {
	public static final String[] FIELDS = {
		Events.CALENDAR_COLOR,
		Events.CALENDAR_DISPLAY_NAME,
		Events.TITLE,
		Events.DESCRIPTION,
		Events.DTSTART,
		Events.DTEND,
		Events.EVENT_LOCATION,
		Events.HAS_ALARM
		};

	private int calendarColor;
	private String calendarDisplayName;
	private String title;
	private String description;
	private long dtStart;
	private long dtEnd;
	private String location;
	private boolean hasAlarm;

	
	
	public CalendarEvent() {

	}

	
	
	public void setCalendarColor(int calendarColor) {
		this.calendarColor = calendarColor;
	}
	
	public int getCalendarColor() {
		return calendarColor;
	}
	
	public void setCalendarDisplayName(String calendarName) {
		this.calendarDisplayName = calendarName;
	}

	public String getCalendarDisplayName() {
		return calendarDisplayName;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDtStart(long dtStart) {
		this.dtStart = dtStart;
	}

	public long getDtStart() {
		return dtStart;
	}

	public void setDtEnd(long dtEnd) {
		this.dtEnd = dtEnd;
	}

	public long getDtEnd() {
		return dtEnd;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setLocation(Context context, LatLng latLng) {
		Geocoder mGeocoder = new Geocoder(context, Locale.getDefault());
		List<Address> addresses = null;
		try {
			addresses = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 3);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (addresses != null && addresses.size() > 0) {
			// TODO: check if getAddressLine() returns the full address
			this.location = addresses.get(0).getAddressLine(0);
		}
	}

	public String getLocation() {
		return location;
	}

	public void setHasAlarm(boolean hasAlarm) {
		this.hasAlarm = hasAlarm;
	}

	public boolean isHasAlarm() {
		return hasAlarm;
	}

}