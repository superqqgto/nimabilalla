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
import com.pax.gl.comm.ISslKeyStore;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ContextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TcpCupSsl extends ATcp {

    private InputStream keyStoreStream;

    public TcpCupSsl(InputStream keyStoreStream) {
        this.keyStoreStream = keyStoreStream;
    }

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
        ret = connectCupSLL(hostIp, hostPort, timeout);
        if (ret != TransResult.ERR_CONNECT) {
            return ret;
        }
        hostIp = getBackHostIp();
        hostPort = getBackHostPort();
        // 启用备用通讯地址
        onShowMsg(ContextUtils.getString(R.string.wait_connect_other));
        ret = connectCupSLL(hostIp, hostPort, timeout);
        return ret;
    }

    @Override
    public int onSend(byte[] data) {
        try {
            onShowMsg(ContextUtils.getString(R.string.wait_send));
            client.send(getCupSslPackage(data));
            return TransResult.SUCC;
        } catch (CommException e) {
            e.printStackTrace();
        }
        return TransResult.ERR_SEND;
    }

    @Override
    public TcpResponse onRecv() {
        onShowMsg(ContextUtils.getString(R.string.wait_recv));
        String sslType = SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE_SSL);
        try {
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

    private int connectCupSLL(String hostIp, int port, int timeout) {
        if (hostIp == null || hostIp.length() == 0 || hostIp.equals("0.0.0.0")) {
            return TransResult.ERR_CONNECT;
        }

        ISslKeyStore keyStore = GlManager.getCommHelper().createSslKeyStore();
        if (keyStoreStream != null) {
            try {
                keyStoreStream.reset();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        keyStore.setTrustStore(keyStoreStream);
        client = GlManager.getCommHelper().createSslClient(hostIp, port, keyStore);
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

    private byte[] getCupSslPackage(byte[] req) {
        String CUP_HOST_NAME = hostIp + ":" + hostPort;
        String CUP_URL = "http://" + CUP_HOST_NAME + "/unp/webtrans/VPB_lb";

        String httpsReq = "POST ";
        httpsReq += CUP_URL + " HTTP/1.1" + "\r\n";
        httpsReq += "HOST: " + CUP_HOST_NAME + "\r\n";
        httpsReq += "User-Agent: Donjin Http 0.1\r\n";
        httpsReq += "Cache-Control: no-cache\r\n";
        httpsReq += "Content-Type: x-ISO-TPDU/x-auth\r\n";
        httpsReq += "Accept: */*\r\n";
        httpsReq += "Content-Length: " + String.valueOf(req.length) + "\r\n\r\n";
        byte[] header = httpsReq.getBytes();
        byte[] bReq = byteMerger(header, req);
        return byteMerger(bReq, "\r\n".getBytes());
    }

    private byte[] byteMerger(byte[] paramArrayOfByte1, byte[] paramArrayOfByte2) {
        byte[] arrayOfByte = new byte[paramArrayOfByte1.length + paramArrayOfByte2.length];
        System.arraycopy(paramArrayOfByte1, 0, arrayOfByte, 0, paramArrayOfByte1.length);
        System.arraycopy(paramArrayOfByte2, 0, arrayOfByte, paramArrayOfByte1.length, paramArrayOfByte2.length);
        return arrayOfByte;
    }
}
