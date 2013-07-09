package de.bht.mmi.ema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import de.bht.mmi.ema.Geofence.SimpleGeofence;
import de.bht.mmi.ema.data.MQCalendarEvent;
import de.bht.mmi.ema.data.CalendarProviderWrapper;
import de.bht.mmi.ema.data.CursorTransformer;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class MQMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private MainActivity mActivity;
	private ActionBar mActionBar;
	private GoogleMap mMap;
	private MenuItem mMenuItemRouting;
	private MenuItem mMenuItemMap;
	private SimpleGeofence mGeofence;
	List<SimpleGeofence> mGeofenceList;
	//private SimpleGeofenceStore mGeofenceStorage;
	
	
	private boolean mRoutingMode;
	private List<MQCalendarEvent> mEvents;
	private Geocoder mGeocoder;
	private Marker mNewEventMarker;
	
	private static final long SECONDS_PER_HOUR = 60;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
            SECONDS_PER_HOUR *
            MILLISECONDS_PER_SECOND;

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		mGeofenceList = new ArrayList<SimpleGeofence>();
		super.onCreate(savedInstanceState);

		this.mActivity = (MainActivity) getActivity();
		this.mActionBar = mActivity.getActionBar();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		
		
		mMap = getMap();
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				if (marker.getId().equals(mNewEventMarker.getId())) {
					mNewEventMarker.remove();
					Intent intent = new Intent(mActivity, EditEventActivity.class);
					MQCalendarEvent mEvent = new MQCalendarEvent();
					mEvent.setLocation(mActivity, marker.getPosition());
					intent.putExtra(EditEventActivity.INTENT_ADDEVENT, new Gson().toJson(mEvent));
		            startActivity(intent);
				} else {
					mNewEventMarker.remove();
				}
			}
		});
		mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latlng) {
				if (mNewEventMarker != null) {
					mNewEventMarker.remove();
				}
				
				mNewEventMarker = mMap.addMarker(new MarkerOptions()
				.icon(null)
				.position(latlng)
				.title("Click to add new event here"));
				mNewEventMarker.showInfoWindow();
			}
		});
		mGeocoder = new Geocoder(mActivity, Locale.getDefault());

		
		if (savedInstanceState != null) {
			// TODO: get objects, variables and states from savedInstanceState
			
		} else {
			getLoaderManager().initLoader(0, null, MQMapFragment.this);
		}
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // TODO: save state on orientation change
    }
    
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_map, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (menu != null) {
			mMenuItemRouting = menu.findItem(R.id.action_map_routing);
			mMenuItemMap = menu.findItem(R.id.action_map_map).setVisible(false);
			menu.findItem(R.id.action_settings);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_map_routing:
			mMenuItemRouting.setVisible(false);
			mMenuItemMap.setVisible(true);
			mRoutingMode = true;
			createRoute();
			return true;
		case R.id.action_map_map:
			mMenuItemRouting.setVisible(true);
			mMenuItemMap.setVisible(false);
			mRoutingMode = false;
			// TODO: show normal map with events
			return true;
		case R.id.action_settings:
			// TODO: start settings activity
			return true;
		}
		return false;
	}
	
	 public void createGeofences(double lat, double lon, float rad) {
		 mGeofence = new SimpleGeofence(
                "1",
                lat,
                lon,
                rad,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER);
	      mGeofenceList.add(mGeofence);
	      CircleOptions mCircleOptions = new CircleOptions();
	      mCircleOptions.center(new LatLng(lat,lon));
	      mCircleOptions.radius(100);
	      mCircleOptions.strokeColor(Color.YELLOW);
	      mCircleOptions.fillColor(Color.argb(40, 50, 60, 70));
	      mMap.addCircle(mCircleOptions);
	      
		 
    }
	 
	 public void createRoute(){
		// for(int i = 0; i< mGeofenceList.size(); i++){
		SimpleGeofence start = mGeofenceList.get(0);
		SimpleGeofence end = mGeofenceList.get(1);
		final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + "saddr=" + start.getLatitude() + ","
				+ start.getLongitude() + "&daddr=" + end.getLatitude() + "," + end.getLongitude()));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
//		startActivity(intent);
					 
			 
		// }
	 }
	 
	 

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return CalendarProviderWrapper.getTodaysEvents(mActivity);
		//Calendar cal = Calendar.getInstance();
		//cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 10);
		//long from = cal.getTimeInMillis();
		//cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 50);
		//long to = cal.getTimeInMillis();

		//return CalendarProviderWrapper.getEventsFromTo(mActivity, from, to);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		List<MQCalendarEvent> events = new ArrayList<MQCalendarEvent>();
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				MQCalendarEvent event = CursorTransformer.cursorToEvent(cursor);
				if (event != null) {
					events.add(event);
				}
			}
		}
		mEvents = events;

		for (MQCalendarEvent event : mEvents) {
			List<Address> addresses = event.getAddresses(mActivity);
			
			if (addresses != null && addresses.size() > 0) {
				Marker marker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
				.snippet(event.getLocation())
				.title(event.getTitle()));
				marker.showInfoWindow();
				
				createGeofences(addresses.get(0).getLatitude(), addresses.get(0).getLongitude(), 300);
			}
		}
		
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}

}