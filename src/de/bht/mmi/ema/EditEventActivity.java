package de.bht.mmi.ema;

import java.util.Calendar;

import com.google.gson.Gson;

import de.bht.mmi.ema.data.MQCalendarEvent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class EditEventActivity extends FragmentActivity {
	public static final String INTENT_EDITEVENT = "intent.editevent.event";
	public static final String INTENT_ADDEVENT = "intent.addevent.event";
	public MQCalendarEvent mEvent;
	public boolean mEditMode;
	
	private EditEventFragment mFragment;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editevent);
		
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.hasExtra(INTENT_EDITEVENT)) {
				String json = getIntent().getExtras().getString(INTENT_EDITEVENT);
		        mEvent = new Gson().fromJson(json, MQCalendarEvent.class);
		        mEditMode = true;
			} else if (intent.hasExtra(INTENT_ADDEVENT)) {
				String json = getIntent().getExtras().getString(INTENT_ADDEVENT);
				mEvent = new Gson().fromJson(json, MQCalendarEvent.class);
				mEditMode = false;
				
				Calendar cal = Calendar.getInstance();
				mEvent.setDtStart(cal.getTimeInMillis());
				cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
				mEvent.setDtEnd(cal.getTimeInMillis());
			}
		}
		
		FragmentManager fm = getSupportFragmentManager();
		mFragment = (EditEventFragment) fm.findFragmentByTag("editevent");
	}

	@Override
	public void onBackPressed() {
		mFragment.save();
		super.onBackPressed();
	}

}