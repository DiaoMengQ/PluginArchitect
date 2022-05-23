package com.moong.pluginarchitect.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author Diaomengqi 6407002777
 * @desc 封装的 toast 类，使toast弹出时自动取消上一个toast
 */
public class ToastUtil {
    /**
     * Toast实例，用于对本页出现的所有Toast进行处理
     */
    private static Toast myToast;

    /**
     * 此处是一个封装的Toast方法，可以取消掉上一次未完成的，直接进行下一次Toast
     *
     * @param context context
     * @param text    需要toast的内容
     */
    public static void toast(Context context, String text) {
        if (myToast != null) {
            myToast.cancel();
        }
        myToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        myToast.show();
    }

    public static void cancelToast() {
        if (myToast != null) {
            myToast.cancel();
        }
    }
}
