package com.github.matt.williams.mighty.mitt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CALIBRATION = 1;
    private HandTrackerService mHandTrackerService;
    private FingerMap mFingerMap;
    private GLView mGlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandTrackerService = new HandTrackerService();

        setContentView(R.layout.activity_glview);
        mGlView = (GLView)findViewById(R.id.glview);

        mFingerMap = new FingerMap();
        SharedPreferences preferences = getSharedPreferences(FingerMap.PREFERENCES_NAME, 0);
        mFingerMap.load(preferences);
        mHandTrackerService.setFingerMap(mFingerMap);
        mGlView.setFingerMap(mFingerMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandTrackerService.start(this, mGlView);
        mGlView.onResume();
    }

    @Override
    protected void onPause() {
        mGlView.onPause();
        mHandTrackerService.stop();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CALIBRATION) {
            if (resultCode == RESULT_OK) {
                SharedPreferences preferences = getSharedPreferences(FingerMap.PREFERENCES_NAME, 0);
                mFingerMap.load(preferences);
                mHandTrackerService.setFingerMap(mFingerMap);
                mGlView.setFingerMap(mFingerMap);
            }
        }
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
            return true;
        } else if (id == R.id.action_debug) {
            Intent intent = new Intent(this, DebugActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
