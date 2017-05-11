package com.pax.manager.sp;

/**
 * Created by huangmuhua on 2017/4/21.
 */

public class SpManager {

    private ControllerSp controlSp;
    private GeneralParamSp generalParamSp;
    private SysParamSp sysParamSp;

    private static class LazyHolder {
        private static final SpManager INSTANCE = new SpManager();
    }

    private SpManager() {
        init();
    }

    private static SpManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private void init() {
        controlSp = ControllerSp.getInstatnce();
        generalParamSp = GeneralParamSp.getInstance();
        sysParamSp = SysParamSp.getInstance();
    }

    public static ControllerSp getControlSp() {
        return getInstance().controlSp;
    }

    public static GeneralParamSp getGeneralParamSp() {
        return getInstance().generalParamSp;
    }

    public static SysParamSp getSysParamSp() {
        return getInstance().sysParamSp;
    }
}
