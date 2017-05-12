package com.pax.pay.app.quickclick;

import android.view.View;

import com.pax.pay.utils.ToastUtils;

/**
 * Created by huangmuhua on 2017/5/6.
 */

public class QuickClickUtils {

    private static long lastClickTime;
    private static int lastClickedKeyCode = -Integer.MAX_VALUE;
    private static View lastClickedView;
    private static final long DEFAULT_MIN_INTERVAL = 500;

    public static boolean isFastDoubleClick(int keyCode, long minInterval) {
        long clickTime = System.currentTimeMillis();
        long interval = clickTime - lastClickTime;
        if (0 < interval && interval < minInterval) {
            if (-Integer.MAX_VALUE != keyCode && keyCode == lastClickedKeyCode) {
                ToastUtils.showShort("手别太快！");
                return true;
            }
        }
        lastClickTime = clickTime;
        lastClickedKeyCode = keyCode;
        return false;
    }

    public static boolean isFastDoubleClick(int keyCode) {
        return isFastDoubleClick(keyCode, DEFAULT_MIN_INTERVAL);
    }

    /**
     * whether the same view is clicked twice in a short time
     *
     * @param view        the clicked view
     * @param minInterval the specified short time, unit: milli seconds
     * @return
     */
    public static boolean isFastDoubleClick(View view, long minInterval) {
        long clickTime = System.currentTimeMillis();
        long interval = clickTime - lastClickTime;

        if (0 < interval && interval < minInterval) {
            if (null != view && view.equals(lastClickedView)) {
                return true;
            }
        }
        lastClickTime = clickTime;
        lastClickedView = view;
        return false;
    }

    public static boolean isFastDoubleClick(View view) {
        return isFastDoubleClick(view, DEFAULT_MIN_INTERVAL);
    }
}
