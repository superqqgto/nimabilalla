package com.pax.pay.password;

import com.pax.abl.utils.EncUtils;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;

/**
 * Created by wangyq on 2017/5/9.
 */

public class ChangeVoidRefundPwdActivity extends BaseChangePwdActivity {
    @Override
    protected void savePwd() {
        SpManager.getSysParamSp().set(SysParamSp.SEC_VOIDREFUNDPWD, EncUtils.SHA1(pwd));
    }
}
