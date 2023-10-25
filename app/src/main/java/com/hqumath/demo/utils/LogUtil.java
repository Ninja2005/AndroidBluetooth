/*
 * Copyright (c) 1992-2015, ZheJiang Dahua Technology Stock CO.LTD.
 * All Rights Reserved.
 */

package com.hqumath.demo.utils;

import android.util.Log;

import com.hqumath.demo.BuildConfig;

/**
 * Log统一管理类
 */
public class LogUtil {

    private static boolean isDebug = BuildConfig.DEBUG;//是否需要打印bug，buildTypes.debug中配置

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean b) {
        LogUtil.isDebug = b;
    }

    private static final String TAG = "DEBUG";

    // *********************************下面四个是默认tag的函数**********************************//
    public static void i(String msg) {
        i(TAG, msg);
    }

    public static void d(String msg) {
        d(TAG, msg);
    }

    public static void e(String msg) {
        e(TAG, msg);
    }

    public static void v(String msg) {
        v(TAG, msg);
    }

    public static void w(String msg) {
        w(TAG, msg);
    }

    // *********************************下面是传入自定义tag的函数********************************//
    public static void i(String tag, String msg) {
        i(tag, msg, false);
    }

    public static void d(String tag, String msg) {
        d(tag, msg, false);
    }

    public static void e(String tag, String msg) {
        e(tag, msg, false);
    }

    public static void v(String tag, String msg) {
        v(tag, msg, false);
    }

    public static void w(String tag, String msg) {
        w(tag, msg, false);
    }

    // *********************************下面是传入自定义tag + LineNo********************************//
    public static void i(String tag, String msg, boolean isShowLineNo) {
        if (isDebug) {
            if (isShowLineNo) {
                Log.i(tag, msg + buildStackTraceElements(Thread.currentThread().getStackTrace()));
            } else {
                Log.i(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg, boolean isShowLineNo) {
        if (isDebug) {
            if (isShowLineNo) {
                Log.d(tag, msg + buildStackTraceElements(Thread.currentThread().getStackTrace()));
            } else {
                Log.d(tag, msg);
            }
        }
    }

    public static void e(String tag, String msg, boolean isShowLineNo) {
        if (isDebug) {
            if (isShowLineNo) {
                Log.e(tag, msg + buildStackTraceElements(Thread.currentThread().getStackTrace()));
            } else {
                Log.e(tag, msg);
            }
        }
    }

    public static void v(String tag, String msg, boolean isShowLineNo) {
        if (isDebug) {
            if (isShowLineNo) {
                Log.v(tag, msg + buildStackTraceElements(Thread.currentThread().getStackTrace()));
            } else {
                Log.v(tag, msg);
            }
        }
    }

    public static void w(String tag, String msg, boolean isShowLineNo) {
        if (isDebug) {
            if (isShowLineNo) {
                Log.w(tag, msg + buildStackTraceElements(Thread.currentThread().getStackTrace()));
            } else {
                Log.w(tag, msg);
            }
        }
    }

    // ******************************************************************************************//

    /**
     * 获取当前代码所在类、方法、行数
     *
     * @return 返回当前线程栈信息
     */
    private static String buildStackTraceElements(StackTraceElement[] elements) {
        StringBuilder sb = new StringBuilder();
        sb.append("  \t===> ");
        if (elements.length < 4) {
            Log.e(TAG, "Stack to shallow");
        } else {
            String fullClassName = elements[3].getClassName();
            sb.append(fullClassName.substring(fullClassName.lastIndexOf(".") + 1)).append(".").append(elements[3]
                    .getMethodName()).append("(...)").append(" [").append(elements[3].getLineNumber()).append("行]");
        }
        return sb.toString();
    }

}