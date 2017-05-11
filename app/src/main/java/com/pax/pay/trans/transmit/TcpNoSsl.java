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

import com.pax.edc.R;
import com.pax.gl.comm.CommException;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ContextUtils;
import com.pax.manager.sp.SysParamSp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TcpNoSsl extends ATcp {

    @Override
    public int onConnect() {
        int ret = setCommParam();
        if (ret != TransResult.SUCC) {
            return ret;
        }

        int timeout = Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.COMM_TIMEOUT)) * 1000;
        // 启用主通讯地址
        ret = TransResult.ERR_CONNECT;
        hostIp = getMainHostIp();
        hostPort = getMainHostPort();
        onShowMsg(ContextUtils.getString(R.string.wait_connect));
        ret = connectNoSLL(hostIp, hostPort, timeout);
        if (ret != TransResult.ERR_CONNECT) {
            return ret;
        }
        hostIp = getBackHostIp();
        hostPort = getBackHostPort();
        // 启用备用通讯地址
        onShowMsg(ContextUtils.getString(R.string.wait_connect_other));
        ret = connectNoSLL(hostIp, hostPort, timeout);
        return ret;
    }

    @Override
    public int onSend(byte[] data) {
        try {
            onShowMsg(ContextUtils.getString(R.string.wait_send));
            client.send(data);
            return TransResult.SUCC;
        } catch (CommException e) {
            e.printStackTrace();
        }
        return TransResult.ERR_SEND;
    }

    @Override
    public TcpResponse onRecv() {
        try {
            onShowMsg(ContextUtils.getString(R.string.wait_recv));
            byte[] lenBuf = client.recv(2);
            if (lenBuf == null || lenBuf.length != 2) {
                return new TcpResponse(TransResult.ERR_RECV, null);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = (((lenBuf[0] << 8) & 0xff00) | (lenBuf[1] & 0xff));
            byte[] rsp = client.recv(len);
            if (rsp == null || rsp.length != len) {
                return new TcpResponse(TransResult.ERR_RECV, null);
            }
            baos.write(rsp);
            rsp = baos.toByteArray();
            return new TcpResponse(TransResult.SUCC, rsp);
        } catch (IOException | CommException e) {
            e.printStackTrace();
        }

        return new TcpResponse(TransResult.ERR_RECV, null);
    }

    @Override
    public void onClose() {
        try {
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int connectNoSLL(String hostIp, int port, int timeout) {
        if (hostIp == null || hostIp.length() == 0 || hostIp.equals("0.0.0.0")) {
            return TransResult.ERR_CONNECT;
        }

        client = GlManager.getCommHelper().createTcpClient(hostIp, port);
        client.setConnectTimeout(timeout);
        client.setRecvTimeout(timeout);
        try {
            client.connect();
            return TransResult.SUCC;
        } catch (CommException e) {
            e.printStackTrace();
        }
        return TransResult.ERR_CONNECT;
    }

}
