package com.pax.pay.db;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.pax.pay.base.DccTransData;


/**
 * Created by wangyq on 2017/5/5.
 */

public class DccTransDataDb {

    private RuntimeExceptionDao<DccTransData, Integer> dccDao = null;

    private DccTransDataDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }
    private final BaseDbHelper dbHelper;

    private static DccTransDataDb instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized DccTransDataDb getInstance(Context context) {
        if (instance == null) {
            instance = new DccTransDataDb(context);
        }

        return instance;
    }
    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<DccTransData, Integer> getDccTransDataDao() {
        if (dccDao == null) {
            dccDao = dbHelper.getRuntimeExceptionDao(DccTransData.class);
        }
        return dccDao;
    }

    /***************************************Trans Data******************************************/
    /**
     * insert a dccTransData
     *
     * @return
     */
    public boolean insertDccTransData(DccTransData dccTransData) {
        try {
            RuntimeExceptionDao<DccTransData, Integer> dao = getDccTransDataDao();
            dao.create(dccTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update a dccTransData
     *
     * @return
     */
    public boolean updateDccTransData(DccTransData dccTransData) {
        try {
            RuntimeExceptionDao<DccTransData, Integer> dao = getDccTransDataDao();
            dao.update(dccTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * delete dccTransData
     */
    public boolean deleteAllDccTransData() {
        //TODO
        return false;
    }

}
