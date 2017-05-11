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
package com.pax.pay.record;

import android.app.Activity;

import com.pax.edc.R;
import com.pax.manager.AcqManager;
import com.pax.manager.DbManager;
import com.pax.manager.sp.ControllerSp;
import com.pax.manager.sp.SpManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.base.Acquirer;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.ReceiptPrintFailedTransDetail;
import com.pax.pay.trans.receipt.ReceiptPrintSettle;
import com.pax.pay.trans.receipt.ReceiptPrintTotal;
import com.pax.pay.trans.receipt.ReceiptPrintTrans;
import com.pax.pay.trans.receipt.ReceiptPrintTransDetail;

import java.util.ArrayList;
import java.util.List;

public class Printer {

    /**
     * 打印最后一笔交易记录
     *
     * @param activity
     */
    public static int printLastTrans(final Activity activity) {
        TransData transData = DbManager.getTransDao().findLastTransData();

        if (transData == null) {
            return TransResult.ERR_NO_TRANS;
        }

        ReceiptPrintTrans receiptPrintTrans = ReceiptPrintTrans.getInstance();
        receiptPrintTrans.print(transData, true, new PrintListenerImpl(activity));

        return TransResult.SUCC;
    }

    /**
     * 打印交易明细
     *
     * @param activity
     */
    //AET-112
    public static int printTransDetail(final String title, final Activity activity, final Acquirer acquirer) {
        // 交易查询
        List<ETransType> list = new ArrayList<>();
        list.add(ETransType.SALE);
        list.add(ETransType.VOID);
        list.add(ETransType.REFUND);
        List<TransData.ETransStatus> filter = new ArrayList<>();
        filter.add(ETransStatus.VOIDED);
        //AET-113
        //filter.add(ETransStatus.ADJUSTED);
        //AET-95
        List<TransData> record = DbManager.getTransDao().findTransData(list, filter, acquirer);

        if (record == null || record.size() == 0) {
            return TransResult.ERR_NO_TRANS;
        }

        ReceiptPrintTransDetail.getInstance().print(title, record, new PrintListenerImpl(activity));
        return TransResult.SUCC;
    }

    /**
     * 打印交易汇总
     *
     * @param activity
     */
    public static void printTransTotal(final Activity activity, final Acquirer acquirer) {
        TransTotal total = DbManager.getTransTotalDao().CalcTotal(acquirer, null);
        ReceiptPrintTotal.getInstance().print(activity.getString(R.string.print_history_total), total,
                new PrintListenerImpl(activity));
    }

    /**
     * 打印上批交易汇总
     */
    public static int printLastBatch(final Activity activity) {
        TransTotal total = DbManager.getTransTotalDao().findLastTransTotal(null, true);
        if (total == null) {
            return TransResult.ERR_NO_TRANS;
        }
        ReceiptPrintTotal.getInstance().print(activity.getString(R.string.print_last_total), total,
                new PrintListenerImpl(activity));
        return TransResult.SUCC;

    }

    // 重打印
    public static void printTransAgain(final Activity activity, final TransData transData) {
        ReceiptPrintTrans receiptPrintTrans = ReceiptPrintTrans.getInstance();
        receiptPrintTrans.print(transData, true, new PrintListenerImpl(activity));

    }

    /**
     * 打印结算总计单
     */
    public static void printSettle(final Activity activity, String title, TransTotal total) {
        int result = SpManager.getControlSp().getInt(ControllerSp.RESULT);
        String resultMsg = null;
        if (result == 1) {
            resultMsg = activity.getString(R.string.print_card_check);
        } else if (result == 2) {
            resultMsg = activity.getString(R.string.print_card_check_uneven);
        } else {
            resultMsg = activity.getString(R.string.print_card_check_err);
        }

        ReceiptPrintSettle.getInstance().print(title, resultMsg, total,
                new PrintListenerImpl(activity));
    }

    /**
     * 打印脱机交易上送失败明细单
     */
    public static int printFailDetail(String title, final Activity activity) {
        // 交易查询
        List<ETransType> list = new ArrayList<>();
        list.add(ETransType.SALE);

        List<TransData.ETransStatus> filter = new ArrayList<>();
        filter.add(ETransStatus.VOIDED);
        filter.add(ETransStatus.ADJUSTED);

        List<TransData> records = DbManager.getTransDao().findTransData(list, filter, AcqManager.getInstance().getCurAcq());
        List<TransData> details = new ArrayList<>();
        if (records == null) {
            return TransResult.ERR_NO_TRANS;
        }

        for (TransData record : records) {
            if (record.getOfflineSendState() != TransData.OfflineStatus.OFFLINE_SENT) {
                // 未成功上送的交易记录 以及 被平台拒绝的交易记录
                details.add(record);
            }
        }

        if (details.size() < 1) {

            return TransResult.ERR_NO_TRANS;
        }

        ReceiptPrintFailedTransDetail.getInstance().print(details, new PrintListenerImpl(activity));
        return TransResult.SUCC;
    }

}
