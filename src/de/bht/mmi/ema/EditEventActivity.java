package de.bht.mmi.ema;

import com.google.gson.Gson;

import de.bht.mmi.ema.data.CalendarEvent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class EditEventActivity extends FragmentActivity {
	public static final String INTENT_EDITEVENT = "intent.editevent.event";
	public CalendarEvent mEvent;
	
	private EditEventFragment mFragment;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editevent);
		
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.hasExtra(INTENT_EDITEVENT)) {
				String json = getIntent().getExtras().getString(INTENT_EDITEVENT);
		        mEvent = new Gson().fromJson(json, CalendarEvent.class);
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