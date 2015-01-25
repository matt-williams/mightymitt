package com.github.matt.williams.mighty.mitt;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FingerSelectionActivity extends ListActivity {
    private static final String TAG = "FingerSelectionActivity";
    public static final String EXTRA_FINGER = "Finger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FingerMap fingerMap = new FingerMap();
        SharedPreferences preferences = getSharedPreferences(FingerMap.PREFERENCES_NAME, 0);
        fingerMap.load(preferences);
        Finger[] fingers = Finger.values();
        List<String> strings = new ArrayList<String>();
        for (Finger finger : fingers) {
            String address = fingerMap.getAddress(finger);
            strings.add(finger.toString() + ((address != null) ? " - " + address : ""));
        }

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strings));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = this.getIntent();
        intent.putExtra(EXTRA_FINGER, Finger.values()[position].toString());
        this.setResult(RESULT_OK, intent);
        finish();
    }
}