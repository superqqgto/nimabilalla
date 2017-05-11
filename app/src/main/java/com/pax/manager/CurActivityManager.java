package com.pax.manager;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 * Created by huangmuhua on 2017/4/2.
 * 保存和获取当前Activity，内部使用弱引用避免单例长期持有Activity造成内存泄露
 */

public class CurActivityManager {

    private WeakReference<Activity> activityWeakRef;

    private static class LazyHolder {
        private static final CurActivityManager INSTANCE = new CurActivityManager();
    }

    private CurActivityManager() {
    }

    public static CurActivityManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void setCurActivity(Activity activity) {
        this.activityWeakRef = new WeakReference<Activity>(activity);
    }

    public Activity getCurActivity() {
        if (null == activityWeakRef) {
            return null;
        }
        Activity currentActivity = activityWeakRef.get();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (null != currentActivity && currentActivity.isDestroyed()) {
                currentActivity = null;
            }
        }
        return currentActivity;
    }

    public static class ActyLifeCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
            CurActivityManager.getInstance().setCurActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }
}
