package com.amlan.hack;

import android.app.Activity;
import android.os.Bundle;

public class ThermometerActivity extends Activity {
	private Thermometer thermometer;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        thermometer = (Thermometer)findViewById(R.id.thermometer);
//        thermometer.
    }
}