package com.github.matt.williams.mighty.mitt;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class FingerMap {
    private static final String PREFERENCE_PREFIX = "FingerAddress.";
    public static final String PREFERENCES_NAME = "FingerMap";
    private final Map<Finger, String> mFingerAddressMap = new HashMap<Finger, String>();
    private final Map<String, Finger> mAddressFingerMap = new HashMap<String, Finger>();

    synchronized void setFingerAddress(Finger finger, String address) {
        Finger replacedFinger = mAddressFingerMap.get(address);
        String replacedAddress = mFingerAddressMap.get(finger);
        if ((replacedFinger != null) &&
            (mFingerAddressMap.get(replacedFinger).equals(address))) {
            mFingerAddressMap.remove(replacedFinger);
        }
        if ((replacedAddress != null) &&
            (mAddressFingerMap.get(replacedAddress).equals(finger))) {
            mAddressFingerMap.remove(replacedAddress);
        }
        mFingerAddressMap.put(finger, address);
        mAddressFingerMap.put(address, finger);
    }

    synchronized void clearFingerAddress(Finger finger)
    {
        String replacedAddress = mFingerAddressMap.get(finger);
        if ((replacedAddress != null) &&
            (mAddressFingerMap.get(replacedAddress).equals(finger))) {
            mAddressFingerMap.remove(replacedAddress);
        }
        mFingerAddressMap.remove(finger);
    }

    synchronized Finger getFinger(String address) {
        return mAddressFingerMap.get(address);
    }

    synchronized String getAddress(Finger finger) {
        return mFingerAddressMap.get(finger);
    }

    synchronized void load(SharedPreferences preferences) {
        clear();
        for (Finger finger : Finger.values()) {
            String address = preferences.getString(PREFERENCE_PREFIX + finger.name(), null);
            if (address != null) {
                setFingerAddress(finger, address);
            }
        }
    }

    synchronized void save(SharedPreferences preferences) {
        Editor editor = preferences.edit();
        for (Finger finger : Finger.values()) {
            String address = getAddress(finger);
            if (address != null) {
                editor.putString(PREFERENCE_PREFIX + finger.name(), address);
            } else {
                editor.remove(PREFERENCE_PREFIX + finger.name());
            }
        }
        editor.apply();
    }

    synchronized public void clear() {
        mFingerAddressMap.clear();
        mAddressFingerMap.clear();
    }
}
