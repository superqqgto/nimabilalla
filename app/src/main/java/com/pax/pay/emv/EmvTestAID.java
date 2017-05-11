/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-1
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.emv;

import com.pax.manager.DbManager;

public class EmvTestAID extends EmvAid {
    public static final int PART_MATCH = 0;
    public static final int FULL_MATCH = 1;


    public static final EmvTestAID EMV = new EmvTestAID(
            "",
            "A0000000999090",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0010000000",
            "D84004F800",
            "D84000A800",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "008c",
            null
    );

    public static final EmvTestAID JCB_TEST = new EmvTestAID(
            "",
            "F1234567890123",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0010000000",
            "D84004F800",
            "D84000A800",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "008c",
            null
    );


    public static final EmvTestAID JCB_TSET_1 = new EmvTestAID(
            "",
            "A0000000651010",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0010000000",
            "FC60ACF800",
            "FC6024A800",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "0200",
            null
    );

    public static final EmvTestAID VISA_VSDC = new EmvTestAID(
            "VISA CREDIT",
            "A0000000031010",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0010000000",
            "D84004F800",
            "D84000A800",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "008c",
            null
    );

    public static final EmvTestAID VISA_ELECTRON = new EmvTestAID(
            "VISA ELECTRON",
            "A0000000032010",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0010000000",
            "D84004F800",
            "D84000A800",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "008c",
            null
    );


    public static final EmvTestAID MASTER_MCHIP = new EmvTestAID(
            "MCHIP",
            "A0000000041010",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0400000000",
            "F850ACF800",
            "FC50ACA000",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "0002",
            null
    );

    public static final EmvTestAID MASTER_MAESTRO = new EmvTestAID(
            "MAESTRO",
            "A0000000043060",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0400000000",
            "F850ACF800",
            "FC50ACA000",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "0002",
            null
    );

    public static final EmvTestAID MASTER_MAESTRO_US = new EmvTestAID(
            "MAESTRO",
            "A0000000042203",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0400000000",
            "F850ACF800",
            "FC50ACA000",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "0002",
            null
    );

    public static final EmvTestAID MASTER_CIRRUS = new EmvTestAID(
            "CIRRUS",
            "A0000000046000",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0400000000",
            "F850ACF800",
            "FC50ACA000",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "0002",
            null
    );

    public static final EmvTestAID MCC_4 = new EmvTestAID(
            "",
            "A0000000046010",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0400000000",
            "F850ACF800",
            "FC50ACA000",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "0002",
            null
    );

    public static final EmvTestAID MCC_5 = new EmvTestAID(
            "",
            "A0000000101030",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0400000000",
            "F850ACF800",
            "FC50ACA000",
            "000000123456",
            "039F3704",
            "0F9F02065F2A029A039C0195059F3704",
            "0002",
            null
    );

    public static final EmvTestAID AMEX_LIVE = new EmvTestAID(
            "",
            "A00000002501",
            PART_MATCH, 0, 0, 0, 1, 1, 1,
            0, 0,
            "0000000000",
            "0000000000",
            "0000000000",
            "000000123456",
            "039F3704",
            "9F3704",
            "0001",
            null
    );

    public static final EmvTestAID DPAS_LIVE = new EmvTestAID(
            "",
            "A0000001523010",
            PART_MATCH, 0, 0, 0, 1, 0, 1,    // random=off
            0, 0,
            "0000000000",
            "0000000000",
            "0000000000",
            "000000123456",
            "039F3704",
            "9F3704",
            "008C",
            null
    );

    public static final EmvTestAID DPAS_LIVE_1 = new EmvTestAID(
            "",
            "A0000003241010",
            PART_MATCH, 0, 0, 0, 1, 0, 1,    // random=off
            0, 0,
            "0000000000",
            "0000000000",
            "0000000000",
            "000000123456",
            "039F3704",
            "9F3704",
            "008c",
            null
    );

    private EmvTestAID(String appName, String aid, int selFlag, int priority, int targetPer, int maxTargetPer,
                       int floorLimitCheck, int randTransSel, int velocityCheck, long floorLimit, long threshold,
                       String tacDenial, String tacOnline, String tacDefault, String acquierId, String dDOL, String tDOL, String version, String riskManData) {

        setAppName(appName);
        setAid(aid);
        setSelFlag(selFlag);
        setVersion(version);
        setTacDefault(tacDefault);
        setRandTransSel(randTransSel);
        setVelocityCheck(velocityCheck);
        setTacOnline(tacOnline);
        setTacDenial(tacDenial);
        setFloorLimit(floorLimit);
        setFloorLimitCheck(floorLimitCheck);
        setThreshold(threshold);
        setMaxTargetPer(maxTargetPer);
        setTargetPer(targetPer);
        setDDOL(dDOL);
        setTDOL(tDOL);

        setPriority(priority);
        setOnlinePin(0);

        setRdClssFLmt(floorLimit);
        setRdClssFLmtFlg(floorLimitCheck);
        setAcquirerId(acquierId);
        setRdClssTxnLmtFlg(0);
        setRdCVMLmtFlg(0);
        setRiskManageData(riskManData);
    }

    public static void load() {
        // test apps
        DbManager.getEmvDao().insertAID(EMV);
        DbManager.getEmvDao().insertAID(JCB_TEST);
        DbManager.getEmvDao().insertAID(JCB_TSET_1);

        DbManager.getEmvDao().insertAID(VISA_VSDC);
        DbManager.getEmvDao().insertAID(VISA_ELECTRON);

        DbManager.getEmvDao().insertAID(MASTER_MCHIP);
        DbManager.getEmvDao().insertAID(MASTER_MAESTRO);
        DbManager.getEmvDao().insertAID(MASTER_MAESTRO_US);
        DbManager.getEmvDao().insertAID(MASTER_CIRRUS);
        DbManager.getEmvDao().insertAID(MCC_4);
        DbManager.getEmvDao().insertAID(MCC_5);

        DbManager.getEmvDao().insertAID(AMEX_LIVE);
        DbManager.getEmvDao().insertAID(DPAS_LIVE);
        DbManager.getEmvDao().insertAID(DPAS_LIVE_1);
    }
}
