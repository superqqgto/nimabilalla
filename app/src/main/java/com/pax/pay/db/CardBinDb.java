/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-15
 * Module Auth: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.db;

import android.content.Context;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.pax.pay.emv.CardBin;
import com.pax.pay.emv.CardBinBlack;

import java.io.IOException;
import java.sql.SQLException;

public class CardBinDb {
    private RuntimeExceptionDao<CardBin, Integer> cardBinDao = null;
    private RuntimeExceptionDao<CardBinBlack, Integer> cardBinBlackDao = null;

    private CardBinDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static CardBinDb instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized CardBinDb getInstance(Context context) {
        if (instance == null) {
            instance = new CardBinDb(context);
        }

        return instance;
    }


    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<CardBin, Integer> getCardBinDao() {
        if (cardBinDao == null) {
            cardBinDao = dbHelper.getRuntimeExceptionDao(CardBin.class);
        }
        return cardBinDao;
    }

    private RuntimeExceptionDao<CardBinBlack, Integer> getCardBinBlackDao() {
        if (cardBinBlackDao == null) {
            cardBinBlackDao = dbHelper.getRuntimeExceptionDao(CardBinBlack.class);
        }
        return cardBinBlackDao;
    }

    /***************************************Card Bin******************************************/
    /**
     * insert a card bin
     *
     * @return
     */
    public boolean insertCardBin(CardBin bin) {
        try {
            RuntimeExceptionDao<CardBin, Integer> dao = getCardBinDao();
            dao.create(bin);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * check if the card No is in the card bin table
     *
     * @param cardNo
     * @return
     */
    public boolean isInCardBinTable(String cardNo) {
        if (cardNo == null || cardNo.length() == 0)
            return false;
        try {
            RuntimeExceptionDao<CardBin, Integer> dao = getCardBinDao();
            GenericRawResults<String[]> rawResults = dao.queryRaw(
                    "select * from card_bin where ? like "
                            + CardBin.BIN_FIELD_NAME + "||'%'", cardNo);

            boolean isIn = rawResults.getResults().size() > 0;
            rawResults.close();
            return isIn;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * delete card bin
     */
    public boolean deleteCardBin() {
        try {
            RuntimeExceptionDao<CardBin, Integer> dao = getCardBinDao();
            dao.delete(dao.queryForAll());
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }


    /***************************************Black******************************************/
    /**
     * insert a black card bin
     *
     * @return
     */
    public boolean insertBlack(CardBinBlack black) {
        try {
            RuntimeExceptionDao<CardBinBlack, Integer> dao = getCardBinBlackDao();
            dao.create(black);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * check if the card is black
     *
     * @param cardNo
     * @return
     */
    public boolean isBlack(String cardNo) {
        if (cardNo == null || cardNo.length() == 0)
            return false;
        try {
            RuntimeExceptionDao<CardBinBlack, Integer> dao = getCardBinBlackDao();
            GenericRawResults<String[]> rawResults = dao.queryRaw(
                    "select * from card_bin_black where ? like "
                            + CardBin.BIN_FIELD_NAME + "||'%'", cardNo);

            boolean isBlack = rawResults.getResults().size() > 0;
            rawResults.close();
            return isBlack;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * delete all black record
     */
    public boolean deleteAllBlack() {
        try {
            RuntimeExceptionDao<CardBinBlack, Integer> dao = getCardBinBlackDao();
            dao.delete(dao.queryForAll());
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }
}
