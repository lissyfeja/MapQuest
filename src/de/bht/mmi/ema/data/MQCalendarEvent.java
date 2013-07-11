package de.bht.mmi.ema.data;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.provider.CalendarContract.Events;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("SimpleDateFormat")
public class MQCalendarEvent {
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, dd. MMMM y");
	public static final SimpleDateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("EE, dd. MMM y");
	public static final SimpleDateFormat DATE_FORMAT_TIME = new SimpleDateFormat("HH:mm");
	
	public static final String[] FIELDS = {
		Events._ID,
		Events.CALENDAR_ID,
		Events.CALENDAR_COLOR,
		Events.CALENDAR_DISPLAY_NAME,
		Events.TITLE,
		Events.DESCRIPTION,
		Events.DTSTART,
		Events.DTEND,
		Events.EVENT_LOCATION,
		Events.HAS_ALARM
		};
	
	private long ID;
	private long calendarID;
	private int calendarColor;
	private String calendarDisplayName;
	private String title;
	private String description;
	private long dtStart;
	private long dtEnd;
	private String location;
	private boolean hasAlarm;
	
	private List<MQReminder> reminders;
	private long reminderToDelete;

	
	
	public MQCalendarEvent() {

	}
	
	
	
	public void setID(long iD) {
		ID = iD;
	}
	
	public long getID() {
		return ID;
	}
	
	public void setCalendarID(long calendarID) {
		this.calendarID = calendarID;
	}
	
	public long getCalendarID() {
		return calendarID;
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
			Address add = addresses.get(0);
			int max = add.getMaxAddressLineIndex();
			this.location = "";
			if (max != -1) {
				for (int i = 0; i < max; i++) {
					this.location += add.getAddressLine(i);
					if (i < max - 1) {
						this.location += ", ";
					}
				}
			}
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
	
	/**
	 * Gets a list of maximum three possible addresses to the events location.
	 * @param geocoder
	 * @return may return an empty list
	 */
	public List<Address> getAddresses(Context context) {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		 
		List<Address> addresses = new ArrayList<Address>();
		if (this.location != null && !this.location.equals("")) {
			try {
				addresses = geocoder.getFromLocationName(this.location, 3);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return addresses;
	}
	
//	public String getAddressLine(Context context) {
//		String line = "";
//		List<Address> addresses = getAddresses(context);
//		if (addresses != null && addresses.size() > 0) {
//			Address add = addresses.get(0);
//			int max = add.getMaxAddressLineIndex();
//			if (max != -1) {
//				for (int i = 0; i < max; i++) {
//					line += add.getAddressLine(i);
//					if (i < max - 1) {
//						line += ", ";
//					}
//				}
//			}
//		}
//		return line;
//	}

	public void setReminders(List<MQReminder> reminders) {
		if (reminders == null) {
			if (this.reminders != null) {
				this.reminderToDelete = this.reminders.get(0).getID();
			}
			this.hasAlarm = false;
		} else {
			this.hasAlarm = true;
		}
		this.reminders = reminders;
	}
	
	public List<MQReminder> getReminders() {
		return reminders;
	}
	
	public long getReminderToDelete() {
		return this.reminderToDelete;
	}

}