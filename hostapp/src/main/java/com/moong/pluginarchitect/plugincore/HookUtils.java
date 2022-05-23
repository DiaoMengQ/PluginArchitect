package com.moong.pluginarchitect.plugincore;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * @author DEMI dmq1212@qq.com
 * @date created on 2021/12/7
 */
public class HookUtils {
    private static final String TAG = "[MoongPlugin]";
    private static final String FILE = "[HookUtils]";

    /**
     * hook AMS 对象
     * 对AMS对象的startActivity方法进行拦截
     * 根据 activity启动流程，ActivityTaskManager 会获取一个IActivityTaskManager单例，这个单例是远程服务 AMS的代理，
     * startActivity最后走进的方法是远程服务 AMS中的startActivity；
     * <p>
     * IActivityManager调用的时候会把要启动的Activity的Intent给ActivityManagerServer,
     * 假设我们此时用动态代理在IActivityManager调用远程服务之前，把在一个在清单文件注册过的Activity的Intent替换，就能通过校验
     *
     * @param context
     */
    public static void hookAMS(Context context) throws Exception {
        Log.d(TAG, FILE + "hookAMS: ");

        // 1. 获取AMS对象
        // 1.1 获取静态属性IActivityManager.IActivityManagerSingleton 的值(singleton类型)
        Field iActivityManagerSingletonField = ActivityManager.class.getDeclaredField("IActivityManagerSingleton");
        iActivityManagerSingletonField.setAccessible(true);
        // 静态属性，获取值不需要传入对象
        Object iActivityManagerSingletonObject = iActivityManagerSingletonField.get(null);

        // 1.2 获取Singleton的 mInstance 属性值
        Class<?> singleTonClazz = Class.forName("android.util.Singleton");
        Field mInstanceField = singleTonClazz.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);
        Object AMSSubject = mInstanceField.get(iActivityManagerSingletonObject);


        // 2. 对AMS对象进行代理
        // 代理和被代理对象需要继承同一个接口或父类
        // 代理和被代理可同时实现多个接口
        Class<?> IActivityManagerInterface = Class.forName("android.app.IActivityManager");
        AMSInvocationHandler amsInvocationHandler = new AMSInvocationHandler(context, AMSSubject);

        Object AMSProxy = Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{IActivityManagerInterface},
                amsInvocationHandler
        );
        // 将 iActivityManagerSingletonObject 上的 mInstance 变量替换成 AMSProxy
        mInstanceField.set(iActivityManagerSingletonObject, AMSProxy);

        // 3. InvocationHandler对AMS对象的方法进行拦截
        Log.d(TAG, FILE + "hookAMS: execute into AMSInvocationHandler.java");
    }

    /**
     * 获取启动activity过程中 Handler 的消息
     * 将 Intent 对象里的HostActivity 替换成 PluginActivity
     */
    public static void hookHandler() throws Exception {
        // 1. 获取到 handler对象（mH属性值）
        // 1.1 获取 ActivityThread 对象
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Field sCurrentActivityThreadField = activityThreadClazz.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThreadField.setAccessible(true);
        // 获取静态对象，直接传入null
        Object activityThreadObject = sCurrentActivityThreadField.get(null);

        // 1.2 获取activityThread 对象的mH属性值
        Field mHField = activityThreadClazz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Object handler = mHField.get(activityThreadObject);

        // 2. 给handler的 mCallback 属性进行赋值
        Field mCallbackField = Handler.class.getDeclaredField("mCallback");
        mCallbackField.setAccessible(true);
        mCallbackField.set(handler, new MyCallback());

        // 3. 在自定义callback中将Intent 中的 HostActivity 替换成 PluginActivity
        Log.d(TAG, FILE + "hookHandler: execute into MyCallback.java");
    }

}
