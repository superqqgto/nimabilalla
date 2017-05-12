package com.pax.pay.app.quickclick;

import android.view.View;

import com.pax.pay.utils.ToastUtils;

/**
 * Created by huangmuhua on 2017/5/6.
 */

public class QuickClickUtils {

    private static long lastClickTime;
    private static View lastClickedView;
    private static final long DEFAULT_MIN_INTERVAL = 500;

    public static boolean isFastDoubleClick(long minInterval) {
        long clickTime = System.currentTimeMillis();
        long interval = clickTime - lastClickTime;
        if (0 < interval && interval < minInterval) {
            ToastUtils.showShort("手别太快！");
            return true;
        }
        lastClickTime = clickTime;
        return false;
    }

    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(DEFAULT_MIN_INTERVAL);
    }

    public static boolean isFastDoubleClick(View view, long minInterval) {
        long clickTime = System.currentTimeMillis();
        long interval = clickTime - lastClickTime;
        if (0 < interval && interval < minInterval && view.equals(lastClickedView)) {
            return true;
        }
        lastClickTime = clickTime;
        lastClickedView = view;
        return false;
    }

    public static boolean isFastDoubleClick(View view) {
        return isFastDoubleClick(view, DEFAULT_MIN_INTERVAL);
    }
}
