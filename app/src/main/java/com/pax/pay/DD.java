package com.pax.pay;

import com.pax.pay.utils.LogUtils;

/**
 * Created by caidz on 2017/4/18.
 */

public class DD {
    private DD() {}
    static void E(String extraString) {
        StackTraceElement ste=new Exception().getStackTrace()[1];
        LogUtils.e("base24",ste.getClassName()+"."+ste.getMethodName()+"["+ste.getLineNumber()+"]"+": "+extraString);
    }
    static void T() {
        StackTraceElement ste=new Exception().getStackTrace()[1];
        LogUtils.e("base24",ste.getClassName()+"."+ste.getMethodName()+"["+ste.getLineNumber()+"]");
    }
}