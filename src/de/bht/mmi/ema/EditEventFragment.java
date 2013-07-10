package de.bht.mmi.ema;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import de.bht.mmi.ema.Database.ProviderWrapper;
import de.bht.mmi.ema.Views.CalendarAdapter;
import de.bht.mmi.ema.data.CalendarProviderWrapper;
import de.bht.mmi.ema.data.CursorTransformer;
import de.bht.mmi.ema.data.MQCalendar;
import de.bht.mmi.ema.data.MQCalendarEvent;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;

public class EditEventFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {
	private static final String CALENDAR_ARGUMENT = "calendarEventToEdit";
	private static final String EDITMODE_ARGUMENT = "editMode";
	
	private MQCalendarEvent mEvent;
	
	private EditEventActivity mActivity;
	private ActionBar mActionBar;
	private boolean mEditMode;
	
	private ScrollView mScrollView;
	private ImageView mTransparentImageView;
	private LinearLayout mCalendarSpinnerBackground;
	private RelativeLayout mMapViewBackground;
	private Spinner mSpinnerCalendar;
	private EditText mEditTextTitle;
	private EditText mEditTextLocation;
	private ImageButton mImageButtonMap;
	private MapView mMapView;
	private Button mButtonFromDate;
	private Button mButtonFromTime;
	private Button mButtonToDate;
	private Button mButtonToTime;
	private EditText mEditTextNotes;
	
	private List<MQCalendar> mCalendars;
	private Marker mNewEventMarker;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.mActivity = (EditEventActivity) getActivity();
		this.mActionBar = mActivity.getActionBar();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View rootView = inflater.inflate(R.layout.fragment_editevent, container, false);
		this.mScrollView = (ScrollView) rootView.findViewById(R.id.edit_scrollview);
		this.mMapViewBackground = (RelativeLayout) rootView.findViewById(R.id.edit_map_backgroudn);
		this.mTransparentImageView = (ImageView) rootView.findViewById(R.id.transparent_image);
		this.mTransparentImageView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_MOVE:
					mScrollView.requestDisallowInterceptTouchEvent(true);
					return false;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					mScrollView.requestDisallowInterceptTouchEvent(false);
					return true;
				default:
					return true;
				}
			}
		});
		this.mCalendarSpinnerBackground = (LinearLayout) rootView.findViewById(R.id.edit_calendar_background);
		this.mSpinnerCalendar = (Spinner) rootView.findViewById(R.id.edit_calendar);
		this.mEditTextTitle = (EditText) rootView.findViewById(R.id.edit_title);
		this.mEditTextLocation = (EditText) rootView.findViewById(R.id.edit_location);
		this.mImageButtonMap = (ImageButton) rootView.findViewById(R.id.edit_location_map);
		this.mMapView = (MapView) rootView.findViewById(R.id.mapview);
		this.mMapView.onCreate(savedInstanceState);
		this.mButtonFromDate = (Button) rootView.findViewById(R.id.edit_from_date);
		this.mButtonFromTime = (Button) rootView.findViewById(R.id.edit_from_time);
		this.mButtonToDate = (Button) rootView.findViewById(R.id.edit_to_date);
		this.mButtonToTime = (Button) rootView.findViewById(R.id.edit_to_time);
		this.mEditTextNotes = (EditText) rootView.findViewById(R.id.edit_notes);
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mActionBar.setCustomView(R.layout.actionbar_savecancel);
		View view = mActionBar.getCustomView();
		view.findViewById(R.id.actionbar_cancel).setOnClickListener(this);
		view.findViewById(R.id.actionbar_save).setOnClickListener(this);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);
		
		if (savedInstanceState != null) {
			String json = savedInstanceState.getString(CALENDAR_ARGUMENT);
			mEvent = new Gson().fromJson(json, MQCalendarEvent.class);
			getLoaderManager().restartLoader(1, null, EditEventFragment.this);
			
			mEditMode = savedInstanceState.getBoolean(EDITMODE_ARGUMENT);
		} else {
			mEvent = mActivity.mEvent;
			mEditMode = mActivity.mEditMode;
			getLoaderManager().initLoader(1, null, EditEventFragment.this);
		}
		
		mSpinnerCalendar.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (mCalendars != null && mCalendars.size() > position) {
					mCalendarSpinnerBackground.setBackgroundColor(mCalendars.get(position).getColor());
					mEvent.setCalendarID(mCalendars.get(position).getCalendarID());
					mEvent.setCalendarDisplayName(mCalendars.get(position).getDisplayName());
					mEvent.setCalendarColor(mCalendars.get(position).getColor());
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		mImageButtonMap.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMapViewBackground.getVisibility() == View.GONE) {
					mMapViewBackground.setVisibility(View.VISIBLE);
				} else if (mMapViewBackground.getVisibility() == View.VISIBLE) {
					mMapViewBackground.setVisibility(View.GONE);
				}
			}
		});
		
		mEditTextTitle.setText(mEvent.getTitle());
//		mEditTextLocation.setText(mEvent.getAddressLine(mActivity));
		mEditTextLocation.setText(mEvent.getLocation());
		mButtonFromDate.setText(MQCalendarEvent.DATE_FORMAT_SHORT.format(mEvent.getDtStart()));
		mButtonFromTime.setText(MQCalendarEvent.DATE_FORMAT_TIME.format(mEvent.getDtStart()));
		mButtonToDate.setText(MQCalendarEvent.DATE_FORMAT_SHORT.format(mEvent.getDtEnd()));
		mButtonToTime.setText(MQCalendarEvent.DATE_FORMAT_TIME.format(mEvent.getDtEnd()));
		mEditTextNotes.setText(mEvent.getDescription());
		
		mButtonFromDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDatePickerDialog(mButtonFromDate, 0);
			}
		});
		mButtonFromTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showTimePickerDialog(mButtonFromTime, 0);
			}
		});
		mButtonToDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDatePickerDialog(mButtonToDate, 1);
			}
		});
		mButtonToTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showTimePickerDialog(mButtonToTime, 1);
			}
		});
		
		final GoogleMap gMap = mMapView.getMap();
		if (gMap != null) {
			gMap.setOnMapClickListener(new OnMapClickListener() {
				@Override
				public void onMapClick(LatLng latlng) {
					if (mNewEventMarker != null) {
						mNewEventMarker.remove();
					}

					mNewEventMarker = gMap.addMarker(new MarkerOptions().icon(null).position(latlng));

					mEvent.setLocation(mActivity, latlng);
					mEditTextLocation.setText(mEvent.getLocation());
				}
			});
		}
		
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putString(CALENDAR_ARGUMENT, new Gson().toJson(mEvent));
        outState.putBoolean(EDITMODE_ARGUMENT, mEditMode);
    }
    
    @Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();
	}
    

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}
	
	private void showTimePickerDialog(final Button button, final int setTime) {
		String[] text = button.getText().toString().split(":");
		TimePickerDialog dialog = new TimePickerDialog(mActivity, new OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				button.setText(Integer.toString(hourOfDay) + ":" + Integer.toString(minute));
				if (setTime == 0) {
					setFromDate();
				} else if (setTime == 1) {
					setToDate();
				}
			}
		}, Integer.parseInt(text[0]), Integer.parseInt(text[1]), DateFormat.is24HourFormat(mActivity));
		dialog.show();
	}
	
	private void showDatePickerDialog(final Button button, final int setDate) {
		Calendar calendar = Calendar.getInstance();
		if (setDate == 0) {
			calendar.setTimeInMillis(mEvent.getDtStart());
		} else if (setDate == 1) {
			calendar.setTimeInMillis(mEvent.getDtEnd());
		}
		
		DatePickerDialog dialog = new DatePickerDialog(mActivity, new OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int month, int day) {
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, month);
				cal.set(Calendar.DAY_OF_MONTH, day);
				
				button.setText(MQCalendarEvent.DATE_FORMAT_SHORT.format(cal.getTimeInMillis()));
				if (setDate == 0) {
					setFromDate();
				} else if (setDate == 1) {
					setToDate();
				}
			}
		}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		dialog.show();
	}
	
	private void setFromDate() {
		String date = mButtonFromDate.getText().toString();
		String[] time = mButtonFromTime.getText().toString().split(":");
		
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(MQCalendarEvent.DATE_FORMAT_SHORT.parse(date));
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		mEvent.setDtStart(calendar.getTimeInMillis());
	}
	
	private void setToDate() {
		String date = mButtonToDate.getText().toString();
		String[] time = mButtonToTime.getText().toString().split(":");
		
		Calendar calendar = Calendar.getInstance();
		try {
			calendar.setTime(MQCalendarEvent.DATE_FORMAT_SHORT.parse(date));
			calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
			calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		mEvent.setDtEnd(calendar.getTimeInMillis());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.actionbar_cancel:
			mActivity.finish();
			break;
		case R.id.actionbar_save:
			if (save()) {
				mActivity.finish();
			}
			break;
		}
	}

	
	/**
	 * Save changes
	 * @return
	 */
	public boolean save() {
		mEvent.setTitle(mEditTextTitle.getText().toString());
		mEvent.setDescription(mEditTextNotes.getText().toString());
		
		if (mEditMode) {
			ProviderWrapper.updateEvent(mActivity, mEvent);
		} else {
			ProviderWrapper.insertEvent(mActivity, mEvent);
		}
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id == 1) {
			return CalendarProviderWrapper.getAllCalendars(mActivity);
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader != null) {
			int pos = 0;
			
			mCalendars = new ArrayList<MQCalendar>();
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					MQCalendar calendar = CursorTransformer.cursorToCalendar(cursor);
					if (calendar != null) {
						mCalendars.add(calendar);
						if (mEvent != null) {
							if (mEvent.getCalendarID() == calendar.getCalendarID()) {
								pos = cursor.getPosition();
							}
						}
					}
				}
			}
			
			CalendarAdapter adapter = new CalendarAdapter(mActivity, 0, mCalendars);
			mSpinnerCalendar.setAdapter(adapter);
			mSpinnerCalendar.setSelection(pos);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// nothing to do here
	}


}