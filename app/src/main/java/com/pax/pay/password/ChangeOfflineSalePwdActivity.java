package com.pax.pay.password;

import com.pax.abl.utils.EncUtils;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;

/**
 * Created by chenzaoyang on 2017/4/19.
 */

public class ChangeOfflineSalePwdActivity extends BaseChangePwdActivity {
    @Override
    protected void savePwd() {
        SpManager.getSysParamSp().set(SysParamSp.SEC_OFFLINEPWD, EncUtils.SHA1(pwd));
    }
}
