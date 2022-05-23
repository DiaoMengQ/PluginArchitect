package com.moong.pluginarchitect.plugincore;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;

/**
 * @author DEMI dmq1212@qq.com
 * @date created on 2021/12/9
 * @desc
 */
public class MyCallback implements Handler.Callback {
    private static final String TAG = "[MoongPlugin]";
    private static final String FILE = "[MyCallback]";

    private void writeDebug(String info) {
        Log.d(TAG, FILE + " " + info);
    }

    MyCallback() {
        getAccessHiddenMethod();
    }

    /**
     * 在 ActivityThread 类中，handler处理信息时的launch activity 代号
     */
    private static final int LAUNCH_ACTIVITY = 100; // API <= 26
    private static final int ENTER_ANIMATION_COMPLETE = 149;
    private static final int EXECUTE_TRANSACTION = 159;

    /**
     * 创建服务时，Handler接收的消息
     */
    private static final int CREATE_SERVICE = 114;

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case LAUNCH_ACTIVITY:
                writeDebug("handleMessage: LAUNCH_ACTIVITY");
                try {
                    // msg.obj 是 ActivityClientRecord 类 （API <= 26）
                    /*
                    *     public static final class ActivityClientRecord {
                            Intent intent;
                    * */
                    Field intentField = msg.obj.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);
                    Intent intent = (Intent) intentField.get(msg.obj);
                    Parcelable actionIntent = null;
                    if (intent != null) {
                        // 获取AMS代理中传出来的，原Intent
                        actionIntent = intent.getParcelableExtra("actionIntent");
                    }
                    if (actionIntent != null) {
                        writeDebug("handleMessage: intent replaced");
                        intentField.set(msg.obj, actionIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case -149:
                // case ENTER_ANIMATION_COMPLETE:
                writeDebug("handleMessage: 149 ENTER_ANIMATION_COMPLETE");
                try {
                    // ---------------- shiquan
                    // ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    // Class activityThreadClass = Class.forName("android.app.ActivityThread");
                    // Object activityThread = systemField.get(am).getClass().getMethod("currentActivityThread").invoke(null);
                    // Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
                    // activitiesField.setAccessible(true);
                    // ArrayMap activities = (ArrayMap) activitiesField.get(activityThread);
                    // Log.d(TAG, "the activities size: " + activities.size());


                    // msg.obj 是获取 ActivityClientRecord 类的 key（BinderProxy）
                    // ActivityThread.java :         final ActivityClientRecord r = mActivities.get(token);
                    IBinder iBinder = (IBinder) msg.obj;
                    Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
                    // 获取 activityThread
                    Field sCurrentActivityThreadField = activityThreadClazz.getDeclaredField("sCurrentActivityThread");
                    sCurrentActivityThreadField.setAccessible(true);
                    Object activityThreadObject = sCurrentActivityThreadField.get(null);

                    // 获取activities 属性
                    Field mActivitiesField = activityThreadClazz.getDeclaredField("mActivities");
                    mActivitiesField.setAccessible(true);
                    ArrayMap activitiesArrayMap = (ArrayMap) mActivitiesField.get(activityThreadObject);
                    writeDebug("handleMessage: activitiesArrayMap size: " + activitiesArrayMap.size());

                    // 获取 activityClientRecord 对象
                    Object activityClientRecordObject = activitiesArrayMap.get(iBinder);

                    // 获取内部类要用 $ 符号
                    Class<?> activityClientRecordClazz = Class.forName("android.app.ActivityThread$ActivityClientRecord");
                    // 获取 activityClientRecord 对象里的 intent 属性
                    Field intentACRField = activityClientRecordClazz.getDeclaredField("intent");
                    intentACRField.setAccessible(true);
                    Intent intent = (Intent) intentACRField.get(activityClientRecordObject);

                    // Field intentField = msg.obj.getClass().getDeclaredField("intent");
                    // intentField.setAccessible(true);
                    // Intent intent = (Intent) intentField.get(msg.obj);
                    Parcelable actionIntent = null;
                    if (intent != null) {
                        // 获取AMS代理中传出来的，原Intent
                        actionIntent = intent.getParcelableExtra("actionIntent");
                    } else {
                        writeDebug("handleMessage: intent == null");
                    }
                    if (actionIntent != null) {
                        writeDebug("handleMessage: intent replaced");
                        writeDebug("handleMessage: get obj class : " + msg.obj.getClass()); // IBinder

                        // 将原 intent 放进 activityClientRecord
                        intentACRField.set(activityClientRecordObject, actionIntent);
                        // 将修改后的 ActivityClientRecord 放入 activities
                        activitiesArrayMap.put(iBinder, activityClientRecordObject);
                        // 将替换后的 activities 放回 activityThread
                        mActivitiesField.set(activityThreadObject, activitiesArrayMap);

                        /* END: 至此 替换不生效*/
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case EXECUTE_TRANSACTION:
                writeDebug("handleMessage: 159 EXECUTE_TRANSACTION");
                // transaction 中获取 callbacks 集合，遍历集合拿到 launchActivityItem，再拿到 intent

                try {
                    // 获取 ClientTransaction 中的 mActivityCallbacks
                    // ActivityThread : final ClientTransaction transaction = (ClientTransaction) msg.obj;
                    Object clientTransactionObject = msg.obj;
                    Class<?> clientTransactionObjectClazz = clientTransactionObject.getClass();
                    Field mActivityCallbacksField = clientTransactionObjectClazz.getDeclaredField("mActivityCallbacks");
                    mActivityCallbacksField.setAccessible(true);
                    List activityCallbacks = (List) mActivityCallbacksField.get(clientTransactionObject);
                    if (activityCallbacks == null) {
                        writeDebug("activityCallbacks == null");
                    } else {
                        // 遍历 mActivityCallbacks，得到 LaunchActivityItem
                        for (Object callback : activityCallbacks) {
                            writeDebug("callback: " + callback.getClass().getName());

                            if ("android.app.servertransaction.LaunchActivityItem".equals(
                                    callback.getClass().getName()
                            )) {
                                Field mIntentFile = callback.getClass().getDeclaredField("mIntent");
                                mIntentFile.setAccessible(true);
                                Intent intent = (Intent) mIntentFile.get(callback);
                                // 替换 LaunchActivityItem 中的 intent
                                Parcelable actionIntent = null;
                                if (intent != null) {
                                    // 获取AMS代理中传出来的，原Intent
                                    actionIntent = intent.getParcelableExtra("actionIntent");
                                } else {
                                    writeDebug("handleMessage: intent == null");
                                }
                                if (actionIntent != null) {
                                    writeDebug("handleMessage: intent replaced");
                                    mIntentFile.set(callback, actionIntent);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case CREATE_SERVICE:
                writeDebug("handleMessage: 114 CREATE_SERVICE");
                try {
                    // msg.obj 是 CreateServiceData 类  android.app.ActivityThread$CreateServiceData
                    writeDebug("msg.obj: " + msg.obj.getClass().getName());
                    //从 obj 中获取到 CreateServiceData对象
                    Object createServiceDataObject = msg.obj;
                    //获取 CreateServiceData对象的类
                    Class<?> createServiceDataClazz = createServiceDataObject.getClass();
                    //获取 CreateServiceData对象的类的方法
                    Field[] declaredFields = createServiceDataClazz.getDeclaredFields();
                    for (Field f : declaredFields) {
                        writeDebug(f.getName());
                    }
                    Method toStringMethod = createServiceDataClazz.getMethod("toString");
                    toStringMethod.setAccessible(true);
                    String str = toStringMethod.invoke(createServiceDataObject,null).toString();



                    Field intentField = msg.obj.getClass().getDeclaredField("intent");
                    intentField.setAccessible(true);
                    Intent intent = (Intent) intentField.get(msg.obj);
                    Parcelable actionServiceIntent = null;
                    if (intent != null) {
                        // 获取AMS代理中传出来的，原Intent
                        actionServiceIntent = intent.getParcelableExtra("actionServiceIntent");
                    } else {
                        writeDebug("intent == null");
                    }
                    if (actionServiceIntent != null) {
                        writeDebug("handleMessage: intent service replaced");
                        intentField.set(msg.obj, actionServiceIntent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                writeDebug("handleMessage: msg.what： " + msg.what);
        }
        /* 注意：在dispatchMessage 中，如果callback的handleMessage return true，那dispatchMessage直接就return了
         * 此处需要return false，dispatchMessage 才能按原本接收消息的顺序往下执行 */
        return false;
    }


    /**
     * 解决限制访问方法
     * 原理：通过反射 API 拿到 getDeclaredMethod 方法。getDeclaredMethod 是 public 的，不存在问题；这个通过反射拿到的方法我们称之为元反射方法。
     * 反射拿到元反射方法去反射调用 getDeclardMethod。
     * 这里我们就实现了以系统身份去反射的目的——反射相关的 API 都是系统类，因此我们的元反射方法也是被系统类加载的方法；
     * 所以我们的元反射方法调用的 getDeclardMethod 会被认为是系统调用的，可以反射任意的方法。
     * <p>
     * 20211213 WARNING: 11修复了这个元反射的破解方法
     */
    private void getAccessHiddenMethod() {
        writeDebug("getAccessHiddenMethod: ");
        // version <= 8.1 || >= 11
        if (SDK_INT <= Build.VERSION_CODES.O_MR1 || SDK_INT >= Build.VERSION_CODES.R) {
            return;
        }

        // version == 9|10;
        try {
            Method forName = Class.class.getDeclaredMethod("forName", String.class);
            Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            getDeclaredMethod.setAccessible(true);
            Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
            Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
            Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
            Object sVmRuntime = getRuntime.invoke(null);
            setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
        } catch (Throwable e) {
            Log.e(TAG, FILE + "[error] reflect bootstrap failed:", e);
        }
    }
}
