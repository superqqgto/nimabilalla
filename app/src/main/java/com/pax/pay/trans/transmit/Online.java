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

import com.pax.abl.core.ipacker.IPacker;
import com.pax.abl.core.ipacker.PackListener;
import com.pax.edc.R;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.LogUtils;

import static com.pax.manager.DbManager.getTransDao;

public class Online {
    private static Online online;

    private Online() {

    }

    public static Online getInstance() {
        if (online == null) {
            online = new Online();
        }

        return online;
    }

    class PackListenerImpl implements PackListener {
        private TransProcessListener listener;

        public PackListenerImpl(TransProcessListener listener) {
            this.listener = listener;
        }

        @Override
        public byte[] onCalcMac(byte[] data) {
            if (listener != null) {
                return listener.onCalcMac(data);
            }
            LogUtils.v("Online", "listener == null");
            return null;
        }

        @Override
        public byte[] onEncTrack(byte[] track) {
            if (listener != null) {
                return listener.onEncTrack(track);
            }
            return null;
        }

    }

    private TransProcessListener listener;
    private ATcp tcp;

    public int online(TransData transData, final TransProcessListener listener) {
        try {
            this.listener = listener;
            onShowMsg(ContextUtils.getString(R.string.wait_process));
            ETransType transType = transData.getTransType();
            // 准备打包器
            IPacker<TransData, byte[]> packager = transType.getPackager(new PackListenerImpl(listener));
            IPacker<TransData, byte[]> dupPackager = transType.getDupPackager(new PackListenerImpl(listener));

            int ret;
            tcp = getTcpClient();
            if (tcp == null)
                return TransResult.ERR_CONNECT;
            tcp.setTransProcessListener(listener);
            // 连接
            ret = tcp.onConnect();
            if (ret != 0) {
                return TransResult.ERR_CONNECT;
            }
            // 打包
            byte[] req;
            if (transData.getReversalStatus() == TransData.ReversalStatus.REVERSAL ||
                    transData.getReversalStatus() == TransData.ReversalStatus.PENDING) {
                req = dupPackager.pack(transData);
            } else {
                req = packager.pack(transData);
            }
            if (req == null) {
                return TransResult.ERR_PACK;
            }
            byte[] sendData = new byte[2 + req.length];
            sendData[0] = (byte) (req.length / 256);
            sendData[1] = (byte) (req.length % 256);
            System.arraycopy(req, 0, sendData, 2, req.length);

            // 联机交易标识
            transData.setOnlineTrans(true);

            // 发送数据
            LogUtils.i("TAG", "SEND:" + GlManager.bcdToStr(sendData));
            ret = tcp.onSend(sendData);
            if (ret != 0) {
                return TransResult.ERR_SEND;
            }

            // 冲正处理
            if ((dupPackager != null) &&
                    (transData.getReversalStatus() == TransData.ReversalStatus.NORMAL) &&
                    (transType != ETransType.DCC)) {
                // 保存冲正
                transData.setReversalStatus(TransData.ReversalStatus.PENDING);
                DbManager.getDccTransDataDao().insertDccTransData(transData.getDccTransData());
                DbManager.getTransDao().insertTransData(transData);

            }

            //AET-32、AET31
            // AET-126只要发送成功保存了交易就要增加流水号，避免流水号复用
            // 冲正交易不需要增加流水号
            if (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL &&
                    (transType != ETransType.OFFLINE_TRANS_SEND) &&
                    (transType != ETransType.SETTLE) &&
                    (transType != ETransType.BATCH_UP)) {
                Component.incTransNo();
            }

            // 接收数据
            TcpResponse tcpResponse = tcp.onRecv();

            if (tcpResponse.getRetCode() != TransResult.SUCC) {
                // 更新冲正原因
                if ((dupPackager != null) &&
                        (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL) &&
                        (transType != ETransType.DCC)) {
                    transData.setReversalStatus(TransData.ReversalStatus.PENDING);
                    transData.setDupReason(TransData.DUP_REASON_NO_RECV);
                    getTransDao().updateTransData(transData);
                }
                return TransResult.ERR_RECV;
            }

            //AET-32、AET31
            // 冲正交易不需要增加流水号
            if (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL &&
                    (transType != ETransType.OFFLINE_TRANS_SEND) &&
                    (transType != ETransType.SETTLE) &&
                    (transType != ETransType.BATCH_UP) &&
                    (transType != ETransType.DCC)) {
                Component.incTransNo();
            }

            if (TcpDemoMode.class.equals(tcp.getClass())) {
                createDummyRecvData(transData);
                return TransResult.SUCC;
            }

            LogUtils.i("TAG", "RECV:" + GlManager.bcdToStr(tcpResponse.getData()));
            if ((dupPackager != null) &&
                    (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL) &&
                    (transType != ETransType.DCC)) {
                return dupPackager.unpack(transData, tcpResponse.getData());
            }
            ret = packager.unpack(transData, tcpResponse.getData());
            // 更新冲正原因
            if ((ret == TransResult.ERR_MAC) &&
                    (dupPackager != null )&&
                    (transData.getReversalStatus() != TransData.ReversalStatus.REVERSAL) &&
                    (transType != ETransType.DCC)) {
                transData.setReversalStatus(TransData.ReversalStatus.PENDING);
                transData.setDupReason(TransData.DUP_REASON_MACWRONG);
                transData.setDateTime(transData.getDateTime());
                getTransDao().updateTransData(transData);
            }
            // 如果39域返回null,删除冲正文件, 或者解包3， 4， 11， 41，42域与请求不同时，删除冲正(BCTC要求下笔交易不发冲正)
            if (ret == TransResult.ERR_BAG || ret == TransResult.ERR_PROC_CODE || ret == TransResult.ERR_TRANS_AMT
                    || ret == TransResult.ERR_TRACE_NO || ret == TransResult.ERR_TERM_ID
                    || ret == TransResult.ERR_MERCH_ID) {
                getTransDao().deleteDupRecord();
            }
            return ret;
        } finally {
            if (tcp != null) {
                tcp.onClose();
            }
        }
    }

    private void onShowMsg(String msg) {
        if (listener != null) {
            listener.onShowProgress(msg, Integer.parseInt(SpManager.getSysParamSp().get(SysParamSp.COMM_TIMEOUT)));
        }
    }

    private ATcp getTcpClient() {
        String commType = SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE);
        if (SysParamSp.Constant.COMMTYPE_DEMO.equals(commType)) {
            return new TcpDemoMode();
        } else {
            String sslType = SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE_SSL);
            if (sslType.equals(SysParamSp.Constant.COMM_NO_SSL)) {
                return new TcpNoSsl();
            }
            /*
            else if (sslType.equals(SysParamSp.Constant.COMM_CUP_SSL)) {
                InputStream inputStream = null;
                try {
                    File file = new File(Constants.CACERT_PATH);
                    if (file.exists()) {
                        inputStream = new FileInputStream(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return new TcpCupSsl(inputStream);
            }
            */
        }

        return null;
    }

    private void createDummyRecvData(TransData transData) {
        if (!ETransType.VOID.equals(transData.getTransType())) {
            transData.setAuthCode(transData.getDateTime().substring(8));
        } else {
            transData.setAuthCode("      ");
        }

        transData.setRefNo(transData.getDateTime().substring(2));
        transData.setResponseCode("00");
        if (ETransType.SETTLE.equals(transData.getTransType())
            //&& '2' == transData.getProcCode().getBytes()[1]
                ) {
            transData.setResponseCode("95");
        }
    }
}
