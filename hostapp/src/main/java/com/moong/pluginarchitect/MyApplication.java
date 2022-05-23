package com.moong.pluginarchitect;

import android.app.Application;

import com.moong.pluginarchitect.plugincore.PluginManager;

/**
 * @author DEMI dmq1212@qq.com
 * @date created on 2021/12/4
 * @desc
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PluginManager.getInstance(this).init();
    }
}
