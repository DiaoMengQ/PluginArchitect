package com.moong.pluginarchitect.plugincore;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;

/**
 * @author DEMI dmq1212@qq.com
 * @date created on 2021/12/4
 */
public class PluginManager {
    private static final String TAG = "[MoongPlugin]";
    private static final String FILE = "[PluginManager]";

    private static PluginManager instance;
    private Context mContext;

    private PluginManager(Context context) {
        mContext = context;
    }

    public static PluginManager getInstance(Context context) {
        // 懒汉式
        if (instance == null) {
            instance = new PluginManager(context);
        }
        return instance;

        // ps. 饿汉式：无论如何都直接 new一个新对象
    }

    public void init() {
        try {
            loadApk();
            HookUtils.hookAMS(mContext);
            HookUtils.hookHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载插件的apk文件，并合并dexElement
     */
    private void loadApk() throws Exception {
        // 加载插件apk
        String pluginApkPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/pluginapp-debug.apk";
        Log.d(TAG, FILE + "loadApk: pluginApkPath: " + pluginApkPath);
        String cachePath = mContext.getDir("cache_plugin", Context.MODE_PRIVATE).getAbsolutePath();
        DexClassLoader dexClassLoader = new DexClassLoader(pluginApkPath, cachePath, null, mContext.getClassLoader());

        // 使用反射获取 baseDexClassLoader 类
        Class<?> baseDexClassLoader = dexClassLoader.getClass().getSuperclass();
        // 获取 baseDexClassLoader 的成员变量 pathList
        Field pathListField = baseDexClassLoader.getDeclaredField("pathList");
        // 变量 pathList 的可操作权限
        pathListField.setAccessible(true);

        // 1. 获取 plugin 的dexElement
        // 获取 plugin PathList 对象；filed.get(obj)返回指定对象obj上此 Field 表示的字段的值
        // 此处表示：在 dexClassLoader 对象上，成员变量 pathList 的值
        Object pluginPathListObject = pathListField.get(dexClassLoader);
        // 获取 PathList 类
        Class<?> pathListClass = pluginPathListObject.getClass();
        // 获取 PathList 类成员变量
        Field dexElementsField = pathListClass.getDeclaredField("dexElements");
        dexElementsField.setAccessible(true); // 设置可访问
        // 获取 DexElement 对象
        Object pluginDexElements = dexElementsField.get(pluginPathListObject);


        // 2. 获取 host 的 dexElement
        ClassLoader pathClassLoader = mContext.getClassLoader();
        Object hostPathListObject = pathListField.get(pathClassLoader);
        Object hostDexElements = dexElementsField.get(hostPathListObject);


        // 3. 合并
        // 获取宿主和插件DexElements总的数组长度
        int pluginDexElementLength = Array.getLength(pluginDexElements);
        int hostDexElementLength = Array.getLength(hostDexElements);
        int newDexElementsLength = pluginDexElementLength + hostDexElementLength;

        // 创建新数组，getComponentType 获取元素的类型
        Object newDexElements = Array.newInstance(hostDexElements.getClass().getComponentType(), newDexElementsLength);
        for (int i = 0; i < newDexElementsLength; i++) {
            // plugin
            if (i < pluginDexElementLength) {
                Array.set(newDexElements, i, Array.get(pluginDexElements, i));
            } else {
                // host
                Array.set(newDexElements, i, Array.get(hostDexElements, i - pluginDexElementLength));
            }
        }
        // 将合并后的 DexElements 赋值给 host PathList 对象
        dexElementsField.set(hostPathListObject, newDexElements);
        Log.d(TAG, FILE + "loadApk: newDexElements  " + Array.getLength(newDexElements));

    }

}
