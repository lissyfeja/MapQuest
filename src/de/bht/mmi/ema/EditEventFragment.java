package de.bht.mmi.ema;


import de.bht.mmi.ema.data.CalendarEvent;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class EditEventFragment extends Fragment implements OnClickListener {
	private EditEventActivity mActivity;
	private ActionBar mActionBar;
	public CalendarEvent mEvent;
	private boolean mEditMode;
	
	
	
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
		// TODO: initialize views
		
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
			// TODO: get objects, variables and states from savedInstanceState
			
		} else {
			mEvent = mActivity.mEvent;
		}
		
		mEditMode = (mEvent != null);
		
	}
	
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        // TODO: save state on orientation change
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
		if (mEditMode) {
			// TODO: update edited event
		} else {
			// TODO: insert new event
		}
		return true;
	}


}