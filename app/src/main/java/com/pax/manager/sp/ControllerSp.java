package com.pax.manager.sp;

import com.pax.manager.AcqManager;
import com.pax.pay.base.Issuer;
import com.pax.pay.trans.TransResult;

import java.util.Calendar;

/**
 * 系统控制参数
 * 删掉了之前的com.pax.trans.model.Controller
 * Created by huangmuhua on 2017/4/23.
 */
public class ControllerSp extends BaseSp {

    public static class Constant {

        public static final int YES = 1;
        public static final int NO = 0;
        /**
         * batch upload type
         */
        public static final int RMBLOG = 1;
        public static final int FRNLOG = 2;
        public static final int ALLLOG = 3;
        public static final int ICLOG = 4;
        /**
         * batch upload status
         */
        public static final int WORKED = 0;
        public static final int BATCH_UP = 1;
    }

    private static final String IS_PARAMFILEEXIST = "IS_PARAMFILEEXIST";

    public static final String IS_FIRST_RUN = "IS_FIRST_RUN";
    //add by xiawh if need setting wizard of not
    public static final String NEED_SET_WIZARD = "need_set_wizard";
    /**
     * is need download capk  NO: not need YES: need
     */
    public static final String NEED_DOWN_CAPK = "need_down_capk";
    /**
     * is need download aid NO: not need YES: need
     */
    public static final String NEED_DOWN_AID = "need_down_aid";
    /**
     * batch upload status {@link Constant#WORKED}not in batch upload , {@link Constant#BATCH_UP}:in batch upload
     */
    public static final String BATCH_UP_STATUS = "batch_up_status";
    /**
     * batch upload type RMBLOG: upload inner card trans FRNLOG upload outside card trans ALLLOG upload all trans ICLOG upload IC trans
     */
    public static final String BATCH_UP_TYPE = "batch_up_type";
    /**
     * check result
     */
    public static final String RESULT = "result";
    /**
     * batch upload number
     */
    public static final String BATCH_NUM = "batch_num";
    /**
     * whether need to clear transaction record: NO: not clear, YES: clear
     */
    public static final String CLEAR_LOG = "clearLog";

    private static class LazyHolder {
        private static final ControllerSp INSTANCE = new ControllerSp();
    }

    private ControllerSp() {
        super(SP_NAME_CONTROL);
        init();
    }

    protected static ControllerSp getInstatnce() {
        return LazyHolder.INSTANCE;
    }


    private void init() {
        if (isParamFileExisted()) {
            return;
        }

        editor.putBoolean(IS_PARAMFILEEXIST, true)
                .putBoolean(IS_FIRST_RUN, true)
                .putInt(NEED_DOWN_CAPK, Constant.YES)
                .putInt(NEED_DOWN_AID, Constant.YES)
                .putInt(BATCH_UP_STATUS, Constant.NO)
                .apply();
    }

    public int getInt(String key) {
        return sp.getInt(key, Constant.NO);
    }

    public boolean getBoolean(String key) {
        if (key.equals(IS_FIRST_RUN)) {
            return sp.getBoolean(key, true);
        }
        return sp.getBoolean(key, false);
    }

    private boolean isParamFileExisted() {
        return sp.getBoolean(IS_PARAMFILEEXIST, false);
    }
}
