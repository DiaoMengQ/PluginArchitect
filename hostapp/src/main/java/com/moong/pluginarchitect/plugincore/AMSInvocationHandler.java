package com.moong.pluginarchitect.plugincore;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.moong.pluginarchitect.HostActivity;
import com.moong.pluginarchitect.HostService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author DEMI dmq1212@qq.com
 * @date created on 2021/12/7
 * @desc 每一个动态代理类的调用处理程序都必须实现InvocationHandler接口，
 * 并且每个代理类的实例都关联到了实现该接口的动态代理类调用处理程序中，
 * 当我们通过动态代理对象调用一个方法时候，这个方法的调用就会被转发到实现 InvocationHandler 接口类的 invoke 方法来调用
 */
public class AMSInvocationHandler implements InvocationHandler {
    private static final String TAG = "[MoongPlugin]";
    private static final String FILE = "[AMSInvocationHandler]";
    private Context mContext;
    private Object subject; // 代理目标对象

    public AMSInvocationHandler(Context mContext, Object subject) {
        this.mContext = mContext;
        this.subject = subject;
    }

    /**
     * proxy:代理类代理的真实代理对象com.sun.proxy.$Proxy0
     * method:我们所要调用某个对象真实的方法的Method对象
     * args:指代代理对象方法传递的参数
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            // 拦截 startActivity 方法
            case "startActivity":
                Log.d(TAG, FILE + "invoke: startActivity");
                // pluginActivity 替换成 hostActivity
                // 找到 intent 参数

                for (int i = 0; i < args.length; i++) {
                    // Log.d(TAG, FILE + "args: " + args[i].getClass().getName());

                    if (args[i] instanceof Intent) {
                        Intent intentNew = new Intent();
                        // 替换为宿主apk中注册的Activity，为了通过AMS检查
                        intentNew.setClass(mContext, HostActivity.class);
                        // 保存原来 activity 的信息
                        intentNew.putExtra("actionIntent", (Intent) args[i]);
                        args[i] = intentNew;
                        Log.d(TAG, FILE + "invoke: new Intent");
                        break;
                    }
                }
                break;
            // 拦截启动服务的方法
            case "startService":
                Log.d(TAG, FILE + "invoke: startService");
                for (int i = 0; i < args.length; i++) {
                    // Log.d(TAG, FILE + "args: " + args[i].getClass().getName());

                    if (args[i] instanceof Intent) {
                        Intent intentNewServ = new Intent();
                        // 替换为宿主apk中注册的Activity，为了通过AMS检查
                        intentNewServ.setClass(mContext, HostService.class);
                        // 保存原来 activity 的信息
                        intentNewServ.putExtra("actionServiceIntent", (Intent) args[i]);
                        args[i] = intentNewServ;
                        Log.d(TAG, FILE + "invoke: new Service Intent");
                        break;
                    }
                }
                break;
            default:
                Log.d(TAG, FILE + "method: " + method.getName());
        }

        return method.invoke(subject, args);
    }


}
