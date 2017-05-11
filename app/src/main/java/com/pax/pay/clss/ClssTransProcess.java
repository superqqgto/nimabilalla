/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-28
 * Module Author: lixc
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.clss;

import android.content.Context;

import com.pax.abl.utils.TrackUtils;
import com.pax.edc.R;
import com.pax.eventbus.ClssLightStatusEvent;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.ClssPreProcInfo;
import com.pax.jemv.clcommon.ClssPreProcInterInfo;
import com.pax.jemv.clcommon.ClssProgramID;
import com.pax.jemv.clcommon.ClssReaderParam;
import com.pax.jemv.clcommon.ClssTornLogRecord;
import com.pax.jemv.clcommon.ClssTransParam;
import com.pax.jemv.clcommon.ClssVisaAidParam;
import com.pax.jemv.clcommon.CvmType;
import com.pax.jemv.clcommon.DDAFlag;
import com.pax.jemv.clcommon.EmvCapk;
import com.pax.jemv.clcommon.EmvRevocList;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.entrypoint.api.ClssEntryApi;
import com.pax.jemv.paypass.api.ClssPassApi;
import com.pax.jemv.paypass.listener.ClssPassCBFunApi;
import com.pax.jemv.paywave.api.ClssWaveApi;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.pay.base.Issuer;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.action.activity.SearchCardActivity;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class ClssTransProcess {
    private int ret;
    private byte KernelType;
    private byte[] buff = new byte[300];
    private int len;
    private byte MSDType;
    private int TransPath;
    private int ACType = com.pax.jemv.clcommon.ACType.AC_AAC;
    private byte CVMType;
    private ClssWaveParam clssWaveParam = ClssWaveParam.getInstance();
    private ClssPreProcInterInfo clssPreProcInterInfo;

    private String track2;

    private static ClssTransProcess clssTransProcess;

    private ClssTransProcess() {

    }

    public static synchronized ClssTransProcess getInstance() {
        if (clssTransProcess == null) {
            clssTransProcess = new ClssTransProcess();
        }
        return clssTransProcess;
    }

    public CTransResult transProcess(Context context, TransData transData, TransProcessListener transProcessListener) {

        CTransResult result = new CTransResult();

        EventBus.getDefault().post(new ClssLightStatusEvent(SearchCardActivity.CLSSLIGHTSTATUS_PROCESSING));

        ret = ClssEntryApi.clssEntrySetMCVersion((byte) 0x03);
        LogUtils.i("clssEntrySetMCVersion", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return result;
        }

        ret = ClssEntryApi.clssEntryAppSlt(0, 0);
        LogUtils.i("clssEntryAppSlt", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return result;
        }

        while (true) {
            KernType type = new KernType();
            ByteArray daArray = new ByteArray();
            ret = ClssEntryApi.clssEntryFinalSelect(type, daArray);// output parameter?
            KernelType = (byte) type.kernType;
            buff = daArray.data;
            len = daArray.length;
            LogUtils.i("clssEntryFinalSelect", "ret = " + ret + ", Kernel Type = " + KernelType);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ret);
                return result;
            }

            clssPreProcInterInfo = new ClssPreProcInterInfo();
            ret = ClssEntryApi.clssEntryGetPreProcInterFlg(clssPreProcInterInfo);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ret);
                return result;
            }

            ByteArray byteArray = new ByteArray();
            ClssEntryApi.clssEntryGetFinalSelectData(byteArray);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ret);
                return result;
            }
            buff = byteArray.data;
            len = byteArray.length;

            if (KernelType == KernType.KERNTYPE_VIS) {// Paywave transaction
                payWaveProc(context, transData, transProcessListener, result);
            } else if (KernelType == KernType.KERNTYPE_MC) {    // Paypass transaction
                payPassProc(transData, result);
            }

            if (result.getTransResult() == RetCode.CLSS_RESELECT_APP) {
                ret = ClssEntryApi.clssEntryDelCurCandApp();
                if (ret != RetCode.EMV_OK) {
                    result.setTransResult(ret);
                    return result;
                }
                continue;
            }

            if (result.getTransResult() != RetCode.EMV_OK) {
                return result;
            }

            String pan = TrackUtils.getPan(transData.getTrack2());
            transData.setPan(pan);

            String expDate = TrackUtils.getExpDate(transData.getTrack2());
            transData.setExpDate(expDate);

            Issuer issuer = AcqManager.getInstance().findIssuerByPan(pan);
            transData.setIssuer(issuer);
            /*whether acquirer need to support issuer or not
            if (issuer != null && FinancialApplication.acqManager.isIssuerSupported(issuer)){
                FinancialApplication.acqManager.setCurIssuer(issuer);
            } else {
                result.setTransResult(CTransResult.CLSS_ERROR);
            }
            */
            return result;
        }
    }

    private void payWaveProc(Context context, TransData transData, TransProcessListener transProcessListener, CTransResult result) {
        ret = ClssWaveApi.clssWaveCoreInit();
        LogUtils.i("clssWaveCoreInit", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        ret = ClssWaveApi.clssWaveSetFinalSelectData(buff, len);
        LogUtils.i("WaveSetFinalSelectData", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        ClssReaderParam clssReaderParam = new ClssReaderParam();
        ret = ClssWaveApi.clssWaveGetReaderParam(clssReaderParam);
        LogUtils.i("WaveGetReaderParam", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }
        clssWaveParam.setClssReaderParam(clssReaderParam);
        clssReaderParam = clssWaveParam.getClssReaderParam(transData);
        ret = ClssWaveApi.clssWaveSetReaderParam(clssReaderParam);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        ClssVisaAidParam clssVisaAidParam = clssWaveParam.getClssVisaAidParam();
        ret = ClssWaveApi.clssWaveSetVisaAidParam(clssVisaAidParam);
        LogUtils.i("clssWaveSetVisaAidParam", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        //FIXME
        String string = "123";
        ret = ClssWaveApi.clssWaveSetTLVData((short) 0x9F5A, string.getBytes(), 3);
        ByteArray proID = new ByteArray();
        ret = getClssTlv(ClssTlvTag.ProIDTag, proID, KernelType);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        //FIXME
        byte[] bytes1 = string.getBytes();
        ClssPreProcInfo clssPreProcInfo = clssWaveParam.getClssPreProcInfo();
        ClssProgramID clssProgramID = new ClssProgramID(clssPreProcInfo.ulRdClssTxnLmt, clssPreProcInfo.ulRdCVMLmt,
                clssPreProcInfo.ulRdClssFLmt, clssPreProcInfo.ulTermFLmt, bytes1, (byte) proID.length,
                clssPreProcInfo.ucRdClssFLmtFlg, clssPreProcInfo.ucRdClssTxnLmtFlg, clssPreProcInfo.ucRdCVMLmtFlg,
                clssPreProcInfo.ucTermFLmtFlg, clssPreProcInfo.ucStatusCheckFlg, (byte) 0, new byte[4]);
        ret = ClssWaveApi.clssWaveSetDRLParam(clssProgramID);
        LogUtils.i("clssWaveSetDRLParam", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        ClssTransParam paywaveTransParam = clssWaveParam.getPaywaveTransParam();
        ret = ClssWaveApi.clssWaveSetTransData(paywaveTransParam, clssPreProcInterInfo);
        LogUtils.i("clssWaveSetTransData", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        TransactionPath transactionPath = new TransactionPath();
        com.pax.jemv.clcommon.ACType acType = new ACType();
        ret = ClssWaveApi.clssWaveProcTrans(transactionPath, acType);    // output parameter?
        LogUtils.i("clssWaveProcTrans", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        EventBus.getDefault().post(new ClssLightStatusEvent(SearchCardActivity.CLSSLIGHTSTATUS_REMOVECARD));

        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
            transProcessListener.onShowNormalMessageWithConfirm(context.getString(R.string.wait_remove_card),
                    Constants.SUCCESS_DIALOG_SHOW_TIME);
        }

        TransPath = transactionPath.path;
        ACType = acType.type;
        result.setPathResult(TransPath);
        result.setAcResult(ACType);
        LogUtils.i("clssWaveProcTrans", "TransPath = " + TransPath + ", ACType = " + ACType);

        if (ACType == com.pax.jemv.clcommon.ACType.AC_AAC) {
            return;
        }

        if (TransPath == TransactionPath.CLSS_VISA_MSD) {
            MSDType = ClssWaveApi.clssWaveGetMSDType();
            LogUtils.i("clssWaveGetMSDType", "MSDType = " + MSDType);
            //get MSD track 2 data
            ByteArray waveGetTrack2List = new ByteArray();
            ret = ClssWaveApi.clssWaveGetTrack2MapData(waveGetTrack2List);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ret);
                return;
            }
            track2 = GlManager.bcdToStr(waveGetTrack2List.data);
            transData.setTrack2(track2.substring(0, 2 * waveGetTrack2List.length));
        } else if (TransPath == TransactionPath.CLSS_VISA_QVSDC) {
            ret = ClssWaveApi.clssWaveProcRestric();
            LogUtils.i("clssWaveProcRestric", "ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ret);
                return;
            }

            if ((ACType == com.pax.jemv.clcommon.ACType.AC_TC)
                    && (!transData.getTransType().equals(ETransType.REFUND))) {

                // TODO: Exception file check

                //according to EDC
                ClssWaveApi.clssWaveDelAllRevocList();
                ClssWaveApi.clssWaveDelAllCapk();
                AddCapkRevList();

                DDAFlag flag = new DDAFlag();
                ret = ClssWaveApi.clssWaveCardAuth(acType, flag);
                LogUtils.i("clssWaveCardAuth", "ret = " + ret);
                if (ret != RetCode.EMV_OK) {
                    result.setTransResult(ret);
                    return;
                } else {
                    if (flag.flag == DDAFlag.FAIL) {
                        result.setTransResult(RetCode.CLSS_TERMINATE);
                        return;
                    }
                }
            }
        } else if (TransPath == TransactionPath.CLSS_VISA_WAVE2) {
            if (ACType == com.pax.jemv.clcommon.ACType.AC_TC) {
                // TODO: Exception file check

                //according to EDC
                ClssWaveApi.clssWaveDelAllRevocList();
                ClssWaveApi.clssWaveDelAllCapk();
                AddCapkRevList();

                DDAFlag flag = new DDAFlag();
                ret = ClssWaveApi.clssWaveCardAuth(acType, flag);
                LogUtils.i("clssWaveCardAuth", "ret = " + ret);
                if (ret != RetCode.EMV_OK) {
                    result.setTransResult(ret);
                    return;
                }
            }
        }

        CVMType = ClssWaveApi.clssWaveGetCvmType();
        LogUtils.i("clssWaveGetCvmType", "CVMType = " + CVMType);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }
        result.setCvmResult(CVMType);

        //track1
        ByteArray waveGetTrack1List = new ByteArray();
        ret = ClssWaveApi.clssWaveGetTrack1MapData(waveGetTrack1List);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }
        String track1 = GlManager.bcdToStr(waveGetTrack1List.data);
        transData.setTrack1(track1.substring(0, 2 * waveGetTrack1List.length));
        //track2
        if (track2 == null) {
            ByteArray waveGetTrack2List = new ByteArray();
            ret = getClssTlv(ClssTlvTag.Track2Tag, waveGetTrack2List, KernelType);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ret);
                return;
            }
            track2 = GlManager.bcdToStr(waveGetTrack2List.data);
            transData.setTrack2(track2.substring(0, 2 * waveGetTrack2List.length));
        }
    }

    public int getKernelType() {
        return KernelType;
    }

    public int getTransPath() {
        return TransPath;
    }

    private void payPassProc(TransData transData, CTransResult result) {
        int tornSum = 0;
        ByteArray mcGetTLVDataList = new ByteArray();

        ret = ClssPassApi.clssMcCoreInit((byte) 0x01);
        LogUtils.i("clssMcCoreInit", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        ret = ClssPassApi.clssMcSetParam(new byte[]{0x01, 0x01, 0x04}, 3);
        LogUtils.i("clssMcSetParam", "ret = " + ret);

        //FIXME
        // Clss_SetCBFun_SendTransDataOutput_MC
        // ClssTermParamSet_MC
        ClssPassCBFunApi passCBFun = ClssPassCBFunApi.getInstance();
        passCBFun.setICBFun(new ClssPassListen());
        ret = ClssPassApi.clssMCSetCBFunSendTransDataOutput();
        LogUtils.i("SetSendTransDataOutput", "ret = " + ret);


        ret = ClssPassApi.clssMcSetFinalSelectData(buff, len);
        LogUtils.i("McSetFinalSelectData", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        ClssPassParam.SetMcTermParam(transData, buff);

        ret = ClssPassApi.clssMcInitiateApp();
        LogUtils.i("clssMcInitiateApp", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            result.setTransResult(ret);
            return;
        }

        byte PathTypeOut;
        PathTypeOut = ClssPassApi.clssMcReadData();    // PathTypeOut

        getClssTlv(ClssTlvTag.Track2Tag, mcGetTLVDataList, KernelType);
        transData.setTrack2(GlManager.bcdToStr(mcGetTLVDataList.data).substring(0, 2 * mcGetTLVDataList.length));
        LogUtils.i("log", "track2 :" + transData.getTrack2());
        LogUtils.i("log", "length :" + mcGetTLVDataList.length);

        if (PathTypeOut < 0) {
            result.setTransResult(PathTypeOut);  //FIXME
            return;
        }
        if (PathTypeOut == TransactionPath.CLSS_MC_MCHIP) {// MChip = 6

            ClssPassApi.clssMcDelAllRevocListMChip();
            ClssPassApi.clssMcDelAllCAPKMChip();
            AddCapkRevList();

            List<ClssTornLog> logList = DbManager.getTornLogDao().findAllTornLog();
            if (logList != null) {
                tornSum = logList.size();
                if (tornSum > 0) {
                    ClssTornLogRecord[] records = new ClssTornLogRecord[tornSum];
                    for (int i = 0; i < tornSum; ++i) {
                        records[i] = logList.get(i).parse();
                    }
                    ClssPassApi.clssMcSetTornLogMChip(records, tornSum);
                }
            }

            ACType = ClssPassApi.clssMcTransProcMChip();
            LogUtils.i("clssMcTransProcMChip", "ACType = " + ACType);

            //FIXME Torn card, need to save to application
            ClssTornLogRecord[] records = new ClssTornLogRecord[5];
            int[] updateFlg = new int[2];
            ret = ClssPassApi.clssMCGetTornLogMChip(records, updateFlg);
            LogUtils.i("clssMCGetTornLogMChip", "ret = " + ret);
            DbManager.getTornLogDao().deleteAllTornLog();
            if (updateFlg[1] == 1) {
                List<ClssTornLog> tornList = new ArrayList<>();
                for (int i = 0; i < updateFlg[0]; ++i) {
                    ClssTornLog tmp = new ClssTornLog(records[i]);
                    tornList.add(tmp);
                }
                DbManager.getTornLogDao().insertTornLog(tornList);
            }
        } else if (PathTypeOut == TransactionPath.CLSS_MC_MAG) {// mag = 5
            ACType = ClssPassApi.clssMcTransProcMag();
            LogUtils.i("clssMcTransProcMag", "ACType = " + ACType);
        }

        result.setAcResult(ACType);

        LogUtils.i("log", "setDetData :" + GlManager.bcdToStr(ClssPassListen.aucOutcomeParamSet.data));

        if (SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE).equals(SysParamSp.Constant.COMMTYPE_DEMO)) {
            result.setTransResult(RetCode.EMV_OK);
            return;
        }

        if (ClssPassListen.aucOutcomeParamSet.data[0] == 0x70 || ClssPassListen.aucOutcomeParamSet.data[1] == (byte) 0xF0) {
            result.setTransResult(RetCode.EMV_OK);
            //FIXME In fact return app try again
            //result.setTransResult(CTransResult.App_Try_Again);
            return;
        }

        mcGetTLVDataList = new ByteArray();
        getClssTlv(ClssTlvTag.pucListTag, mcGetTLVDataList, KernelType);
        if ((mcGetTLVDataList.data[3] & 0x30) == 0x10) {    // byte3 - CVM
            // signature
            result.setCvmResult((byte) CvmType.RD_CVM_SIG);
            LogUtils.i("CVM", "CVM: signature");
        } else if ((mcGetTLVDataList.data[3] & 0x30) == 0x20) {// byte3 - CVM
            // online pin
            result.setCvmResult((byte) CvmType.RD_CVM_ONLINE_PIN);
            LogUtils.i("CVM", "CVM: online pin");
        }

        result.setTransResult(RetCode.EMV_OK);
    }

    public int getClssTlv(byte[] tag, ByteArray list, int kernType) {
        switch (kernType) {
            case KernType.KERNTYPE_VIS:
                if (tag.length > 2) {
                    ret = RetCode.CLSS_PARAM_ERR;
                } else {
                    short waveTag = tag.length == 2 ? (short) ((tag[1] & 0xFF) | ((tag[0] & 0xFF) << 8)) : (short) ((tag[0] & 0xFF));
                    ret = ClssWaveApi.clssWaveGetTLVData(waveTag, list);
                }
                break;
            case KernType.KERNTYPE_MC:
                ret = ClssPassApi.clssMcGetTLVDataList(tag, (byte) tag.length, (byte) 100, list);
                break;
            default:
                break;
        }

        return ret;
    }

    public void setDetData(byte[] tag, byte[] data, int kernelType) {
        byte[] buf = new byte[tag.length + 1 + data.length];

        System.arraycopy(tag, 0, buf, 0, tag.length);
        buf[tag.length] = (byte) data.length;
        System.arraycopy(data, 0, buf, tag.length + 1, data.length);

        switch (kernelType) {
            case KernType.KERNTYPE_MC:
                ClssPassApi.clssMcSetTLVDataList(buf, buf.length);
                break;
            default:
                break;
        }
    }

    private void AddCapkRevList() {
        ByteArray mcGetTLVDataList = new ByteArray();
        if (getClssTlv(ClssTlvTag.CapkIdTag, mcGetTLVDataList, KernelType) == RetCode.EMV_OK) {
            byte index = mcGetTLVDataList.data[0];
            if (getClssTlv(ClssTlvTag.CapkRidTag, mcGetTLVDataList, KernelType) == RetCode.EMV_OK) {
                byte[] aid = new byte[]{mcGetTLVDataList.data[0], mcGetTLVDataList.data[1]};
                EmvCapk ptCAPKey = null;

                List<com.pax.pay.emv.EmvCapk> capkList = DbManager.getEmvDao().findAllCAPK();
                for (com.pax.pay.emv.EmvCapk capk : capkList) {
                    if (capk.getRID().equals(new String(aid)) && capk.getKeyID() == index) {
                        ptCAPKey = new EmvCapk(capk.getRID().getBytes(), (byte) capk.getKeyID(), (byte) capk.getHashInd(),
                                (byte) capk.getArithInd(), (byte) capk.getModule().length(), capk.getModule().getBytes(),
                                (byte) capk.getExponent().length(), capk.getExponent().getBytes(), capk.getExpDate().getBytes(),
                                capk.getCheckSum().getBytes());
                    }
                }
                EmvRevocList emvRevocList = new EmvRevocList(aid, index, new byte[]{0x00, 0x07, 0x11});
                if (ptCAPKey != null) {
                    switch (KernelType) {
                        case KernType.KERNTYPE_VIS:
                            ClssWaveApi.clssWaveAddCapk(ptCAPKey);
                            ClssWaveApi.clssWaveAddRevocList(emvRevocList);
                            break;
                        case KernType.KERNTYPE_MC:
                            ClssPassApi.clssMcAddCAPKMChip(ptCAPKey);
                            ClssPassApi.clssMcAddRevocListMChip(emvRevocList);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
}
