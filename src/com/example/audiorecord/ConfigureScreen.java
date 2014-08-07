package com.example.audiorecord;


import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigureScreen extends PreferenceActivity {
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.configure);
	}
}
