/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-7
 * Module Author: laiyi
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.db;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.pax.pay.clss.ClssTornLog;

import java.util.List;

public class TornLogDb {
    private RuntimeExceptionDao<ClssTornLog, Integer> tornLogDao = null;
    private final BaseDbHelper dbHelper;
    private static TornLogDb instance;

    private TornLogDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized TornLogDb getInstance(Context context) {
        if (instance == null) {
            instance = new TornLogDb(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<ClssTornLog, Integer> getTornLogDao() {
        if (tornLogDao == null) {
            tornLogDao = dbHelper.getRuntimeExceptionDao(ClssTornLog.class);
        }
        return tornLogDao;
    }

    /***************************************Torn Data******************************************/
    /**
     * insert a tornLog
     *
     * @return
     */
    public boolean insertTornLog(List<ClssTornLog> tornLog) {
        try {
            RuntimeExceptionDao<ClssTornLog, Integer> dao = getTornLogDao();
            dao.create(tornLog);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find tornLog
     *
     * @return
     */
    public List<ClssTornLog> findAllTornLog() {
        try {
            RuntimeExceptionDao<ClssTornLog, Integer> dao = getTornLogDao();
            return dao.queryForAll();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * delete clssTornLog
     */
    public boolean deleteAllTornLog() {
        try {
            RuntimeExceptionDao<ClssTornLog, Integer> dao = getTornLogDao();
            dao.delete(findAllTornLog());
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }
}
