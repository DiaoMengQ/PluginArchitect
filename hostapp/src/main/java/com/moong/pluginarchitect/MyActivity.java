package com.moong.pluginarchitect;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * @author Demi dmq1212@qq.com
 * @date created on 2021/12/15
 */
public class MyActivity extends Activity {
    private static final String TAG = "[MoongPlugin]";
    private static final String FILE = "[MyActivity]";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, FILE + "onCreate: ");
    }
}
