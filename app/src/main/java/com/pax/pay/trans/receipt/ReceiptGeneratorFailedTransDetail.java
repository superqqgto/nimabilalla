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
package com.pax.pay.trans.receipt;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.BaseTransData.*;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * offline transaction upload failed detail generator
 *
 * @author Steven.W
 */
@SuppressLint("SimpleDateFormat")
class ReceiptGeneratorFailedTransDetail implements IReceiptGenerator {
    /**
     * @param failedTransList ：failedTransList
     */
    public ReceiptGeneratorFailedTransDetail(List<TransData> failedTransList) {
        this.failedTransList = failedTransList;
    }

    private List<TransData> failedTransList;

    @Override
    public Bitmap generateBitmap() {

        List<TransData> failedList = new ArrayList<>();
        List<TransData> rejectList = new ArrayList<>();

        for (TransData data : failedTransList) {

            if (data.getOfflineSendState() == OfflineStatus.OFFLINE_ERR_SEND) {
                failedList.add(data);
            }
            if (data.getOfflineSendState() == OfflineStatus.OFFLINE_ERR_RESP) {
                rejectList.add(data);
            }
        }

        IPage page = Device.generatePage();

        generateFailedMainInfo(page);
        generateFailedData(page, failedList);
        page.addLine().addUnit("\n", FONT_NORMAL); //AET-71
        generateRejectMainInfo(page);
        generateRejectData(page, rejectList);

        page.addLine().addUnit("\n\n\n\n", FONT_NORMAL); //AET-71

        return GlManager.getImgProcessing().pageToBitmap(page, 384);
    }

    @Override
    public String generateString() {
        return null;
    }

    /**
     * generate failed transaction detail main information
     *
     * @return
     */
    private void generateFailedMainInfo(IPage page) {
        Acquirer acquirer = AcqManager.getInstance().getCurAcq();

        String temp = "";
        // title
        page.addLine().addUnit(ContextUtils.getString(R.string.print_offline_send_failed), FONT_BIG,
                EAlign.CENTER);

        // merchant ID
        page.addLine().addUnit(
                ContextUtils.getString(R.string.receipt_merchant_code), FONT_SMALL, (float) 4)
                .addUnit(acquirer.getMerchantId(), FONT_NORMAL, EAlign.RIGHT, (float) 6);

        // terminal ID/operator ID
        page.addLine().addUnit(
                ContextUtils.getString(R.string.receipt_terminal_code_space), FONT_SMALL, (float) 4)
                .addUnit(acquirer.getTerminalId(), FONT_NORMAL, EAlign.RIGHT, (float) 4);

        // batch NO
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_batch_num_space), FONT_SMALL)
                .addUnit(Component.getPaddedNumber(AcqManager.getInstance().getCurAcq().getCurrBatchNo(), 6),
                        FONT_SMALL, EAlign.RIGHT);

        // data/time
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_date), FONT_SMALL, (float) 4)
                .addUnit(Device.getTime(Constants.TIME_PATTERN_DISPLAY), FONT_SMALL, EAlign.RIGHT, (float) 6);

        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_failed_trans_details), FONT_SMALL,
                EAlign.LEFT);

        // transaction information
        page.addLine().addUnit("VOUCHER", FONT_SMALL, (float) 2)
                .addUnit("TYPE", FONT_SMALL, EAlign.CENTER, (float) 1)
                .addUnit("AMOUNT", FONT_SMALL, EAlign.RIGHT, (float) 3);
        page.addLine().addUnit("CARD NO", FONT_SMALL, (float) 2);
    }

    /**
     * generate failed transaction detail
     */
    private void generateFailedData(IPage page, List<TransData> list) {
        String temp = "";
        for (TransData transData : list) {
            String type = "";

            // transaction NO/transaction type/amount
            temp = CurrencyConverter.convert(Long.parseLong(transData.getAmount()), transData.getCurrency());
            long transNo;
            transNo = transData.getTraceNo();

            page.addLine().addUnit(Component.getPaddedNumber(transNo, 6), FONT_SMALL, (float) 2)
                    .addUnit(type, FONT_SMALL, EAlign.CENTER, (float) 1)
                    .addUnit(temp, FONT_SMALL, EAlign.RIGHT, (float) 3);

            // card NO/auth code
            temp = transData.getPan();
            page.addLine().addUnit(temp, FONT_SMALL, (float) 3);
        }

    }

    /**
     * generate offline transaction upload rejected receipt main information
     *
     * @return
     */
    private void generateRejectMainInfo(IPage page) {
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_reject_trans_details), FONT_SMALL,
                EAlign.LEFT);
        // transaction information
        page.addLine().addUnit("VOUCHER", FONT_SMALL, (float) 2)
                .addUnit("TYPE", FONT_SMALL, EAlign.CENTER, (float) 1)
                .addUnit("AMOUNT", FONT_SMALL, EAlign.RIGHT, (float) 3);
        page.addLine().addUnit("CARD NO", FONT_SMALL, (float) 2);

    }

    /**
     * generate offline transaction rejected detail
     *
     * @return
     */
    private void generateRejectData(IPage page, List<TransData> rejectTransDataList) {
        String temp = "";
        for (TransData transData : rejectTransDataList) {
            ETransType transType = transData.getTransType();
            String type = "";

            if (transType.equals(ETransType.SALE)) {
                type = "S";
            }

            // transaction NO/type/amount
            temp = CurrencyConverter.convert(Long.parseLong(transData.getAmount()), transData.getCurrency());
            page.addLine().addUnit(Component.getPaddedNumber(transData.getTraceNo(), 6), FONT_SMALL, (float) 2)
                    .addUnit(type, FONT_SMALL, EAlign.CENTER, (float) 1)
                    .addUnit(temp, FONT_SMALL, EAlign.RIGHT, (float) 3);

            // card NO/auth code
            temp = transData.getPan();
            page.addLine().addUnit(temp, FONT_SMALL, (float) 3);
        }
    }
}
