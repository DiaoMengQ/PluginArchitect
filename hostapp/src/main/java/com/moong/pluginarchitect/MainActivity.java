package com.moong.pluginarchitect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "[MoongPlugin]";
    private static final String FILE = "[MainActivity]";

    private Button btnStartActivity;
    private Button btnStartActivityOwn;

    private HostService.TestBinder mBinder;
    private boolean connected = false;
    private Context mContext;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, FILE + "onServiceConnected: ");
            mBinder = (HostService.TestBinder) service;
            mBinder.showTip();
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            connected = false;
        }
    };
    private Button btnTest;
    private EditText etTip;
    private Button btnStartService;

    private void initView() {
        btnStartActivity = findViewById(R.id.btn_startActivity);
        btnStartActivity.setOnClickListener(new mOnClickListener());
        btnStartActivityOwn = findViewById(R.id.btn_startActivity_own);
        btnStartActivityOwn.setOnClickListener(new mOnClickListener());
        btnTest = findViewById(R.id.btn_test);
        btnTest.setOnClickListener(new mOnClickListener());
        btnStartService = findViewById(R.id.btn_startService);
        btnStartService.setOnClickListener(new mOnClickListener());

        etTip = findViewById(R.id.et_tip);
        etTip.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = MainActivity.this;

        try {
            Class<?> clazz = Class.forName("com.moong.pluginapp.PluginActivity");
            // 警告: 最后一个参数使用了不准确的变量类型的 varargs 方法的非 varargs 调用;
            //             clazz.getMethod("doSomething").invoke(null, null);
            //   对于 varargs 调用, 应使用 Object
            clazz.getMethod("doSomething").invoke(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();
        // startPluginService();
    }

    private void startPluginService() {
        // Intent mIntent = new Intent(mContext, HostService.class);
        // bindService(mIntent, serviceConnection, BIND_AUTO_CREATE);
        Intent intentPluginService = new Intent();
        // intent.setClass(MainActivity.this, xxx.class);
        intentPluginService.setComponent(new ComponentName(
                "com.moong.pluginapp",
                "com.moong.pluginapp.PluginService"
        ));
        startService(intentPluginService);
    }

    private class mOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_startActivity:
                    Log.d(TAG, FILE + "onClick: btn_startActivity 跳转插件apk");
                    Intent intentPlugin = new Intent();
                    // intent.setClass(MainActivity.this, xxx.class);
                    intentPlugin.setComponent(new ComponentName(
                            "com.moong.pluginapp",
                            "com.moong.pluginapp.PluginActivity"
                    ));
                    startActivity(intentPlugin);
                    break;
                case R.id.btn_startActivity_own:
                    Log.d(TAG, FILE + "onClick: btn_startActivity 跳转自己的apk");
                    Intent intentOwnActivity = new Intent();
                    // intent.setClass(MainActivity.this, xxx.class);
                    intentOwnActivity.setComponent(new ComponentName(
                            "com.moong.pluginarchitect",
                            "com.moong.pluginarchitect.MyActivity"
                    ));
                    startActivity(intentOwnActivity);
                    break;
                case R.id.btn_startService:
                    startPluginService();
                    break;
                case R.id.btn_test:
                    Log.d(TAG, FILE + "onClick: btn_test");

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + v.getId());
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                Log.d(TAG, FILE + "onKeyDown: KEYCODE_1");
                mBinder.showTip();
                break;
            default:
                Log.d(TAG, FILE + "onKeyDown: keyCode: " + keyCode);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connected) {
            unbindService(serviceConnection);
        }
    }
}
