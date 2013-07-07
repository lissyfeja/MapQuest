package de.bht.mmi.ema;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersListView;
import com.google.gson.Gson;

import de.bht.mmi.ema.data.CalendarEvent;
import de.bht.mmi.ema.data.CalendarProviderWrapper;
import de.bht.mmi.ema.data.CursorTransformer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public class EventListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener, ListView.OnScrollListener {
	/**
	 * The fragment argument representing the section number for this fragment.
	 */
	public static final String ARG_SECTION_NUMBER = "section_number";

	private MainActivity mActivity;
	private ActionBar mActionBar;
	private EventListAdapter mEventListAdapter;
	private StickyListHeadersListView mStickyHeadersListView;

	private int mFrom = 10;
	private int mTo = 50;
	boolean mLoading = false;

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.mActivity = (MainActivity) getActivity();
		this.mActionBar = mActivity.getActionBar();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View rootView = inflater.inflate(R.layout.fragment_list, container, false);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// List<Calendar> calendars =
		// CalendarProviderWrapper.getCalendars(mActivity);
		// for (Calendar cal: calendars) {
		// Log.d("MapQuest", cal.getDisplayName());
		// }

		mEventListAdapter = new EventListAdapter(mActivity);
		mStickyHeadersListView = (StickyListHeadersListView) getView().findViewById(R.id.list);
		mStickyHeadersListView.setDrawingListUnderStickyHeader(false);
		mStickyHeadersListView.setAdapter(mEventListAdapter);
		mStickyHeadersListView.setOnItemClickListener(this);
		mStickyHeadersListView.setOnScrollListener(this);

		getLoaderManager().initLoader(0, null, EventListFragment.this);
	}

	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_list, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		if (menu != null) {
			menu.findItem(R.id.action_add);
			menu.findItem(R.id.action_today);
			menu.findItem(R.id.action_settings);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
            Intent intent = new Intent(mActivity, EditEventActivity.class);
            startActivity(intent);
			return true;
		case R.id.action_today:
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			mStickyHeadersListView.smoothScrollToPosition(mEventListAdapter.getPosition(calendar.getTimeInMillis()));
			return true;
		case R.id.action_settings:
			// TODO: start settings activity
			return true;
		}
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d("MapQuest", "onCreateLoader()");
		// return CalendarProviderWrapper.getAllEvents(mActivity);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - mFrom);
		long from = cal.getTimeInMillis();
		cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + mTo);
		long to = cal.getTimeInMillis();

		return CalendarProviderWrapper.getEventsFromTo(mActivity, from, to);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		Log.d("MapQuest", "onLoadFinished()");

		List<CalendarEvent> events = new ArrayList<CalendarEvent>();
		if (cursor.getCount() > 0) {
			Log.d("MapQuest", "onLoadFinished()!!!!!!===0");
			while (cursor.moveToNext()) {
				CalendarEvent event = CursorTransformer.cursorToEvent(cursor);
				if (event != null) {
					events.add(event);
				}
			}
		}
		mEventListAdapter.setEvents(events);
		mLoading = false;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// nothing to do here
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
		String json = new Gson().toJson(mEventListAdapter.getItem(position));
        Intent intent = new Intent(mActivity, EditEventActivity.class);
        intent.putExtra(EditEventActivity.INTENT_EDITEVENT, json);
        startActivity(intent);
	}
	

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (visibleItemCount > 0) {
			if (firstVisibleItem + visibleItemCount >= totalItemCount - 1 && !mLoading) {
				// if scrolled to bottom:
				mTo += 50;
				if (mFrom + mTo >= 300) {
					mFrom -= 50;
				}
				Log.d("MapQuest", "mTo: " + Integer.toString(mTo));
				mLoading = true;
				getLoaderManager().restartLoader(0, null, EventListFragment.this);
			}
			// else if (firstVisibleItem == 0) {
			// mFrom += 50;
			// if (mFrom + mTo >= 300) {
			// mTo -= 50;
			// }
			// Log.d("MapQuest", "mFrom: " + Integer.toString(mFrom));
			// mLoading = true;
			// getLoaderManager().restartLoader(0, null,
			// EventListFragment.this);
			// }
		}
		Log.d("MapQuest", "firstVisibleItem: " + Integer.toString(firstVisibleItem));
		Log.d("MapQuest", "visibleItemCount: " + Integer.toString(visibleItemCount));
		Log.d("MapQuest", "totalItemCount:   " + Integer.toString(totalItemCount));
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

}