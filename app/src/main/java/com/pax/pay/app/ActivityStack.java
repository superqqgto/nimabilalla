/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.app;

import android.app.Activity;

import java.util.Stack;

public class ActivityStack {

    private Stack<Activity> activityStack;
    private static ActivityStack instance;

    private ActivityStack() {
        activityStack = new Stack<>();
    }

    public static ActivityStack getInstance() {
        if (instance == null)
            instance = new ActivityStack();

        return instance;
    }

    public void pop() {
        try {
            Activity activity = activityStack.lastElement();
            if (activity != null) {
                activity.finish();
                activityStack.remove(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从栈的后面开始删除，知道删除自身界面为止
     *
     * @param activity
     */
    public void popTo(Activity activity) {
        if (activity != null) {
            while (true) {
                Activity lastCurrent = top();
                if (activity == top()) {
                    return;
                }
                activityStack.remove(lastCurrent);
                lastCurrent.finish();
            }
        }
    }

    public Activity top() {
        try {
            return activityStack.lastElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void push(Activity activity) {
        activityStack.add(activity);
    }

    // 查找栈中是否存在指定的activity
    public boolean find(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 除站底外，其他pop掉
     */
    public void popAllButBottom() {
        while (true) {
            Activity topActivity = top();
            if (topActivity == null || topActivity == activityStack.firstElement()) {
                break;
            }
            activityStack.remove(topActivity);
            topActivity.finish();
        }

    }

    /**
     * 结束所有栈中的activity
     */
    public void popAll() {
        if (activityStack == null) {
            return;
        }
        while (true) {
            Activity activity = top();
            if (activity == null) {
                break;
            }
            activityStack.remove(activity);
            activity.finish();
        }
    }

    public Activity bottom() {
        return activityStack.firstElement();
    }

}
