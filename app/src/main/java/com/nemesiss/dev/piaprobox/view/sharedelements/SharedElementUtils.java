package com.nemesiss.dev.piaprobox.view.sharedelements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;


@SuppressLint("PrivateApi")
public class SharedElementUtils {

    private static Class<?> CLAZZ;

    static {
        try {
            CLAZZ = Class.forName("android.app.ActivityTransitionState");
        } catch (ClassNotFoundException e) {
            Log.e("SharedElementUtils", "load class android.app.ActivityTransitionState failed!", e);
        }
    }

    public static void setPendingExitSharedElements(Activity activity, ArrayList<String> elements) {
        try {
            Field mActivityTransitionStateField = Activity.class.getDeclaredField("mActivityTransitionState");
            mActivityTransitionStateField.setAccessible(true);
            Object mActivityTransitionStateObject = mActivityTransitionStateField.get(activity);
            Field mPendingExitNamesField = CLAZZ.getDeclaredField("mPendingExitNames");
            mPendingExitNamesField.setAccessible(true);
            mPendingExitNamesField.set(mActivityTransitionStateObject, elements);
        } catch (Throwable thr) {
            Log.e("SharedElementUtils", "reflective set pending exit shared elements failed!", thr);
        }
    }
}
