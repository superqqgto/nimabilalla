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
package com.pax.pay;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ATransaction.TransEndListener;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.EncUtils;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.sp.SpManager;
import com.pax.manager.sp.SysParamSp;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.Printer;
import com.pax.pay.service.ParseReq;
import com.pax.pay.service.ParseReq.RequestData;
import com.pax.pay.service.ParseResp;
import com.pax.pay.service.Payment;
import com.pax.pay.service.ServiceConstant;
import com.pax.pay.trans.authtrans.AuthTrans;
import com.pax.pay.trans.ReadCardTrans;
import com.pax.pay.trans.RefundTrans;
import com.pax.pay.trans.SaleTrans;
import com.pax.pay.trans.SaleVoidTrans;
import com.pax.pay.trans.SettleTrans;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputPassword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.ReceiptPrintBitmap;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SettingsActivity;
import com.pax.view.dialog.DialogUtils;

import org.json.JSONObject;

public class PaymentActivity extends Activity {

    public static final String REQUEST = "REQUEST";
    private static final int REQUEST_CODE = 100;
    private boolean isInstalledNeptune = true;
    private JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_null);

        Intent intent = getIntent();
        ActivityStack.getInstance().push(this);

        // check if neptune is installed
        isInstalledNeptune = Component.neptuneInstalled(this, new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface arg0) {
                transFinish(new ActionResult(TransResult.ERR_ABORTED, null));
            }
        });

        if (!isInstalledNeptune) {
            return;
        }
        FinancialApplication.mApp.initManagers();
        Device.enableStatusBar(false);
        Device.enableHomeRecentKey(false);

        try {
            String jsonStr = intent.getExtras().getString(REQUEST);
            json = new JSONObject(jsonStr);
        } catch (Exception e) {
            transFinish(new ActionResult(TransResult.ERR_PARAM, null));
            return;
        }

        initDev();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityStack.getInstance().pop();
    }

    /**
     * device initial，bind IPPS
     */
    private void initDev() {
        int ret = ParseReq.getInstance().check(json);
        if (ret != TransResult.SUCC) {
            transFinish(new ActionResult(ret, null));
            return;
        }
        doTrans();
    }

    private void doTrans() {
        RequestData requestData = ParseReq.getInstance().getRequestData();
        String transType = requestData.getTransType();

        switch (transType) {
            case ServiceConstant.TRANS_SALE:
                doSale(requestData);
                break;
            case ServiceConstant.TRANS_VOID:
                doVoid(requestData);
                break;
            case ServiceConstant.TRANS_AUTH:
                doAuth(requestData);
                break;
            case ServiceConstant.TRANS_REFUND:
                doRefund(requestData);
                break;
            case ServiceConstant.TRANS_SETTLE:
                doSettle(requestData);
                break;
            case ServiceConstant.TRANS_PRN_LAST:
                doPrnLast(requestData);
                break;
            case ServiceConstant.TRANS_PRN_ANY:
                doPrnAny(requestData);
                break;
            case ServiceConstant.TRANS_PRN_DETAIL:
                doPrnDetail(requestData);
                break;
            case ServiceConstant.TRANS_PRN_TOTAL:
                doPrnTotal(requestData);
                break;
            case ServiceConstant.TRANS_PRN_LAST_BATCH:
                doPrnLastBatch(requestData);
                break;
            case ServiceConstant.TRANS_GET_CARD_NO:
                doReadCard(requestData);
                break;
            case ServiceConstant.TRANS_SETTING:
                doSetting(requestData);
                break;
            case ServiceConstant.PRN_BITMAP:
                doPrnBitmap(requestData);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInstalledNeptune) {
            return;
        }
        SpManager.getSysParamSp().setUpdateListener(new SysParamSp.UpdateListener() {

            @Override
            public void onErr(String prompt) {
                DialogUtils.showUpdateDialog(PaymentActivity.this, prompt);
            }
        });
        SpManager.getSysParamSp().init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            transFinish(new ActionResult(TransResult.SUCC, null));
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void transFinish(ActionResult result) {
        ActivityStack.getInstance().popAll();
        Payment.getInstance(PaymentActivity.this).setResult(ParseResp.getInstance().parse(result));

    }

    /***************************************************** do sale *****************************************************/

    /**
     * sale
     *
     * @param requestData
     */
    private void doSale(RequestData requestData) {
        new SaleTrans(requestData.getTransAmount(), requestData.getTipAmount(), (byte) -1, true,
                new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
    }

    /**
     * void
     *
     * @param requestData
     */
    private void doVoid(RequestData requestData) {
        String voucherNo = requestData.getVoucherNo();
        if (voucherNo == null || voucherNo.length() == 0) {
            new SaleVoidTrans(new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {
            new SaleVoidTrans(voucherNo, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * refund
     *
     * @param requestData
     */
    private void doRefund(RequestData requestData) {
        TransData transData = new TransData();
        String amount = requestData.getTransAmount();
        if (amount == null || amount.length() == 0) {// enter amount
            // swipe card after enter amount
            new RefundTrans(new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();

        } else {// swipe card
            transData.setAmount(amount);
            new RefundTrans(new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * pre-authorization
     *
     * @param requestData
     */
    private void doAuth(RequestData requestData) {
        new AuthTrans(requestData.getTransAmount(), new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * settle
     *
     * @param requestData
     */
    private void doSettle(RequestData requestData) {
        new SettleTrans(new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * print last transaction
     *
     * @param requestData
     */
    private void doPrnLast(RequestData requestData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = Printer.printLastTrans(PaymentActivity.this);

                transFinish(new ActionResult(result, null));
            }
        }).start();
    }

    /**
     * print detail
     *
     * @param requestData
     */
    private void doPrnDetail(RequestData requestData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = Printer.printTransDetail(getString(R.string.print_history_detail),
                        PaymentActivity.this, AcqManager.getInstance().getCurAcq());

                transFinish(new ActionResult(result, null));
            }
        }).start();
    }

    /**
     * print total
     *
     * @param requestData
     */
    private void doPrnTotal(RequestData requestData) {
        //FIXME may have bug the getCurAcq
        new Thread(new Runnable() {
            @Override
            public void run() {
                Printer.printTransTotal(PaymentActivity.this, AcqManager.getInstance().getCurAcq());

                transFinish(new ActionResult(TransResult.SUCC, null));
            }
        }).start();
    }

    /**
     * print last batch
     *
     * @param requestData
     */
    private void doPrnLastBatch(RequestData requestData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = Printer.printLastBatch(PaymentActivity.this);

                transFinish(new ActionResult(result, null));
            }
        }).start();
    }

    /**
     * print any transaction
     *
     * @param requestData
     */
    private void doPrnAny(RequestData requestData) {
        ActionInputTransData prnAnyAction = new ActionInputTransData(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(getString(R.string.manage_menu_deal_inquiry))
                        .setInputLine1(getString(R.string.prompt_input_transno), EInputType.NUM, 6, false);
                TransContext.getInstance().setCurrentAction(action);
            }
        }, 1);
        prnAnyAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() != TransResult.SUCC) {
                    transFinish(new ActionResult(TransResult.ERR_HOST_REJECT, null));
                    return;
                }

                String content = (String) result.getData();
                if (content == null || content.length() == 0) {
                    ToastUtils.showShort(R.string.please_input_again);
                    return;
                }
                long transNo = Long.parseLong(content);
                final TransData transData = DbManager.getTransDao().findTransDataByTraceNo(transNo);

                if (transData == null) {
                    transFinish(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
                    return;
                }

                ActivityStack.getInstance().pop();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Printer.printTransAgain(PaymentActivity.this, transData);
                        transFinish(new ActionResult(TransResult.SUCC, null));
                    }
                }).start();
            }
        });
        prnAnyAction.execute();
    }

    /**
     * read card NO
     *
     * @param requestData
     */
    private void doReadCard(RequestData requestData) {
        new ReadCardTrans(new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * terminal setting
     *
     * @param requestData
     */
    private void doSetting(RequestData requestData) {
        ActionInputPassword inputPasswordAction = new ActionInputPassword(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPassword) action).setParam(8, getString(R.string.prompt_sys_pwd), null);
                TransContext.getInstance().setCurrentAction(action);
            }
        });

        inputPasswordAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    transFinish(result);
                    return;
                }

                String data = EncUtils.SHA1((String) result.getData());
                if (!data.equals(SpManager.getSysParamSp().get(SysParamSp.SEC_SYSPWD))) {
                    transFinish(new ActionResult(TransResult.ERR_PASSWORD, null));
                    return;
                }

                Intent intent = new Intent(PaymentActivity.this, SettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.settings_title));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        inputPasswordAction.execute();
    }

    /**
     * print bitmap
     *
     * @param requestData
     */
    private void doPrnBitmap(RequestData requestData) {
        final String bitmapStr = requestData.getBitmap();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReceiptPrintBitmap.getInstance().print(bitmapStr, new PrintListenerImpl(PaymentActivity.this));
                transFinish(new ActionResult(TransResult.SUCC, null));
            }
        }).start();
    }

}
