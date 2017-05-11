/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.transmit;

import com.pax.dal.entity.EChannelType;
import com.pax.dal.entity.LanParam;
import com.pax.dal.entity.MobileParam;
import com.pax.edc.R;
import com.pax.gl.comm.IComm;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.DalManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ContextUtils;

public abstract class ATcp {
    /**
     * 建立连接
     *
     * @return
     */
    public abstract int onConnect();

    /**
     * 发送数据
     *
     * @param data
     * @return
     */
    public abstract int onSend(byte[] data);

    /**
     * 接收数据
     *
     * @return
     */
    public abstract TcpResponse onRecv();

    /**
     * 关闭连接
     */
    public abstract void onClose();

    protected IComm client;
    protected String hostIp;
    protected int hostPort;

    protected TransProcessListener transProcessListener;

    /**
     * 设置监听器
     *
     * @param listener
     */
    protected void setTransProcessListener(TransProcessListener listener) {
        this.transProcessListener = listener;
    }

    protected void onShowMsg(String msg) {
        if (transProcessListener != null) {
            transProcessListener.onShowProgress(msg,
                    Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.COMM_TIMEOUT)));
        }
    }

    protected void onShowMsg(String msg, int timeoutSec) {
        if (transProcessListener != null) {
            transProcessListener.onShowProgress(msg, timeoutSec);
        }
    }

    /**
     * 参数设置和路由选择
     *
     * @return
     */
    protected int setCommParam() {

        SysParamSp sysParam = SpManager.getSysParamSp();
        String commType = sysParam.get(SysParamSp.APP_COMM_TYPE);
        EChannelType channelType = EChannelType.WIFI;
        switch (commType) {
            case SysParamSp.Constant.COMMTYPE_LAN:
                channelType = EChannelType.LAN;

                LanParam lanParam = new LanParam();
                lanParam.setDhcp(sysParam.get(SysParamSp.LAN_DHCP).equals(SysParamSp.Constant.YES));
                lanParam.setDns1(sysParam.get(SysParamSp.LAN_DNS1));
                lanParam.setDns2(sysParam.get(SysParamSp.LAN_DNS2));
                lanParam.setGateway(sysParam.get(SysParamSp.LAN_GATEWAY));
                lanParam.setLocalIp(sysParam.get(SysParamSp.LAN_LOCALIP));
                lanParam.setSubnetMask(sysParam.get(SysParamSp.LAN_SUBNETMASK));
                DalManager.getCommManager().setLanParam(lanParam);
                break;
            case SysParamSp.Constant.COMMTYPE_WIFI:
                channelType = EChannelType.WIFI;
                break;
            case SysParamSp.Constant.COMMTYPE_MOBILE:
                channelType = EChannelType.MOBILE;
                // mobile参数设置
                MobileParam param = new MobileParam();
                param.setApn(sysParam.get(SysParamSp.MOBILE_APN));
                param.setPassword(sysParam.get(SysParamSp.MOBILE_PWD));
                param.setUsername(sysParam.get(SysParamSp.MOBILE_USER));
                DalManager.getCommManager().setMobileParam(param);
                break;
            case SysParamSp.Constant.COMMTYPE_DEMO:
                onShowMsg(ContextUtils.getString(R.string.wait_demo_mode), 5);
                return TransResult.SUCC;
            default:
                return TransResult.ERR_CONNECT;

        }
        onShowMsg(ContextUtils.getString(R.string.wait_initialize_net));
            /* FIXME un-comment it ?
            int timeout = Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.COMM_TIMEOUT));
            int ret = commManager.enableChannelExclusive(channelType, timeout);
            if (ret != 0) {
                return TransResult.ERR_COMM_CHANNEL;
            }*/
        return TransResult.SUCC;

    }

    /**
     * 获取主机地址
     *
     * @return
     */
    protected String getMainHostIp() {
        return AcqManager.getInstance().getCurAcq().getIp();
    }

    /**
     * 获取主机端口
     *
     * @return
     */
    protected int getMainHostPort() {
        return AcqManager.getInstance().getCurAcq().getPort();
    }

    /**
     * 获取备份主机地址
     *
     * @return
     */
    protected String getBackHostIp() {
        return AcqManager.getInstance().getCurAcq().getIpBak1();
    }

    /**
     * 获取备份主机端口
     *
     * @return
     */
    protected int getBackHostPort() {
        return AcqManager.getInstance().getCurAcq().getPortBak1();
    }
}
