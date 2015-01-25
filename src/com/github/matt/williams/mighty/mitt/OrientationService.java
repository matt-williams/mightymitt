package com.github.matt.williams.mighty.mitt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class OrientationService implements SensorEventListener {
    private Listener mListener;
    private SensorManager mSensorManager;

    public OrientationService() {
    }

    public static interface Listener {
        public void onOrientationUpdate(float[] rotationMatrix);
    }

    public void start(Context context, Listener listener) {
        mListener = listener;
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        mSensorManager = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Only process the event if it is reliable and the right type
        if ((event.accuracy != SensorManager.SENSOR_STATUS_UNRELIABLE) &&
            (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)) {
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            mListener.onOrientationUpdate(rotationMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
