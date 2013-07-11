package de.bht.mmi.ema;

import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationStatusCodes;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class GeofenceActivity extends FragmentActivity 
		implements ConnectionCallbacks,
		OnConnectionFailedListener,
		LocationListener,
		OnAddGeofencesResultListener,
		OnRemoveGeofencesResultListener {
	
	// Holds the location client
    public LocationClient mLocationClient;
    
    public Location mCurrentLocation;
	// Define an object that holds accuracy and frequency parameters
	private LocationRequest mLocationRequest;
	
	private MQLocationListener mMQLocationListener;
    
	public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
	private static final long UPDATE_INTERVAL = 1000 * UPDATE_INTERVAL_IN_SECONDS;
	
	public static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	private static final long FASTEST_INTERVAL = 1000 * FASTEST_INTERVAL_IN_SECONDS;
    
    
    // Stores the PendingIntent used to request geofence monitoring
    private PendingIntent mGeofenceRequestIntent;
    
    private PendingIntent mTransitionPendingIntent;
    
    // Defines the allowable request types.
    // Enum type for controlling the type of removal requested
    public enum REQUEST_TYPE {ADD, REMOVE_INTENT, REMOVE_LIST}
    
    private REQUEST_TYPE mRequestType;
    
    // Flag that indicates if a request is underway.
    private boolean mInProgress;
    
 // Store the list of geofence Ids to remove
    private List<String> mGeofencesToRemove;
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
        // Start with the request flag set to false
        mInProgress = false;

		// Create a new location client, using the enclosing class to handle callbacks.
		mLocationClient = new LocationClient(this, this, this);

		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		
		
    }
    
    public void setMQLocationListener(MQLocationListener mQLocationListener) {
    	this.mMQLocationListener = mQLocationListener;
    }
	
	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
		
	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	protected void onStop() {
		if (mLocationClient.isConnected()) {
			/*
			 * Remove location updates for a listener. The current Activity is
			 * the listener, so the argument is "this".
			 */
			mLocationClient.removeLocationUpdates(this);
		}
		
		/*
         * After disconnect() is called, the client is
         * considered "dead".
         */
		mLocationClient.disconnect();
		
		super.onStop();
	}
    
    
    
    
    
    
    
    
    
	// Global constants
	/*
	 * Define a request code to send to Google Play services This code is
	 * returned in Activity.onActivityResult
	 */
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	// Define a DialogFragment that displays the error dialog
	public static class ErrorDialogFragment extends DialogFragment {
		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}

	}

	/*
	 * Handle results returned to the FragmentActivity by Google Play services
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			// If the result code is Activity.RESULT_OK, try to connect again
			switch (resultCode) {
			case Activity.RESULT_OK:
				// Try the request again
				break;
			}

		}

	}

	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Geofence Detection", "Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
//			// Get the error code
//			int errorCode = connectionResult.getErrorCode();
//			// Get the error dialog from Google Play services
//			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
//
//			// If Google Play services can provide an error dialog
//			if (errorDialog != null) {
//				// Create a new DialogFragment for the error dialog
//				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
//				// Set the dialog in the DialogFragment
//				errorFragment.setDialog(errorDialog);
//				// Show the error dialog in the DialogFragment
//				errorFragment.show(getSupportFragmentManager(), "Geofence Detection");
//			}
			return false;
		}
	}
    
    
    
    
    
    
    
    
    
    
    
    
	
    
    /**
     * Start a request for geofence monitoring by calling
     * LocationClient.connect().
     */
	public void addGeofences() {
		// Start a request to add geofences
		mRequestType = REQUEST_TYPE.ADD;
		/*
		 * Test for Google Play services after setting the request type. If
		 * Google Play services isn't present, the proper request can be
		 * restarted.
		 */
		if (!servicesConnected()) {
			return;
		}
		/*
		 * Create a new location client object. Since the current activity class
		 * implements ConnectionCallbacks and OnConnectionFailedListener, pass
		 * the current activity object as the listener for both parameters
		 */
		mLocationClient = new LocationClient(this, this, this);
		// If a request is not already underway
		if (!mInProgress) {
			// Indicate that a request is underway
			mInProgress = true;
			// Request a connection from the client to Location Services
			mLocationClient.connect();
		} else {
			/*
			 * A request is already underway. You can handle this situation by
			 * disconnecting the client, re-setting the flag, and then re-trying
			 * the request.
			 */
		}
	}
	
	/**
	 * Start a request to remove geofences by calling LocationClient.connect()
	 */
	public void removeGeofences(PendingIntent requestIntent) {
		// Record the type of removal request
		mRequestType = REQUEST_TYPE.REMOVE_INTENT;
		/*
		 * Test for Google Play services after setting the request type. If
		 * Google Play services isn't present, the request can be restarted.
		 */
		if (!servicesConnected()) {
			return;
		}
		// Store the PendingIntent
		mGeofenceRequestIntent = requestIntent;
		/*
		 * Create a new location client object. Since the current activity class
		 * implements ConnectionCallbacks and OnConnectionFailedListener, pass
		 * the current activity object as the listener for both parameters
		 */
		mLocationClient = new LocationClient(this, this, this);
		// If a request is not already underway
		if (!mInProgress) {
			// Indicate that a request is underway
			mInProgress = true;
			// Request a connection from the client to Location Services
			mLocationClient.connect();
		} else {
			/*
			 * A request is already underway. You can handle this situation by
			 * disconnecting the client, re-setting the flag, and then re-trying
			 * the request.
			 */
		}
	}
	
	/**
	 * Start a request to remove monitoring by calling LocationClient.connect()
	 * 
	 */
	public void removeGeofences(List<String> geofenceIds) {
		// If Google Play services is unavailable, exit
		// Record the type of removal request
		mRequestType = REQUEST_TYPE.REMOVE_LIST;
		/*
		 * Test for Google Play services after setting the request type. If
		 * Google Play services isn't present, the request can be restarted.
		 */
		if (!servicesConnected()) {
			return;
		}
		// Store the list of geofences to remove
		mGeofencesToRemove = geofenceIds;
		/*
		 * Create a new location client object. Since the current activity class
		 * implements ConnectionCallbacks and OnConnectionFailedListener, pass
		 * the current activity object as the listener for both parameters
		 */
		mLocationClient = new LocationClient(this, this, this);
		// If a request is not already underway
		if (!mInProgress) {
			// Indicate that a request is underway
			mInProgress = true;
			// Request a connection from the client to Location Services
			mLocationClient.connect();
		} else {
			/*
			 * A request is already underway. You can handle this situation by
			 * disconnecting the client, re-setting the flag, and then re-trying
			 * the request.
			 */
		}
	}
    
    
	/*
	 * Create a PendingIntent that triggers an IntentService in your app when a
	 * geofence transition occurs.
	 */
	private PendingIntent getTransitionPendingIntent() {
		// Create an explicit Intent
		Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
		/*
		 * Return the PendingIntent
		 */
		return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	 /*
     * Provide the implementation of
     * OnAddGeofencesResultListener.onAddGeofencesResult.
     * Handle the result of adding the geofences
     *
     */
	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		// If adding the geofences was successful
        if (LocationStatusCodes.SUCCESS == statusCode) {
            /*
             * Handle successful addition of geofences here.
             * You can send out a broadcast intent or update the UI.
             * geofences into the Intent's extended data.
             */
        } else {
        // If adding the geofences failed
            /*
             * Report errors here.
             * You can log the error using Log.e() or update
             * the UI.
             */
        }
        // Turn off the in progress flag and disconnect the client
        mInProgress = false;
        mLocationClient.disconnect();
	}

	// Implementation of OnConnectionFailedListener.onConnectionFailed
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Turn off the request flag
		mInProgress = false;
		/*
		 * If the error has a resolution, start a Google Play services activity
		 * to resolve it.
		 */
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			// Get the error code
			int errorCode = connectionResult.getErrorCode();
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				// Set the dialog in the DialogFragment
				errorFragment.setDialog(errorDialog);
				// Show the error dialog in the DialogFragment
				errorFragment.show(getSupportFragmentManager(), "Geofence Detection");
			}
		}
	}

	/*
     * Provide the implementation of ConnectionCallbacks.onConnected()
     * Once the connection is available, send a request to add the
     * Geofences
     */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(mLocationRequest, this);
//		switch (mRequestType) {
//		case ADD:
//			// Get the PendingIntent for the request
//			mTransitionPendingIntent = getTransitionPendingIntent();
//			// Send a request to add the current geofences
//			mLocationClient.addGeofences(mCurrentGeofences, pendingIntent, this);
//		case REMOVE_INTENT:
//			mLocationClient.removeGeofences(mGeofenceRequestIntent, this);
//			break;
//		case REMOVE_LIST:
//			// If removeGeofencesById was called
//			mLocationClient.removeGeofences(mGeofencesToRemove, this);
//			break;
//		}
	}
	
	/**
     * When the request to remove geofences by IDs returns, handle the
     * result.
     *
     * @param statusCode The code returned by Location Services
     * @param geofenceRequestIds The IDs removed
     */
	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
		// If removing the geocodes was successful
		if (LocationStatusCodes.SUCCESS == statusCode) {
			/*
			 * Handle successful removal of geofences here. You can send out a
			 * broadcast intent or update the UI. geofences into the Intent's
			 * extended data.
			 */
		} else {
			// If removing the geofences failed
			/*
			 * Report errors here. You can log the error using Log.e() or update
			 * the UI.
			 */
		}
		// Indicate that a request is no longer in progress
		mInProgress = false;
		// Disconnect the location client
		mLocationClient.disconnect();
	}
	
	 /**
     * When the request to remove geofences by PendingIntent returns,
     * handle the result.
     *
     *@param statusCode the code returned by Location Services
     *@param requestIntent The Intent used to request the removal.
     */
	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent requestIntent) {
		// If removing the geofences was successful
		if (statusCode == LocationStatusCodes.SUCCESS) {
			/*
			 * Handle successful removal of geofences here. You can send out a
			 * broadcast intent or update the UI. geofences into the Intent's
			 * extended data.
			 */
		} else {
			// If adding the geocodes failed
			/*
			 * Report errors here. You can log the error using Log.e() or update
			 * the UI.
			 */
		}
		/*
		 * Disconnect the location client regardless of the request status, and
		 * indicate that a request is no longer in progress
		 */
		mInProgress = false;
		mLocationClient.disconnect();
	}

	/*
     * Implement ConnectionCallbacks.onDisconnected()
     * Called by Location Services once the location client is
     * disconnected.
     */
	@Override
	public void onDisconnected() {
		// Turn off the request flag
        mInProgress = false;
        // Destroy the current location client
        mLocationClient = null;
	}

	// Define the callback method that receives location updates
	@Override
	public void onLocationChanged(Location location) {
//		Log.d("MapQuest", "GeofenceActivity: " + location.toString());
		// TODO Auto-generated method stub
		if (mMQLocationListener != null) {
			mMQLocationListener.onUserLocationChanged(location);
		}
	}


}