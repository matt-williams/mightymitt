package com.github.matt.williams.mighty.mitt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;

// TODO: Not yet a service - would be good to make it so!
public class EstimoteService implements LeScanCallback {
    private static final String TAG = "BLEService";
    private static final float ACCELERATION_SCALE_FACTOR = 9.8f / 63; // Earth gravity (9.8m/s^2) seems to be about 63 units
    private BluetoothAdapter mBluetoothAdapter;
    private Listener mListener;

    public static interface Listener {
        public void onEstimoteUpdate(String address, int rssi, float[] acceleration);
    }

    public boolean start(Context context, Listener listener) {
        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        mListener = listener;

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if ((bluetoothAdapter != null) && bluetoothAdapter.isEnabled()) {
            mBluetoothAdapter = bluetoothAdapter;
            boolean success = bluetoothAdapter.startLeScan(this);

            android.util.Log.i(TAG, success ? "Started LE scan" : "Failed to start scan");
            return true;
        } else {
            return false;
        }
    }

    public void stop() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.stopLeScan(this);
            mBluetoothAdapter = null;
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        //if (device.getAddress().equals("ED:D1:60:27:27:BB")) {
            // Advertising records are LTVs (TLVs with length and type swapped).
            int pos = 0;
            while ((pos < scanRecord.length) &&
                   (scanRecord[pos] != 0))
            {
              int len = scanRecord[pos] & 0xff; // Converts signed to unsigned - why?
              int type = scanRecord[pos + 1] & 0xff;
              byte[] value = new byte[len - 1];
              System.arraycopy(scanRecord, pos + 2, value, 0, len - 1);
//              android.util.Log.e(TAG, "Type = " + type + ", Value= " +  java.util.Arrays.toString(value));
              if ((type == 255) && (value[0] == 0x5d) && (value[1] == 1)) {
                  // Negate y and z values (but not x) to switch from a left-handed coordinate system to the same right-handed one that the native Android one uses.
                  mListener.onEstimoteUpdate(device.getAddress(), rssi, new float[] {value[16] * ACCELERATION_SCALE_FACTOR, -value[17] * ACCELERATION_SCALE_FACTOR, -value[18] * ACCELERATION_SCALE_FACTOR});
              }
              pos += 1 + len;
            }
            /*
            if (got_it)
            {
                pos = 0;
                while ((pos < scanRecord.length) &&
                        (scanRecord[pos] != 0))
                {
                    int len = scanRecord[pos] & 0xff; // Converts signed to unsigned - why?
                    int type = scanRecord[pos + 1] & 0xff;
                    byte[] value = new byte[len - 1];
                    System.arraycopy(scanRecord, pos + 2, value, 0, len - 1);
                    android.util.Log.e(TAG, "Type = " + type + ", Value= " +  java.util.Arrays.toString(value));
                    if (type == 255) {
                        int ax = value[16];
                        int ay = value[17];
                        int az = value[18];

                        String orientation;
                        int max = Math.max(Math.max(Math.abs(ax), Math.abs(ay)), Math.abs(az));
                        if (Math.abs(ax) == max) {
                            if (ax == -max) {
                                orientation = "on side - point up";
                            } else {
                                orientation = "on side - point down";
                            }
                        } else if (Math.abs(ay) == max) {
                            if (ay == -max) {
                                orientation = "on end - point down" ;
                            } else {
                                orientation = "on end - point up" ;
                            }
                        } else {
                            if (az == -max) {
                                orientation = "flat - face up" ;
                            } else {
                                orientation = "flat - face down" ;
                            }
                        }
                        android.util.Log.e(TAG, "Orientaton: (" + ax + ", " + ay + ", " + az + ") - " + orientation);
                    }
                    pos += 1 + len;
                }
            }
            */
        //}
    }
/*
    /// Returns the distance from here to the source of RSSI1 over the distance from here to the source of RSSI2
    public static double rssiToDistanceRatio(double rssi1, double rssi2) {
        // RSSI is in dB, so is 10*log10(power)
        // Hence, power is 10^(rssi/10)
        // Ratio of powers is 10^((rssiA - rssiB) / 10)
        // Assuming that signal strength degrades as 1/(r^2), i.e. no obstructions
        // Ratio of distances is 1/sqrt(10^((rssiA - rssiB) / 10))
        // This is 10^((rssiB - rssiA) / 20)
        return Math.pow(10, (rssi2 - rssi1) / 20.0f);
    }

    public static double rssiToPower(double rssi) {
        return Math.pow(10, rssi / 10.0f);
    }

    public static double powerToRssi(double power) {
        return 10.0 * Math.log10(power);
    }

    public static double ratioToAlpha(double a_over_b) {
        // We have a / b and we want to calculate a / (a + b).
        // TODO: Check for overlow (shouldn't happen as a_over_b is never zero.
        double alpha = 1 / ((1 / a_over_b) + 1);
        android.util.Log.d(TAG, "Translated a_over_b (" + a_over_b + ") to " + alpha);
        return alpha;
    }
*/
}