package com.github.matt.williams.mighty.mitt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CalibrationActivity extends ListActivity implements com.github.matt.williams.mighty.mitt.TagTrackerService.Listener {
    private static final String TAG = "CalibrationActivity";
    private static final int REQUEST_FINGER_SELECTION = 1;
    private static final String EXTRA_FINGER = "Finger";
    private static final String EXTRA_ADDRESS = "Address";
    private TagTrackerService mTagTrackerService;
    private Finger mFinger;
    private List<AddressCandidate> mAddressCandidates;
    private Handler mHandler;

    private class AddressCandidate {
        String address;
        float minAngle;
        float maxAngle;
        float angle;

        @Override
        public boolean equals(Object other) {
            android.util.Log.e(TAG, "Comparing " + this + " with " + other);
            // Nasty hack - should really implement equality correctly and have a separate key field
            if ((other instanceof AddressCandidate) &&
                (this.address.equals(((AddressCandidate)other).address)))
            {
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(getString(R.string.candidate_label), address, (angle * 180 / Math.PI), (minAngle * 180 / Math.PI), (maxAngle * 180 / Math.PI));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTagTrackerService = new TagTrackerService();
        mAddressCandidates = new ArrayList<AddressCandidate>();
        ArrayAdapter<AddressCandidate> adapter = new ArrayAdapter<AddressCandidate>(this, android.R.layout.simple_list_item_1);
        setListAdapter(adapter);
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                ((ArrayAdapter<AddressCandidate>)getListAdapter()).notifyDataSetChanged();
            };
        };

        Intent intent = new Intent(this, FingerSelectionActivity.class);
        startActivityForResult(intent, REQUEST_FINGER_SELECTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTagTrackerService.start(this, this);
    }

    @Override
    protected void onPause() {
        mTagTrackerService.stop();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FINGER_SELECTION) {
            if (resultCode == RESULT_OK) {
                mFinger = Finger.valueOf(data.getStringExtra(FingerSelectionActivity.EXTRA_FINGER));
                this.setTitle(String.format(getString(R.string.calibrate_format), mFinger.name()));
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        FingerMap fingerMap = new FingerMap();
        SharedPreferences preferences = getSharedPreferences(FingerMap.PREFERENCES_NAME, 0);
        fingerMap.load(preferences);
        fingerMap.setFingerAddress(mFinger, mAddressCandidates.get(position).address);
        fingerMap.save(preferences);

        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_FINGER, mFinger.toString());
        intent.putExtra(EXTRA_ADDRESS, mAddressCandidates.get(position).address);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onTagUpdate(String address, float angle) {
        android.util.Log.e(TAG, address + " - " + angle * 180 / Math.PI);
        AddressCandidate newCandidate = new AddressCandidate();
        newCandidate.address = address;
        int index = mAddressCandidates.indexOf(newCandidate);
        if (index != -1) {
            AddressCandidate candidate = mAddressCandidates.get(index);
            candidate.maxAngle = Math.max(candidate.maxAngle, angle);
            candidate.minAngle = Math.min(candidate.minAngle, angle);
            candidate.angle = angle;
        } else {
            newCandidate.minAngle = angle;
            newCandidate.maxAngle = angle;
            newCandidate.angle = angle;
            mAddressCandidates.add(newCandidate);
        }
        Collections.sort(mAddressCandidates, new Comparator<AddressCandidate>() {
            @Override
            public int compare(AddressCandidate lhs, AddressCandidate rhs) {
                if (lhs.maxAngle - lhs.minAngle > rhs.maxAngle - rhs.minAngle) {
                    return -1;
                } else if (lhs.maxAngle - lhs.minAngle < rhs.maxAngle - rhs.minAngle) {
                    return 1;
                } else {
                    return lhs.address.compareTo(rhs.address);
                }
            }
        });

        ArrayAdapter<AddressCandidate> adapter = (ArrayAdapter<AddressCandidate>)getListAdapter();
        adapter.setNotifyOnChange(false);
        adapter.clear();
        adapter.addAll(mAddressCandidates);
        Message message = mHandler.obtainMessage();
        message.sendToTarget();
    }
}