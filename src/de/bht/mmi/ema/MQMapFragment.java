package de.bht.mmi.ema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import de.bht.mmi.ema.data.CalendarEvent;
import de.bht.mmi.ema.data.CalendarProviderWrapper;
import de.bht.mmi.ema.data.CursorTransformer;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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
	
	private boolean mRoutingMode;
	private List<CalendarEvent> mEvents;
	private Geocoder mGeocoder;
	private Marker mNewEventMarker;

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
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
					CalendarEvent mEvent = new CalendarEvent();
					mEvent.setLocation(mActivity, marker.getPosition());
					intent.putExtra(EditEventActivity.INTENT_EDITEVENT, new Gson().toJson(mEvent));
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
			// TODO: show routing
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return CalendarProviderWrapper.getTodaysEvents(mActivity);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		List<CalendarEvent> events = new ArrayList<CalendarEvent>();
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				CalendarEvent event = CursorTransformer.cursorToEvent(cursor);
				if (event != null) {
					events.add(event);
				}
			}
		}
		mEvents = events;
		
		
		for (CalendarEvent event : mEvents) {
			List<Address> addresses = null;
			try {
				addresses = mGeocoder.getFromLocationName(event.getLocation(), 3);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (addresses != null && addresses.size() > 0) {
				Marker marker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude()))
				.title(event.getTitle()));
			}
			
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
	}

}