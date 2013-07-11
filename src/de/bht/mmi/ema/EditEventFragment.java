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
import de.bht.mmi.ema.data.MQReminder;
import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Reminders;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
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
	
	private ImageButton mImageButtonReminderTime;
	private ImageButton mImageButtonReminderLocation;
	private ImageButton mImageButtonReminderDelete;
	private LinearLayout mReminderTimeLayout;
	private LinearLayout mReminderLocationLayout;
	private Spinner mSpinnerReminderTimeTime;
	private Spinner mSpinnerReminderTimeMethod;
	private SeekBar mSeekbarReminderLocationRadius;
	private TextView mSeekbarReminderLocationRadiusValue;
	private ImageButton mImageButtonReminderLocationEnter;
	private ImageButton mImageButtonReminderLocationExit;
	
	private int[] mReminderMinutes;
	
	private List<MQCalendar> mCalendars;
	private Marker mNewEventMarker;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.mActivity = (EditEventActivity) getActivity();
		this.mActionBar = mActivity.getActionBar();
		this.mReminderMinutes = mActivity.getResources().getIntArray(R.array.reminder_times);
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
		
		this.mImageButtonReminderTime = (ImageButton) rootView.findViewById(R.id.edit_reminder_time);
		this.mImageButtonReminderLocation = (ImageButton) rootView.findViewById(R.id.edit_reminder_location);
		this.mImageButtonReminderDelete = (ImageButton) rootView.findViewById(R.id.edit_reminder_delete);
		this.mReminderTimeLayout = (LinearLayout) rootView.findViewById(R.id.edit_reminder_time_layout);
		this.mReminderLocationLayout = (LinearLayout) rootView.findViewById(R.id.edit_reminder_location_layout);
		this.mSpinnerReminderTimeTime = (Spinner) rootView.findViewById(R.id.edit_reminder_time_time);
		this.mSpinnerReminderTimeMethod = (Spinner) rootView.findViewById(R.id.edit_reminder_time_method);
		this.mSeekbarReminderLocationRadius = (SeekBar) rootView.findViewById(R.id.edit_reminder_location_radius);
		this.mSeekbarReminderLocationRadiusValue = (TextView) rootView.findViewById(R.id.edit_reminder_location_radius_value);
		this.mImageButtonReminderLocationEnter = (ImageButton) rootView.findViewById(R.id.edit_reminder_location_enter);
		this.mImageButtonReminderLocationExit = (ImageButton) rootView.findViewById(R.id.edit_reminder_location_exit);
		
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
			setReminderToUI();
		} else {
			mEvent = mActivity.mEvent;
			mEditMode = mActivity.mEditMode;
			getLoaderManager().initLoader(1, null, EditEventFragment.this);
			if (mEditMode && mEvent.isHasAlarm()) {
				getLoaderManager().initLoader(2, null, EditEventFragment.this);
			}
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
		
		mImageButtonReminderTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				activateReminderTime();
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		
		mImageButtonReminderLocation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				activateReminderLocation();
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		
		mImageButtonReminderLocationEnter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				activateReminderLocationEnter();
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		
		mImageButtonReminderLocationExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				activateReminderLocationExit();
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		
		mImageButtonReminderDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mReminderTimeLayout.setVisibility(View.GONE);
				mReminderLocationLayout.setVisibility(View.GONE);
				mImageButtonReminderTime.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_time));
				mImageButtonReminderLocation.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_place));
				mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
		
		mSeekbarReminderLocationRadius.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mSeekbarReminderLocationRadiusValue.setText(Integer.toString(progress) + " m");
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
		});
		
		ArrayAdapter<String> adapterTime = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item);
		adapterTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapterTime.addAll(mActivity.getResources().getStringArray(R.array.reminder_times_names));
		mSpinnerReminderTimeTime.setAdapter(adapterTime);
		
		ArrayAdapter<String> adapterMethod = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item);
		adapterMethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapterMethod.addAll(mActivity.getResources().getStringArray(R.array.reminder_method_names));
		mSpinnerReminderTimeMethod.setAdapter(adapterMethod);
		
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
        
        getReminderFromUI();
        
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
	
	private void activateReminderTime() {
		mReminderTimeLayout.setVisibility(View.VISIBLE);
		mReminderLocationLayout.setVisibility(View.GONE);
		mImageButtonReminderTime.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_time_activated));
		mImageButtonReminderLocation.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_place));
	}
	
	private void activateReminderLocation() {
		mReminderTimeLayout.setVisibility(View.GONE);
		mReminderLocationLayout.setVisibility(View.VISIBLE);
		mImageButtonReminderTime.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_time));
		mImageButtonReminderLocation.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_place_activated));
	}
	
	private void activateReminderLocationEnter() {
		mImageButtonReminderLocationEnter.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_geofence_enter_activated));
		mImageButtonReminderLocationExit.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_geofence_exit));
	}
	private void activateReminderLocationExit() {
		mImageButtonReminderLocationEnter.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_geofence_enter));
		mImageButtonReminderLocationExit.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_action_geofence_exit_activated));
	}
	
	/**
	 * Writes the reminder data from mEvent to the UI-elements.
	 * Called after orientation change, and after onLoadFinished()
	 */
	private void setReminderToUI() {
		List<MQReminder> reminders = mEvent.getReminders();
		if (reminders != null && reminders.size() > 0) {
			MQReminder reminder = reminders.get(0);
			if (false) {
				activateReminderLocation();
				// TODO: abfragen ob ein local reminder gespeichert wurde
				// wenn ja, dann wird der local reminder gesetzt

			} else {
				activateReminderTime();
				int minute = reminder.getMinutes();
				int bestMinute = 200;
				int bestPos = 0;
				for (int i = 0; i < mReminderMinutes.length; i++) {
					if (Math.abs(minute - mReminderMinutes[i]) < Math.abs(minute - bestMinute)) {
						bestMinute = mReminderMinutes[i];
						bestPos = i;
					}
				}
				mSpinnerReminderTimeTime.setSelection(bestPos);
				
				String method = reminder.getMethod();
				if (method.equals(Integer.toString(Reminders.METHOD_ALERT))) {
					mSpinnerReminderTimeMethod.setSelection(0);
				} else if (method.equals(Integer.toString(Reminders.METHOD_EMAIL))) {
					mSpinnerReminderTimeMethod.setSelection(1);
				}

			}
		}
	}
	
	/**
	 * Writes the reminder data from the UI into mEvent.
	 * Called before orientation change, and before save()
	 */
	private void getReminderFromUI() {
		MQReminder reminder;
		if (mEvent.isHasAlarm()) {
			reminder = mEvent.getReminders().get(0);
		} else {
			reminder = new MQReminder();
		}
		
		if (mReminderTimeLayout.getVisibility() == View.VISIBLE) {
			int time = mSpinnerReminderTimeTime.getSelectedItemPosition();
			reminder.setMinutes(mReminderMinutes[time]);
			
			int method = mSpinnerReminderTimeMethod.getSelectedItemPosition();
			if (method == 0) {
				reminder.setMethod(Integer.toString(Reminders.METHOD_ALERT));
			} else if (method == 1) {
				reminder.setMethod(Integer.toString(Reminders.METHOD_EMAIL));
			}
			List<MQReminder> reminders = new ArrayList<MQReminder>();
			reminders.add(reminder);
			mEvent.setReminders(reminders);
		} else if (mReminderLocationLayout.getVisibility() == View.VISIBLE) {
			
			//TODO: location reminder
			
		} else {
			mEvent.setReminders(null);
		}
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
		
		getReminderFromUI();
		
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
		} else if (id == 2) {
			return CalendarProviderWrapper.getReminder(mActivity, mEvent.getID());
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader != null) {
			if (loader.getId() == 1) {
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
			} else if (loader.getId() == 2) {
				List<MQReminder> reminders = new ArrayList<MQReminder>();
				if (cursor.getCount() > 0) {
					while (cursor.moveToNext()) {
						MQReminder reminder = CursorTransformer.cursorToReminder(cursor);
						reminders.add(reminder);
					}
				}
				mEvent.setReminders(reminders);
				setReminderToUI();
			}
			cursor.close();
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// nothing to do here
	}


}