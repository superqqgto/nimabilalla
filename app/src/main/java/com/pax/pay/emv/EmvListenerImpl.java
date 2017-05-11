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
package com.pax.pay.emv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.ConditionVariable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.TrackUtils;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.eemv.IEmvListener;
import com.pax.eemv.entity.Amounts;
import com.pax.eemv.entity.CandList;
import com.pax.eemv.enums.ECertType;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eventbus.ClssLightStatusEvent;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.ITlv.ITlvDataObjList;
import com.pax.gl.packer.TlvException;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.neptune.EmvManager;
import com.pax.manager.neptune.GlManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.base.Issuer;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.activity.SearchCardActivity;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.Online;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.Transmit;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.LogUtils;
import com.pax.pay.utils.RspCodeUtils;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class EmvListenerImpl implements IEmvListener {
    private static ConditionVariable cv;
    private static int intResult;
    private static final String TAG = EmvListenerImpl.class.getSimpleName();
    private Context context;
    private static TransData transData;
    TransProcessListener transProcessListener;

    //用于传递给UI显示
    public static String cardnum;
    public static String holdername;
    public static String expdate;
    //
    public static float percent;

    public EmvListenerImpl(Context context, TransData transData, TransProcessListener listener) {
        this.context = context;
        EmvListenerImpl.transData = transData;
        intResult = -1;
        this.transProcessListener = listener;
    }

    @Override
    public int onCardHolderPwd(final boolean isOnlinePin, final int offlinePinLeftTimes, byte[] pinData) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        cv = new ConditionVariable();
        intResult = 0;

        enterPin(isOnlinePin, offlinePinLeftTimes);

        if (isOnlinePin) {
            cv.block();
        }

        return intResult;
    }

    @Override
    public int onCertVerify(final ECertType arg0, final String arg1) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        cv = new ConditionVariable();
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String title = "";
                switch (arg0) {
                    case ENTRY:
                        title = context.getString(R.string.emv_arrival_card);
                        break;
                    case ID:
                        title = context.getString(R.string.emv_identity_card);
                        break;
                    case OFFICER:
                        title = context.getString(R.string.emv_officers_card);
                        break;
                    case OTHER_CREDENTIAL:
                        title = context.getString(R.string.emv_other_card);
                        break;
                    case PASSPORT:
                        title = context.getString(R.string.emv_passport);
                        break;
                    case TEMPORARY_ID:
                        title = context.getString(R.string.emv_interim_identity_card);
                        break;
                    default:
                        break;
                }

                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
                dialog.setTitleText(title);
                dialog.setContentText(arg1);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                dialog.showCancelButton(true);
                dialog.setCancelClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
                        alertDialog.dismiss();
                        cv.open();
                    }
                });
                dialog.showConfirmButton(true);
                dialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
                        alertDialog.dismiss();
                        cv.open();
                    }
                });
            }
        });

        cv.block();
        return intResult;
    }

    @Override
    public boolean onChkExceptionFile() {

        LogUtils.e("TAG", "onChkExceptionFile");
        byte[] track2 = EmvManager.getTlv(0x57);
        String strTrack2 = GlManager.bcdToStr(track2);
        strTrack2 = strTrack2.split("F")[0];
        // 卡号
        String pan = TrackUtils.getPan(strTrack2);
        boolean ret = DbManager.getCardBinDao().isBlack(pan);
        if (ret) {
            transProcessListener.onShowErrMessageWithConfirm(context.getString(R.string.emv_card_in_black_list), Constants.FAILED_DIALOG_SHOW_TIME);
            return true;
        }

        return false;
    }

    @Override
    public int onConfirmCardNo(final String cardno) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }

        Issuer issuer = AcqManager.getInstance().findIssuerByPan(cardno);
        if (issuer != null && AcqManager.getInstance().isIssuerSupported(issuer)) {
            transData.setIssuer(issuer);
        } else {
            intResult = EEmvExceptions.EMV_ERR_DATA.getErrCodeFromBasement();
            return intResult;
        }

        percent = transData.getIssuer().getAdjustPercent();

        cv = new ConditionVariable();

        cardnum = cardno;
        byte[] holderNameBCD = EmvManager.getTlv(0x5F20);
        holdername = new String(holderNameBCD);  //holderNameBCD.toString不行
        byte[] expDateBCD = EmvManager.getTlv(0x5F24);
        expdate = GlManager.bcdToStr(expDateBCD);

        if (!Issuer.validPan(transData.getIssuer(), cardno) ||
                !Issuer.validCardExpiry(transData.getIssuer(), expdate)) {
            intResult = EEmvExceptions.EMV_ERR_DATA.getErrCodeFromBasement();
            return intResult;
        }

        EventBus.getDefault().post(new ClssLightStatusEvent(SearchCardActivity.CARD_NUM_CONFIRM));

        cv.block();

        // FIXME workaround for DEMO mode
        String commType = SpManager.getSysParamSp().get(SysParamSp.APP_COMM_TYPE);
        if (SysParamSp.Constant.COMMTYPE_DEMO.equals(commType)) {
            return EEmvExceptions.EMV_ERR_UNKNOWN.getErrCodeFromBasement();
        }
        return intResult;
    }

    @Override
    public boolean onConfirmECTips() {
        LogUtils.e("TAG", "onConfirmECTips");
        return false;
    }

    @Override
    public int onDetectRFCardAgain() {
        LogUtils.e("TAG", "onDetectRFCardAgain");
        return 0;
    }

    @Override
    public Amounts onGetAmounts() {
        return null;
    }

    @Override
    public EOnlineResult onOnlineProc() {
        try {
            ETransType transType = transData.getTransType();
            // read ARQC
            byte[] arqc = EmvManager.getTlv(0x9f26);
            if (arqc != null && arqc.length > 0) {
                transData.setArqc(GlManager.bcdToStr(arqc));
            }

            // 处理冲正
            Transmit.getInstance().sendReversal(transProcessListener);

            // 生成联机的55域数据
            byte[] f55 = EmvTags.getF55(transType, false);
            byte[] f55Dup = EmvTags.getF55(transType, true);

            transData.setSendIccData(GlManager.bcdToStr(f55));
            if (f55Dup != null && f55Dup.length > 0) {
                transData.setDupIccData(GlManager.bcdToStr(f55Dup));
            }
            Component.saveCardInfoAndCardSeq(transData);

            // 联机通讯
            int commResult = 0;
            if (transProcessListener != null) {
                transProcessListener.onUpdateProgressTitle(transType.getTransName());
            }
            int ret = Online.getInstance().online(transData, transProcessListener);
            LogUtils.i(TAG, "Online  ret = " + ret);
            if (ret == TransResult.SUCC) {
                commResult = 1;
                if (!transData.getResponseCode().equals("00")) {
                    commResult = 2; // 联机拒绝
                }
            } else {
                if (transProcessListener != null) {
                    transProcessListener.onShowErrMessageWithConfirm(TransResult.getMessage(ret),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }
                return EOnlineResult.ABORT;
            }

            String rspF55 = transData.getRecvIccData();
            LogUtils.i(TAG, "rspF55 = " + rspF55);
            ITlv tlv = GlManager.getPacker().getTlv();
            // 测试交易上送， 模拟平台下发脚本
            //rspF55 = "72289F1804AABBCCDD86098424000004AABBCCDD86098418000004AABBCCDD86098416000004AABBCCDD";

            if (rspF55 != null && rspF55.length() > 0) {
                // 设置授权数据
                byte[] resp55 = GlManager.strToBcdPaddingLeft(rspF55);
                ITlvDataObjList list = tlv.unpack(resp55);

                byte[] value91 = list.getValueByTag(0x91);
                if (value91 != null && value91.length > 0) {
                    EmvManager.setTlv(0x91, value91);
                }
                // 设置脚本 71
                byte[] value71 = list.getValueByTag(0x71);
                if (value71 != null && value71.length > 0) {
                    EmvManager.setTlv(0x71, value71);
                }

                // 设置脚本 72
                byte[] value72 = list.getValueByTag(0x72);
                if (value72 != null && value72.length > 0) {
                    EmvManager.setTlv(0x72, value72);
                }
            }

            if (commResult != 1) {
                DbManager.getTransDao().deleteDupRecord();
                Device.beepErr();
                if (transProcessListener != null) {
                    transProcessListener.onShowErrMessageWithConfirm(
                            context.getString(R.string.prompt_err_code) + transData.getResponseCode(),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                }
                return EOnlineResult.DENIAL;
            }
            // 设置授权码
            String authCode = transData.getAuthCode();
            if (authCode != null && authCode.length() > 0) {
                EmvManager.setTlv(0x89, authCode.getBytes());
            }
            EmvManager.setTlv(0x8A, "00".getBytes());
            // write transaction record
            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            transData.setDupReason("");
            DbManager.getTransDao().updateTransData(transData);
            return EOnlineResult.APPROVE;

        } catch (TlvException e) {
            e.printStackTrace();
        } finally {
            if (transProcessListener != null)
                transProcessListener.onHideProgress();
        }

        return EOnlineResult.FAILED;
    }

    @Override
    public void onRemoveCardPrompt() {

    }

    @Override
    public int onSetParam(byte arg0, byte[] arg1) {
        return 0;
    }

    @Override
    public int onWaitAppSelect(final boolean arg0, final List<CandList> arg1) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        cv = new ConditionVariable();
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if (arg0) {
                    builder.setTitle(context.getString(R.string.emv_application_choose));
                } else {
                    SpannableString sstr = new SpannableString(context.getString(R.string.emv_application_choose_again));
                    sstr.setSpan(new ForegroundColorSpan(Color.RED), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setTitle(sstr);
                }
                String[] appNames = new String[arg1.size()];
                for (int i = 0; i < appNames.length; i++) {
                    appNames[i] = arg1.get(i).getAppName();
                }
                builder.setSingleChoiceItems(appNames, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        intResult = arg1;
                        arg0.dismiss();
                        cv.open();
                    }
                });

                builder.setPositiveButton(context.getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
                                arg0.dismiss();
                                cv.open();
                            }
                        });
                builder.setCancelable(false);
                builder.create().show();

            }
        });

        cv.block();
        return intResult;
    }

    private void enterPin(boolean isOnlinePin, int offlinePinLeftTimes) {
        if (isOnlinePin) { // 联机密码
            final String header = context.getString(R.string.prompt_pin);
            final String subHeader = context.getString(R.string.prompt_no_pin);

            final String totalAmount = transData.getTransType().isSymbolNegative() ? "-" + transData.getAmount() : transData.getAmount();
            final String tipAmount = transData.getTransType().isSymbolNegative() ? null : transData.getTipAmount();

            ActionEnterPin actionEnterPin = new ActionEnterPin(new AAction.ActionStartListener() {

                @Override
                public void onStart(AAction action) {

                    byte[] track2 = EmvManager.getTlv(0x57);
                    String strTrack2 = GlManager.bcdToStr(track2);
                    strTrack2 = strTrack2.split("F")[0];
                    String pan = strTrack2.split("D")[0];

                    ((ActionEnterPin) action).setParam(transData.getTransType()
                                    .getTransName(), pan, true, header, subHeader,
                            totalAmount, tipAmount, ActionEnterPin.EEnterPinType.ONLINE_PIN);


                }
            });

            actionEnterPin.setEndListener(new AAction.ActionEndListener() {

                @Override
                public void onEnd(AAction action, ActionResult result) {
                    int ret = result.getRet();
                    if (ret == TransResult.SUCC) {
                        String data = (String) result.getData();
                        transData.setPin(data);
                        if (data != null && data.length() > 0) {
                            transData.setHasPin(true);
                            intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
                        } else {
                            intResult = EEmvExceptions.EMV_ERR_NO_PASSWORD.getErrCodeFromBasement(); // bypass
                        }

                        if (cv != null)
                            cv.open();
                    } else {
                        intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
                        if (cv != null) {
                            cv.open();
                        }
                    }
                    ActivityStack.getInstance().pop();
                }
            });
            actionEnterPin.execute();
        }
    }

    public static void cardNumConfigErr() {
        intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
        cv.open();
    }

    public static void cardNumConfigSucc() {
        intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
        cv.open();
    }

    public static void cardNumConfigSucc(String amount, String tipAmount) {
        transData.setAmount(String.valueOf(CurrencyConverter.parse(amount)));
        transData.setTipAmount(String.valueOf(CurrencyConverter.parse(tipAmount)));
        intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
        cv.open();
    }
}
