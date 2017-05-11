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
package com.pax.pay.trans;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.BitmapFactory;
import android.os.SystemClock;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.TrackUtils;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.manager.neptune.DalManager;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionTransPreDeal;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.LogUtils;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.DialogUtils;
import com.pax.pay.trans.model.BaseTransData.*;

public abstract class BaseTrans extends ATransaction {
    // 当前交易类型
    protected ETransType transType;
    protected TransData transData;

    /**
     * transaction listener
     */
    private TransEndListener transListener;

    public BaseTrans(ETransType transType, TransEndListener transListener) {
        this.transType = transType;
        this.transListener = transListener;
    }

    public BaseTrans() {
    }

    /**
     * set transaction type
     *
     * @param transType
     */
    public void setTransType(ETransType transType) {
        this.transType = transType;
    }

    protected void setTransListener(TransEndListener transListener) {
        this.transListener = transListener;
    }

    /**
     * transaction result prompt
     */
    protected void transEnd(final ActionResult result) {
        LogUtils.i("TAG", transType.toString() + " TRANS--END--");
        dispResult(transType.getTransName(), result, new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                promptDialog = null;
                FinancialApplication.mApp.runInBackground(new Runnable() {

                    @Override
                    public void run() {
                        removeCard();
                        try {
                            DalManager.getPiccInternal().close();
                        } catch (PiccDevException e) {
                            e.printStackTrace();
                        }

                        ActivityStack.getInstance().popAllButBottom();
                        TransContext.getInstance().setCurrentAction(null);
                        if (transListener != null) {
                            transListener.onEnd(result);
                        }

                        setTransRunning(false);
                    }
                });
            }
        });
    }

    /**
     * override execute， add function to judge whether transaction check is running and add transaction pre-deal
     */
    @Override
    public void execute() {
        LogUtils.i("TAG", transType.toString() + " TRANS--START--");
        if (isTransRunning()) {
            setTransRunning(false);
            return;
        }
        setTransRunning(true);

        // transData initial
        transData = Component.transInit();
        // set current context
        ActionTransPreDeal preDealAction = new ActionTransPreDeal(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransPreDeal) action).setParam(transType);
            }
        });
        preDealAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() != TransResult.SUCC) {
                    transEnd(result);
                    return;
                }
                transData.setTransType(transType);
                exe();
            }
        });
        preDealAction.execute();
    }

    /**
     * execute father execute()
     */
    private void exe() {
        super.execute();
    }

    /**
     * whether transaction is running, it's global for all transaction, if insert a transaction in one transaction, control the status itself
     */
    private static boolean isTransRunning = false;

    /**
     * get transaction running status
     *
     * @return
     */
    public static boolean isTransRunning() {
        return isTransRunning;
    }

    /**
     * set transaction running status
     *
     * @param isTransRunning
     */
    public static void setTransRunning(boolean isTransRunning) {
        BaseTrans.isTransRunning = isTransRunning;
    }

    /**
     * save card information and input type after search card
     *
     * @param cardInfo
     * @param transData
     */
    public void saveCardInfo(CardInformation cardInfo, TransData transData) {
        // manual input card number
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.KEYIN) {
            transData.setPan(cardInfo.getPan());
            transData.setExpDate(cardInfo.getExpDate());
            transData.setEnterMode(EnterMode.MANUAL);
            transData.setIssuer(cardInfo.getIssuer());
        } else if (mode == SearchMode.SWIPE) {
            transData.setTrack1(cardInfo.getTrack1());
            transData.setTrack2(cardInfo.getTrack2());
            transData.setTrack3(cardInfo.getTrack3());
            transData.setPan(cardInfo.getPan());
            transData.setExpDate(TrackUtils.getExpDate(cardInfo.getTrack2()));
            transData.setEnterMode(EnterMode.SWIPE);
            transData.setIssuer(cardInfo.getIssuer());
        } else if (mode == SearchMode.INSERT || mode == SearchMode.WAVE) {
            transData.setEnterMode(mode == SearchMode.INSERT ? EnterMode.INSERT : EnterMode.CLSS);
        }
    }

    /**
     * transaction result prompt and deal with remove card
     *
     * @param transName
     * @param result
     * @param dismissListener
     */
    protected void dispResult(String transName, final ActionResult result, OnDismissListener dismissListener) {
        if (result.getRet() == TransResult.SUCC) {
            DialogUtils.showSuccMsg(transName, dismissListener, Constants.SUCCESS_DIALOG_SHOW_TIME);
        } else if (result.getRet() == TransResult.ERR_ABORTED || result.getRet() == TransResult.ERR_HOST_REJECT) {
            // ERR_ABORTED AND ERR_HOST_REJECT  not prompt error message
            dismissListener.onDismiss(null);
        } else if (result.getRet() == TransResult.ERR_USER_CANCEL) {
            DialogUtils.showErrMsg(transName, TransResult.getMessage(result.getRet()),
                    dismissListener, Constants.FAILED_DIALOG_SHOW_TIME);
        } else {
            DialogUtils.showErrMsg(transName, TransResult.getMessage(result.getRet()),
                    dismissListener, Constants.FAILED_DIALOG_SHOW_TIME);
        }
    }

    /**
     * remove card check, need start thread when call this function
     */
    protected void removeCard() {
        while (true) {
            try {
                PollingResult result = DalManager.getCardReaderHelper().polling(EReaderType.ICC_PICC, 100);
                if (result.getReaderType() == EReaderType.ICC || result.getReaderType() == EReaderType.PICC) {
                    // remove card prompt
                    if (result.getReaderType() == EReaderType.ICC) {
                        showWarning(R.string.wait_pull_card);
                    } else {
                        // remove card prompt
                        showWarning(R.string.wait_remove_card);
                    }
                    SystemClock.sleep(500);
                    Device.beepErr();
                } else {
                    if (promptDialog != null) {
                        promptDialog.dismiss();
                    }
                    break;
                }
            } catch (MagDevException | IccDevException | PiccDevException e) {
                e.printStackTrace();
                if (promptDialog != null) {
                    promptDialog.dismiss();
                }
                break;
            }
        }
    }

    private CustomAlertDialog promptDialog;

    /**
     * show warning
     *
     * @param warningResId
     */
    private void showWarning(final int warningResId) {
        FinancialApplication.mApp.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (promptDialog == null) {
                    promptDialog = new CustomAlertDialog(ContextUtils.getActyContext(), CustomAlertDialog.WARN_TYPE);
                    promptDialog.show();
                    promptDialog.setImage(BitmapFactory.decodeResource(ContextUtils.getResources(),
                            R.drawable.ic16));
                    promptDialog.setCancelable(false);
                    promptDialog.setTitleText(transType.getTransName());
                }
                String warning=ContextUtils.getString(warningResId);
                promptDialog.setContentText(warning);
            }
        });
    }

    @Override
    protected void bind(String state, AAction action) {
        super.bind(state, action);
        if (action != null) {
            action.setEndListener(new ActionEndListener() {

                @Override
                public void onEnd(AAction action, final ActionResult result) {
                    FinancialApplication.mApp.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                LogUtils.i("TAG", transType.toString() + " ACTION--" + currentState + "--end");
                                onActionResult(currentState, result);
                            } catch (Exception e) {
                                e.printStackTrace();
                                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                            }

                        }
                    });
                }
            });
        }
    }

    private String currentState;

    @Override
    public void gotoState(String state) {
        this.currentState = state;
        LogUtils.i("TAG", transType.toString() + " ACTION--" + currentState + "--start");
        super.gotoState(state);
    }

    /**
     * deal action result
     *
     * @param currentState ：current State
     * @param result       ：current action result
     */
    public abstract void onActionResult(String currentState, ActionResult result);
}
