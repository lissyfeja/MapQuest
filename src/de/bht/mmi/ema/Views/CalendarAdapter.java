package de.bht.mmi.ema.Views;

import java.util.List;

import de.bht.mmi.ema.R;
import de.bht.mmi.ema.data.MQCalendar;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CalendarAdapter extends ArrayAdapter<MQCalendar> {
	private LayoutInflater mLayoutInflater;
	private List<MQCalendar> mCalendars;
	
	
	
	public CalendarAdapter(Activity activity, int textViewResourceId, List<MQCalendar> calendars) {
		super(activity, R.layout.fragment_editevent_calendarspinner_item, calendars);
		
		this.mLayoutInflater = activity.getLayoutInflater();
		this.mCalendars = calendars;
	}
	
	
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = instantiateView(parent);
		}
		
		MQCalendar calendar = mCalendars.get(position);
		bindView(position, calendar, convertView, true);
		
		return convertView;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = instantiateView(parent);
		}
		
		MQCalendar calendar = mCalendars.get(position);
		bindView(position, calendar, convertView, false);
		
		return convertView;
	}
	
	private View instantiateView(ViewGroup parent) {
		View view = mLayoutInflater.inflate(R.layout.fragment_editevent_calendarspinner_item, parent, false);
		
		final CalendarViewHolder viewHolder = new CalendarViewHolder();
		viewHolder.color = (View) view.findViewById(R.id.calendar_spinner_color);
		viewHolder.name = (TextView) view.findViewById(R.id.calendar_spinner_name);
		viewHolder.accountName = (TextView) view.findViewById(R.id.calendar_spinner_accountname);
		
		view.setTag(viewHolder);
		return view;
	}
	
	private void bindView(final int position, final MQCalendar calendar, View view, boolean showColor) {
		final CalendarViewHolder viewHolder = (CalendarViewHolder) view.getTag(); 
		viewHolder.color.setBackgroundColor(calendar.getColor());
		if (showColor) {
			viewHolder.color.setVisibility(View.VISIBLE);
		} else {
			viewHolder.color.setVisibility(View.INVISIBLE);
		}
		viewHolder.name.setText(calendar.getDisplayName());
		viewHolder.accountName.setText(calendar.getAccountName());
	}
	
	private static final class CalendarViewHolder {
		public View color;
		public TextView name;
		public TextView accountName;
	}
	
}