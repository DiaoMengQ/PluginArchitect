package com.moong.pluginarchitect;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.moong.pluginarchitect.utils.ToastUtil;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author DEMI dmq1212@qq.com
 * @date created on 2021/12/13
 * @desc
 */
public class HostService extends Service {
    private static final String TAG = "[MoongPlugin]";
    private static final String FILE = "[HostService]";
    private Context mContext;

    ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(2);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, FILE + "onBind: ");
        return new TestBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, FILE + "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, FILE + "onStartCommand: ");
        // scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
        //     @Override
        //     public void run() {
        //         Log.d(TAG, "run: 延时1s后每5s执行一次");
        //     }
        // }, 1, 5, TimeUnit.SECONDS);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scheduledThreadPool.shutdown();

        try {
            while (!scheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                Log.d(TAG, FILE + "onDestroy: 线程池没有关闭");
            }
            Log.d(TAG, FILE + "onDestroy: 线程池已经关闭");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class TestBinder extends Binder {
        void showTip() {
            Log.d(TAG, FILE + "showTip: ");
            ToastUtil.toast(getApplicationContext(), "call method in service showTip()");
        }
    }
}
