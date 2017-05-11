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

import com.pax.abl.utils.PanUtils;
import com.pax.device.Device;
import com.pax.edc.R;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.manager.AcqManager;
import com.pax.manager.neptune.GlManager;
import com.pax.pay.base.Acquirer;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.ContextUtils;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.TimeConverter;

import java.util.List;

/**
 * transaction detail generator
 *
 * @author Steven.W
 */
@SuppressLint("SimpleDateFormat")
class ReceiptGeneratorTransDetail implements IReceiptGenerator {
    /**
     * generate transaction detail, use secondary construction method
     *
     * @param transDataList ：transDataList
     */
    public ReceiptGeneratorTransDetail(List<TransData> transDataList) {
        this.transDataList = transDataList;
    }

    /**
     * generate detail main information, use this construction method
     */
    public ReceiptGeneratorTransDetail() {

    }

    private List<TransData> transDataList;

    @Override
    public Bitmap generateBitmap() {
        IPage page = Device.generatePage();
        String temp = "";
        String temp2 = "";
        String date = "";

        for (TransData transData : transDataList) {
            ETransType transType = transData.getTransType();
            String type = transType.toString();

            // AET-18
            // transaction NO/transaction type/amount
            if (transType.isSymbolNegative()) {
                temp = CurrencyConverter.convert(0 - Long.parseLong(transData.getAmount()), transData.getCurrency());
            } else {
                temp = CurrencyConverter.convert(Long.parseLong(transData.getAmount()), transData.getCurrency());
            }
            temp2 = Component.getPaddedNumber(transData.getTraceNo(), 6);
            date = TimeConverter.convert(transData.getDateTime(), Constants.TIME_PATTERN_TRANS,
                    Constants.TIME_PATTERN_DISPLAY);
            //AET-125
            page.addLine().addUnit(temp2, FONT_NORMAL, EAlign.LEFT).addUnit(date, FONT_NORMAL, EAlign.RIGHT, (float) 3);
            page.addLine().addUnit(type, FONT_NORMAL, EAlign.LEFT).addUnit(temp, FONT_NORMAL, EAlign.RIGHT, (float) 3);

            // card NO/auth code
            temp = PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern());
            temp2 = transData.getAuthCode() == null ? "" : transData.getAuthCode();

            page.addLine().addUnit(temp, FONT_NORMAL, (float) 3).addUnit(temp2, FONT_NORMAL, EAlign.RIGHT);
            page.addLine().addUnit("\n", FONT_NORMAL);
        }

        page.addLine().addUnit("\n\n\n\n", FONT_NORMAL);

        IImgProcessing imgProcessing = GlManager.getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

    @Override
    public String generateString() {
        return null;
    }

    /**
     * generate detail information
     *
     * @param title
     * @return
     */
    public Bitmap generateMainInfo(String title) {
        IPage page = Device.generatePage();

        Acquirer acquirer = AcqManager.getInstance().getCurAcq();

        // title
        page.addLine().addUnit(title, FONT_BIG, EAlign.CENTER);

        //  merchant ID
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_merchant_code), FONT_SMALL, (float) 4)
                .addUnit(acquirer.getMerchantId(), FONT_NORMAL, EAlign.RIGHT, (float) 6);

        // terminal ID/operator ID
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_terminal_code), FONT_SMALL, (float) 4)
                .addUnit(acquirer.getTerminalId(), FONT_NORMAL, EAlign.RIGHT, (float) 4);

        // batch NO
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_batch_num_space), FONT_SMALL)
                .addUnit(Component.getPaddedNumber(acquirer.getCurrBatchNo(), 6),
                        FONT_SMALL, EAlign.RIGHT);

        // date/time
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_date), FONT_SMALL, (float) 4)
                .addUnit(Device.getTime(Constants.TIME_PATTERN_DISPLAY), FONT_SMALL, EAlign.RIGHT, (float) 6);

        // transaction information
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_voucher), FONT_SMALL, EAlign.LEFT)
                .addUnit(ContextUtils.getString(R.string.receipt_date), FONT_SMALL, EAlign.RIGHT);
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_type), FONT_SMALL, EAlign.LEFT)
                .addUnit(ContextUtils.getString(R.string.receipt_amount), FONT_SMALL, EAlign.RIGHT);
        page.addLine().addUnit(ContextUtils.getString(R.string.receipt_card_no), FONT_SMALL, (float) 2)
                .addUnit(ContextUtils.getString(R.string.receipt_auth_code), FONT_SMALL, EAlign.RIGHT);

        IImgProcessing imgProcessing = GlManager.getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

}
