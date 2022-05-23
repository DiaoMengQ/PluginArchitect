package com.moong.pluginapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * @author Demi dmq1212@qq.com
 * @date created on 2021/12/22
 */
public class PluginService extends Service {
    private static final String TAG = "[MoongPluginTest]";
    private static final String FILE = "[PluginService]";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, FILE + "plugin service onCreate: ");
    }
}
