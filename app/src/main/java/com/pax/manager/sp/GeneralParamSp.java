package com.pax.manager.sp;

/**
 * 通用参数
 * 删掉了之前的com.pax.device.GeneralParam
 * Created by huangmuhua on 2017/4/21.
 */

public class GeneralParamSp extends BaseSp {
    //PIN密钥
    public static final String TPK = "TPK";
    //MAC密钥
    public static final String TAK = "TAK";
    //DES密钥
    public static final String TDK = "TDK";

    private static class LazyHolder {
        private static final GeneralParamSp INSTANCE = new GeneralParamSp();
    }

    private GeneralParamSp() {
        super(SP_NAME_GENERAL_PARAM);
    }

    protected static GeneralParamSp getInstance() {
        return LazyHolder.INSTANCE;
    }
}
