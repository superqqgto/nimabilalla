/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-1-6
 * Module Auth: Frose.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.manager;

import android.text.TextUtils;

import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.base.Acquirer;
import com.pax.pay.base.CardRange;
import com.pax.pay.base.Issuer;

import java.sql.SQLException;
import java.util.List;

public class AcqManager {

    private Acquirer curAcquirer;//当前收单行？

    private static class LazyHolder {
        private static final AcqManager INSTANCE = new AcqManager();
    }

    public static AcqManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private AcqManager() {
        init();
    }

    /**
     * 设置当前收单行
     *
     * @param acq
     */
    public void setCurAcq(Acquirer acq) {
        curAcquirer = acq;
    }

    public Acquirer getCurAcq() {
        return curAcquirer;
    }

    public boolean isIssuerSupported(final Acquirer acquirer, final Issuer issuer) {
        try {
            List<Issuer> issuers = DbManager.getAcqDao().lookupIssuersForAcquirer(acquirer);
            return issuers.contains(issuer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isIssuerSupported(final Issuer issuer) {
        try {
            List<Issuer> issuers = DbManager.getAcqDao().lookupIssuersForAcquirer(curAcquirer);
            for (Issuer tmp : issuers) {
                if (tmp.getName().equals(issuer.getName())) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Issuer findIssuerByPan(final String pan) {

        CardRange cardRange = DbManager.getAcqDao().findCardRange(pan);
        if (null == cardRange) {
            return null;
        }
        return cardRange.getIssuer();
    }

    public Issuer findIssuerByPan(final Acquirer acquirer, final String pan) {
        try {
            List<Issuer> issuers = DbManager.getAcqDao().lookupIssuersForAcquirer(acquirer);
            Issuer issuer = findIssuerByPan(pan);
            if (issuer != null && issuers.contains(issuer)) {
                return issuer;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void init() {
        //从Sp中读取出收单行的名字
        String name = SpManager.getSysParamSp().get(SysParamSp.ACQ_NAME);
        if (!TextUtils.isEmpty(name)) {
            //通过收单行名字在数据库中查找到收单行信息
            Acquirer acquirer = DbManager.getAcqDao().findAcquirer(name);
            if (acquirer != null) {
                setCurAcq(acquirer);
            }
        }
    }
}
