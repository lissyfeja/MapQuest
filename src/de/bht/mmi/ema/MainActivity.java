package de.bht.mmi.ema;

import com.google.android.gms.maps.SupportMapFragment;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Bundle;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private ArrayAdapter<String> navigationAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		navigationAdapter = new ArrayAdapter<String>(getActionBarThemedContextCompat(), android.R.layout.simple_list_item_1, android.R.id.text1,
				new String[] { getString(R.string.title_map), getString(R.string.title_eventlist) });

		if (savedInstanceState == null) {
			// TODO: handle orientation change in listfragment
		}

		// Specify a SpinnerAdapter to populate the dropdown list.
		actionBar.setListNavigationCallbacks(navigationAdapter, this);
	}

	/**
	 * Backward-compatible version of {@link ActionBar#getThemedContext()} that
	 * simply returns the {@link android.app.Activity} if
	 * <code>getThemedContext</code> is unavailable.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private Context getActionBarThemedContextCompat() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getActionBar().getThemedContext();
		} else {
			return this;
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the container view.
		switch (position) {
		case 0:
			FragmentTransaction mTransaction = getSupportFragmentManager().beginTransaction();
			SupportMapFragment mFRaFragment = new MQMapFragment();
			mTransaction.replace(R.id.container, mFRaFragment);
			mTransaction.commit();
			break;
		case 1:
			EventListFragment listFragment = new EventListFragment();
			Bundle args = new Bundle();
			args.putInt(EventListFragment.ARG_SECTION_NUMBER, position + 1);
			listFragment.setArguments(args);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, listFragment).commit();
			break;
		}
		return true;
	}

}
