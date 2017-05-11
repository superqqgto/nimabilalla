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

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.pax.pay.emv.EmvAid;
import com.pax.pay.emv.EmvCapk;

import java.util.List;

public class EmvDb {

    private RuntimeExceptionDao<EmvAid, Integer> aidDao = null;
    private RuntimeExceptionDao<EmvCapk, Integer> capkDao = null;

    private EmvDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static EmvDb instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized EmvDb getInstance(Context context) {
        if (instance == null) {
            instance = new EmvDb(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<EmvAid, Integer> getAidDao() {
        if (aidDao == null) {
            aidDao = dbHelper.getRuntimeExceptionDao(EmvAid.class);
        }
        return aidDao;
    }

    private RuntimeExceptionDao<EmvCapk, Integer> getCapkDao() {
        if (capkDao == null) {
            capkDao = dbHelper.getRuntimeExceptionDao(EmvCapk.class);
        }
        return capkDao;
    }

    /***************************************AID******************************************/
    /**
     * insert an aid
     *
     * @return
     */
    public boolean insertAID(EmvAid aid) {
        try {
            RuntimeExceptionDao<EmvAid, Integer> dao = getAidDao();
            dao.create(aid);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update an aid
     *
     * @return
     */
    public boolean updateAID(EmvAid aid) {
        try {
            RuntimeExceptionDao<EmvAid, Integer> dao = getAidDao();
            dao.update(aid);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find aid by aid string
     *
     * @param aid
     * @return
     */
    public EmvAid findAID(String aid) {
        if (aid == null || aid.length() == 0)
            return null;
        try {
            RuntimeExceptionDao<EmvAid, Integer> dao = getAidDao();
            List<EmvAid> list = dao.queryForEq(EmvAid.AID_FIELD_NAME, aid);

            if (list != null && list.size() > 0)
                return list.get(0);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find all aid
     *
     * @return
     */
    public List<EmvAid> findAllAID() {
        RuntimeExceptionDao<EmvAid, Integer> dao = getAidDao();
        return dao.queryForAll();
    }

    /**
     * 删除bin
     */
    public boolean deleteAID(int id) {
        try {
            RuntimeExceptionDao<EmvAid, Integer> dao = getAidDao();
            dao.deleteById(id);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }


    /***************************************CAPK******************************************/
    /**
     * insert a CAPK
     *
     * @return
     */
    public boolean insertCAPK(EmvCapk capk) {
        try {
            RuntimeExceptionDao<EmvCapk, Integer> dao = getCapkDao();
            dao.create(capk);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update a CAPK
     *
     * @return
     */
    public boolean updateCAPK(EmvCapk capk) {
        try {
            RuntimeExceptionDao<EmvCapk, Integer> dao = getCapkDao();
            dao.update(capk);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find CAPK by rid string
     *
     * @param rid
     * @return
     */
    public EmvCapk findCAPK(String rid) {
        if (rid == null || rid.length() == 0)
            return null;
        try {
            RuntimeExceptionDao<EmvCapk, Integer> dao = getCapkDao();
            List<EmvCapk> list = dao.queryForEq(EmvCapk.RID_FIELD_NAME, rid);

            if (list != null && list.size() > 0)
                return list.get(0);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find all CAPK
     *
     * @return
     */
    public List<EmvCapk> findAllCAPK() {
        RuntimeExceptionDao<EmvCapk, Integer> dao = getCapkDao();
        return dao.queryForAll();
    }

    /**
     * delete CAPK
     */
    public boolean deleteCAPK(int id) {
        try {
            RuntimeExceptionDao<EmvCapk, Integer> dao = getCapkDao();
            dao.deleteById(id);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }
}
