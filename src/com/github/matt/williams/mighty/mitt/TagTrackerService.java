package com.github.matt.williams.mighty.mitt;

import java.util.Arrays;

import android.content.Context;

public class TagTrackerService implements com.github.matt.williams.mighty.mitt.EstimoteService.Listener, com.github.matt.williams.mighty.mitt.AccelerometerService.Listener {
    private static final String TAG = "FingerTrackerService";
    private final EstimoteService mEstimoteService;
    private final AccelerometerService mAccelerometerService;
    private float[] mAcceleration;
    private Listener mListener;

    public static interface Listener {
        public void onTagUpdate(String address, float[] angles);
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

        float pitch = calculateAngle(acceleration[1], acceleration[2], mAcceleration[1], mAcceleration[2]);
        float yaw = -calculateAngle(-acceleration[0], -acceleration[1], mAcceleration[0], mAcceleration[1]);
        yaw = (yaw > 90) ? (180 - yaw) : yaw;
        yaw = (yaw < -90) ? (-180 - yaw) : yaw;
        float roll = 0;//calculateAngle(acceleration[0], acceleration[2], mAcceleration[0], mAcceleration[2]);
        
        mListener.onTagUpdate(address, new float[] {roll, pitch, yaw});
    }

	private float calculateAngle(float p0, float p1, float q0, float q1) {
		float[] p = MathUtils.safeNorm2(new float[] {p0, p1});
        float[] q = MathUtils.safeNorm2(new float[] {q0, q1});
        float confidence = MathUtils.mod2(new float[] {p0, p1}) * MathUtils.mod2(new float[] {q0, q1});
        return (float)Math.acos(MathUtils.dot2(p, q)) * -Math.signum(MathUtils.cross2(p, q)) * (float)((confidence < 10) ? Math.exp(confidence) / Math.exp(10) : 1);
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
