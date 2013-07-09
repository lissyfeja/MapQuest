package de.bht.mmi.ema;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import de.bht.mmi.ema.data.MQCalendarEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; 
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.content.Context; 

public class EventListAdapter extends ArrayAdapter<MQCalendarEvent> implements StickyListHeadersAdapter {
	private Context mContext;
	private List<MQCalendarEvent> mEvents;
	private LayoutInflater mInflater;
	
	

	public EventListAdapter(final Context context) {
		super(context, 0);
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
	}
	
	
	
	public void setEvents(List<MQCalendarEvent> events) {
		this.mEvents = events;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return (mEvents == null) ? 0 : mEvents.size();
	}

	@Override
	public MQCalendarEvent getItem(int position) {
		return (mEvents == null) ? null : mEvents.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public int getPosition(long time) {
		int pos = 0;
		MQCalendarEvent minEvent = mEvents.get(0);
		for (int i = 1; i < mEvents.size(); i++) {
			if (Math.abs(mEvents.get(i).getDtStart() - time) < Math.abs(minEvent.getDtStart() - time)) {
				pos = i;
				minEvent = mEvents.get(i);
			}
		}
		return pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder;
		
		if (convertView == null) {
			holder = new ItemViewHolder();
			convertView = mInflater.inflate(R.layout.fragment_list_item, parent, false);
			holder.color = (View) convertView.findViewById(R.id.item_color);
			holder.title = (TextView) convertView.findViewById(R.id.item_title);
			holder.time = (TextView) convertView.findViewById(R.id.item_time);
			holder.description = (TextView) convertView.findViewById(R.id.item_description);
			holder.location = (TextView) convertView.findViewById(R.id.item_location);
			convertView.setTag(holder);
		} else {
			holder = (ItemViewHolder) convertView.getTag();
		}
		
		MQCalendarEvent event = mEvents.get(position);
		holder.color.setBackgroundColor(event.getCalendarColor());
		holder.title.setText(event.getTitle());
		holder.time.setText(MQCalendarEvent.DATE_FORMAT_TIME.format(event.getDtStart()) + " - " + MQCalendarEvent.DATE_FORMAT_TIME.format(event.getDtEnd()));
		holder.description.setText(event.getDescription());
		holder.location.setText(event.getLocation());
		return convertView;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder holder;
		if (convertView == null) {
			holder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.fragment_list_header, parent, false);
			holder.text = (TextView) convertView.findViewById(R.id.header_text);
			convertView.setTag(holder);
		} else {
			holder = (HeaderViewHolder) convertView.getTag();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mEvents.get(position).getDtStart());
		holder.text.setText(MQCalendarEvent.DATE_FORMAT.format(calendar.getTime()));
		
		return convertView;
	}

	@Override
	public long getHeaderId(int position) {
		if (mEvents == null) {
			return 0;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mEvents.get(position).getDtStart());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		return calendar.getTimeInMillis();
	}
	
	private class ItemViewHolder {
		public View color;
		public TextView title;
		public TextView time;
		public TextView description;
		public TextView location;
	}
	
	private class HeaderViewHolder {
		public TextView text;
	}

}