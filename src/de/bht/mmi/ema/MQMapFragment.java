package de.bht.mmi.ema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import de.bht.mmi.ema.Geofence.SimpleGeofence;
import de.bht.mmi.ema.Geofence.SimpleGeofenceStore;
import de.bht.mmi.ema.data.MQCalendarEvent;
import de.bht.mmi.ema.data.CalendarProviderWrapper;
import de.bht.mmi.ema.data.CursorTransformer;
import de.bht.mmi.ema.data.MQReminder;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MQMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	private MainActivity mActivity;
	private ActionBar mActionBar;
	private GoogleMap mMap;
	private MenuItem mMenuItemRouting;
	private MenuItem mMenuItemMap;
	private MQCalendarEvent mNewEvent;
	private List<MQCalendarEvent> mEvents = new ArrayList<MQCalendarEvent>();
//	private List<Marker> mMarker = new ArrayList<Marker>();
	private HashMap<Marker, Long> mMarker = new HashMap<Marker, Long>();
	
	
	private SimpleGeofence mGeofence;
	List<SimpleGeofence> mGeofenceList;
	//private SimpleGeofenceStore mGeofenceStorage;
	private boolean mRoutingMode;
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
				Intent intent = new Intent(mActivity, EditEventActivity.class);
				if (mNewEventMarker != null && marker.getId().equals(mNewEventMarker.getId())) {
					mNewEventMarker.remove();
					
//					MQCalendarEvent mEvent = new MQCalendarEvent();
//					mEvent.setLocation(mActivity, marker.getPosition());
					intent.putExtra(EditEventActivity.INTENT_ADDEVENT, new Gson().toJson(mNewEvent));
				} else {
					if (mNewEventMarker != null) {
						mNewEventMarker.remove();
					}
					
					long id = mMarker.get(marker);
					for (int i = 0; i < mEvents.size(); i++) {
						if (id == mEvents.get(i).getID()) {
							intent.putExtra(EditEventActivity.INTENT_EDITEVENT, new Gson().toJson(mEvents.get(i)));
							break;
						}
					}
				}
	            startActivity(intent);
			}
		});
		mMap.setOnMapClickListener(new OnMapClickListener() {
			@Override
			public void onMapClick(LatLng latlng) {
				if (mNewEventMarker != null) {
					mNewEventMarker.remove();
				}
				
				if (mNewEvent == null) {
					mNewEvent = new MQCalendarEvent();	
				}
				
				mNewEvent.setLocation(mActivity, latlng);
				
				mNewEventMarker = mMap.addMarker(new MarkerOptions()
				.icon(null)
				.position(latlng)
				.snippet(mNewEvent.getLocation())
				.title("Click to add new event here"));
				mNewEventMarker.showInfoWindow();
			}
		});
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.isInfoWindowShown()) {
					marker.hideInfoWindow();
				} else {
					marker.showInfoWindow();
				}
				
				if (mNewEventMarker != null) {
					mNewEventMarker.remove();
				}
				return true;
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
			
			// TODO: show our own position
			// TODO: show routes from our position to first event to second event to third...
			// TODO: (only if we have time) show split actionbar like in concept
			
//			createRoute();
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
		startActivity(intent);
					 
			 
		// }
	 }
	 
	 

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == 0) {
			return CalendarProviderWrapper.getTodaysEvents(mActivity);
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader != null) {
			if (loader.getId() == 0) {
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						MQCalendarEvent event = CursorTransformer.cursorToEvent(cursor);
						if (event != null) {
							SimpleGeofenceStore store = new SimpleGeofenceStore(mActivity);
							SimpleGeofence geofence = store.getGeofence(Long.toString(event.getID()));
							if (geofence != null) {
								event.setGeofenceReminder(geofence);

								CircleOptions mCircleOptions = new CircleOptions();
								mCircleOptions.center(new LatLng(geofence.getLatitude(), geofence.getLongitude()));
								mCircleOptions.radius(geofence.getRadius());
								mCircleOptions.strokeColor(Color.YELLOW);
								mCircleOptions.fillColor(Color.argb(40, 50, 60, 70));
								mMap.addCircle(mCircleOptions);
							}
							mEvents.add(event);

							List<Address> addresses = event.getAddresses(mActivity);
							if (addresses != null && addresses.size() > 0) {
								Marker marker = mMap.addMarker(new MarkerOptions()
										.position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
										.snippet(event.getLocation()).title(event.getTitle()));

								mMarker.put(marker, event.getID());
							}
						}
					}
				}

				if (mEvents != null && mEvents.size() > 0) {
					getLoaderManager().initLoader(2, null, MQMapFragment.this);
				}
			}
		}

//		for (MQCalendarEvent event : mEvents) {
//			List<Address> addresses = event.getAddresses(mActivity);
//			
//			if (addresses != null && addresses.size() > 0) {
//				Marker marker = mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
//				.snippet(event.getLocation())
//				.title(event.getTitle()));
//				marker.showInfoWindow();
//				
////				createGeofences(addresses.get(0).getLatitude(), addresses.get(0).getLongitude(), 300);
//			}
//		}
		
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}

}