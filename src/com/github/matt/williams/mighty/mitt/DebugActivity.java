package com.github.matt.williams.mighty.mitt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.github.matt.williams.mighty.mitt.HandTrackerService.Listener;

public class DebugActivity extends ListActivity implements Listener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CALIBRATION = 1;
    private HandTrackerService mHandTrackerService;
    private FingerMap mFingerMap;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandTrackerService = new HandTrackerService();

        mFingerMap = new FingerMap();
        SharedPreferences preferences = getSharedPreferences(FingerMap.PREFERENCES_NAME, 0);
        mFingerMap.load(preferences);
        mHandTrackerService.setFingerMap(mFingerMap);

        Finger[] fingers = Finger.values();
        List<String> strings = new ArrayList<String>();
        for (Finger finger : fingers) {
            String address = mFingerMap.getAddress(finger);
            strings.add(finger.toString() + ((address == null) ? " (unconfigured)" : ""));
        }

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                ((ArrayAdapter<String>)getListAdapter()).notifyDataSetChanged();
            };
        };

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings));
        android.util.Log.e(TAG, "onCreate done!");
    }

    @Override
    protected void onResume() {
        android.util.Log.e(TAG, "In onResume...");
        super.onResume();
        mHandTrackerService.start(this, this);
        android.util.Log.e(TAG, "onResume done!");
    }

    @Override
    protected void onPause() {
        android.util.Log.e(TAG, "In onPause...");
        mHandTrackerService.stop();
        super.onPause();
        android.util.Log.e(TAG, "onPause done!");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        android.util.Log.e(TAG, "In onActivityResult...");
        if (requestCode == REQUEST_CALIBRATION) {
            if (resultCode == RESULT_OK) {
                SharedPreferences preferences = getSharedPreferences(FingerMap.PREFERENCES_NAME, 0);
                mFingerMap.load(preferences);
                updateFingers();
            }
        }
        android.util.Log.e(TAG, "onActivityResult done!");
    }

    private void updateFingers() {
        mHandTrackerService.setFingerMap(mFingerMap);
        Finger[] fingers = Finger.values();
        List<String> strings = new ArrayList<String>();
        for (Finger finger : fingers) {
            String address = mFingerMap.getAddress(finger);
            strings.add(finger.toString() + ((address == null) ? " (unconfigured)" : ""));
        }
        ArrayAdapter<String> adapter = (ArrayAdapter<String>)getListAdapter();
        adapter.setNotifyOnChange(false);
        adapter.clear();
        adapter.addAll(strings);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_calibrate) {
            Intent intent = new Intent(this, CalibrationActivity.class);
            startActivityForResult(intent, REQUEST_CALIBRATION);
            return true;
        } else if (id == R.id.action_clear_settings) {
            mFingerMap.clear();
            SharedPreferences preferences = getSharedPreferences(FingerMap.PREFERENCES_NAME, 0);
            mFingerMap.save(preferences);
            updateFingers();
            return true;
        } else if (id == R.id.action_debug) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFingerUpdate(Finger finger, float[] angles) {
        android.util.Log.e(TAG, "In onFingerUpdate...");
        ArrayAdapter<String> adapter = (ArrayAdapter<String>)getListAdapter();
        adapter.setNotifyOnChange(false);
        int fingerIndex = Arrays.asList(Finger.values()).indexOf(finger);
        adapter.remove(adapter.getItem(fingerIndex));
        adapter.insert(String.format("%1$s - (%2$1.1f, %3$1.1f, %4$1.1f)", finger, angles[0] * 180 / Math.PI, angles[1] * 180 / Math.PI, angles[2] * 180 / Math.PI), fingerIndex);
        Message message = mHandler.obtainMessage();
        message.sendToTarget();
        android.util.Log.e(TAG, "onFingerUpdate done!");
    }
}