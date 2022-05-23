package com.moong.pluginapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class PluginActivity extends AppCompatActivity {
    private static final String TAG = "[MoongPluginTest]";
    private static final String FILE = "[PluginManager]";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_plugin);
        Log.d(TAG, FILE + "onCreate: ");
    }

    public static void doSomething() {
        Log.d(TAG, FILE + "PluginActivity doSomething: ");
    }
}
