package com.github.matt.williams.mighty.mitt;

import android.content.Context;

public class TagTrackerService implements com.github.matt.williams.mighty.mitt.EstimoteService.Listener, com.github.matt.williams.mighty.mitt.AccelerometerService.Listener {
    private static final String TAG = "FingerTrackerService";
    private final EstimoteService mEstimoteService;
    private final AccelerometerService mAccelerometerService;
    private float[] mAcceleration;
    private Listener mListener;

    public static interface Listener {
        public void onTagUpdate(String address, float angle);
    }

    public TagTrackerService() {
        mEstimoteService = new EstimoteService();
        mAccelerometerService = new AccelerometerService();
        mAcceleration = new float[3];
    }

    public void start(Context context, Listener listener) {
        mListener = listener;
        mEstimoteService.start(context, this);
        mAccelerometerService.start(context, this);
    }

    public void stop() {
        mAccelerometerService.stop();
        mEstimoteService.stop();
    }

    @Override
    public void onEstimoteUpdate(String address, int rssi, float[] acceleration) {
        //android.util.Log.e(TAG, address + " - " + orientationToString(acceleration));
        //android.util.Log.e(TAG, "Phone - " + orientationToString(mAcceleration));

        float[] normTagAccelerationYZ = MathUtils.norm2(new float[] {acceleration[1], acceleration[2]});
        float[] normLocalAccelerationYZ = MathUtils.norm2(new float[] {mAcceleration[1], mAcceleration[2]});
        float angle = (float)Math.acos(MathUtils.dot2(normTagAccelerationYZ, normLocalAccelerationYZ));
        //android.util.Log.e(TAG, address + " - " + angle * 180 / Math.PI);

        mListener.onTagUpdate(address, angle);
    }

    private String orientationToString(float[] acceleration) {
        float ax = acceleration[0];
        float ay = acceleration[1];
        float az = acceleration[2];
        String orientation = "(" + ax + ", " + ay + ", " + az + ") - ";
        float max = Math.max(Math.max(Math.abs(ax), Math.abs(ay)), Math.abs(az));
        if (Math.abs(ax) == max) {
            if (ax == max) {
                orientation += "balanced on left side";
            } else {
                orientation += "balanced on right side";
            }
        } else if (Math.abs(ay) == max) {
            if (ay == max) {
                orientation += "balanced on bottom" ;
            } else {
                orientation += "balanced on top" ;
            }
        } else {
            if (az == max) {
                orientation += "flat - face up" ;
            } else {
                orientation += "flat - face down" ;
            }
        }
        return orientation;
    }

    @Override
    public void onAccelerationUpdate(float[] acceleration) {
        mAcceleration = acceleration;
    }
}
