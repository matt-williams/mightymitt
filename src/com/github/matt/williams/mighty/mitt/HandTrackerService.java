package com.github.matt.williams.mighty.mitt;

import android.content.Context;

public class HandTrackerService extends TagTrackerService implements com.github.matt.williams.mighty.mitt.TagTrackerService.Listener {
    private static final String TAG = "HandTrackerService";
    private final TagTrackerService mTagTrackerService;
    private FingerMap mFingerMap;
    private Listener mListener;

    public static interface Listener {
        public void onFingerUpdate(Finger finger, float[]	angles);
    }

    public HandTrackerService() {
        mTagTrackerService = new TagTrackerService();
        mFingerMap = new FingerMap();
    }

    public void setFingerMap(FingerMap fingerMap) {
        mFingerMap = fingerMap;
    }

    public void start(Context context, Listener listener) {
        mListener = listener;
        mTagTrackerService.start(context, this);
    }

    @Override
    public void stop() {
        mTagTrackerService.stop();
    }

    @Override
    public void onTagUpdate(String address, float[] angles) {
        Finger finger = mFingerMap.getFinger(address);
        if (finger != null) {
            mListener.onFingerUpdate(finger, angles);
        }
    }
}
