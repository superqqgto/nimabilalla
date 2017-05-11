/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-3-23
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.db;

import android.content.Context;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.pax.manager.DbManager;
import com.pax.pay.base.Acquirer;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransTotal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.pax.pay.trans.model.BaseTransData.ETransStatus.*;

public class TransTotalDb {

    private RuntimeExceptionDao<TransTotal, Integer> transDao = null;

    private TransTotalDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static TransTotalDb instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized TransTotalDb getInstance(Context context) {
        if (instance == null) {
            instance = new TransTotalDb(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<TransTotal, Integer> getTotalDao() {
        if (transDao == null) {
            transDao = dbHelper.getRuntimeExceptionDao(TransTotal.class);
        }
        return transDao;
    }

    /***************************************Trans Data******************************************/
    /**
     * insert a transTotal
     *
     * @return
     */
    public boolean insertTransTotal(TransTotal transTotal) {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            dao.create(transTotal);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update a transTotal
     *
     * @return
     */
    public boolean updateTransTotal(TransTotal transTotal) {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            dao.update(transTotal);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find transTotal by id
     *
     * @param id
     * @return
     */
    public TransTotal findTransTotal(int id) {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            return dao.queryForId(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find transTotal by batch No
     *
     * @param acquirer
     * @param batchNo
     * @return
     */
    public TransTotal findTransTotalByBatchNo(Acquirer acquirer, long batchNo) {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            QueryBuilder<TransTotal, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(TransTotal.BATCHNO_FIELD_NAME, batchNo)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * find the last transTotal
     *
     * @return
     */
    public TransTotal findLastTransTotal(Acquirer acquirer, boolean isClosed) {
        try {
            List<TransTotal> list = findAllTransTotal(acquirer, isClosed);
            if (list != null && list.size() > 0) {
                return list.get(list.size() - 1);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find transTotal by batch No
     *
     * @return
     */
    public List<TransTotal> findAllTransTotal(Acquirer acquirer, boolean isClosed) {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            QueryBuilder<TransTotal, Integer> queryBuilder = dao.queryBuilder();
            Where<TransTotal, Integer> where = queryBuilder.where().eq(TransTotal.IS_CLOSED_FIELD_NAME, isClosed);
            if (acquirer != null)
                where.and().eq(Acquirer.ID_FIELD_NAME, acquirer);
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * delete TransTotal by id
     */
    public boolean deleteTransTotal(int id) {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            dao.deleteById(id);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete transTotal by batch no
     */
    public boolean deleteTransTotalByBatchNo(Acquirer acquirer, long batchNo) {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            DeleteBuilder<TransTotal, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(TransTotal.BATCHNO_FIELD_NAME, batchNo)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer);
            dao.delete(deleteBuilder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete all transTotal
     */
    public boolean deleteAllTransTotal() {
        try {
            RuntimeExceptionDao<TransTotal, Integer> dao = getTotalDao();
            List<TransTotal> list = dao.queryForAll();
            dao.delete(list);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static final List<TransData.ETransStatus> filter = new ArrayList<TransData.ETransStatus>() {{
        add(NORMAL);
        add(ADJUSTED);
    }};

    public TransTotal CalcTotal(TransTotal total) {
        if (total == null) {
            total = new TransTotal();
        }
        // 消费
        long[] obj = DbManager.getTransDao().countSumOf(ETransType.SALE, filter);
        total.setSaleTotalNum(obj[0]);
        total.setSaleTotalAmt(obj[1]);

        // 撤销
        obj = DbManager.getTransDao().countSumOf(ETransType.VOID, NORMAL);
        total.setVoidTotalNum(obj[0]);
        total.setVoidTotalAmt(obj[1]);
        // 退货
        obj = DbManager.getTransDao().countSumOf(ETransType.REFUND, NORMAL);
        total.setRefundTotalNum(obj[0]);
        total.setRefundTotalAmt(obj[1]);
        //sale void total
        obj = DbManager.getTransDao().countSumOf(ETransType.SALE, VOIDED);
        total.setSaleVoidTotalNum(obj[0]);
        total.setSaleVoidTotalAmt(obj[1]);
        //refund void total
        obj = DbManager.getTransDao().countSumOf(ETransType.REFUND, VOIDED);
        total.setRefundVoidTotalNum(obj[0]);
        total.setRefundVoidTotalAmt(obj[1]);
        // 预授权
        obj = DbManager.getTransDao().countSumOf(ETransType.PREAUTH, NORMAL);
        total.setAuthTotalNum(obj[0]);
        total.setAuthTotalAmt(obj[1]);

        // 脱机 AET-75
        obj = DbManager.getTransDao().countSumOf(ETransType.OFFLINE_TRANS_SEND, filter);
        total.setOfflineTotalNum(obj[0]);
        total.setOfflineTotalAmt(obj[1]);

        return total;
    }

    public TransTotal CalcTotal(Acquirer acquirer, TransTotal total) {
        if (total == null) {
            total = new TransTotal();
        }
        // 消费
        long[] obj = DbManager.getTransDao().countSumOf(acquirer, ETransType.SALE, filter);
        total.setSaleTotalNum(obj[0]);
        total.setSaleTotalAmt(obj[1]);

        // 撤销
        obj = DbManager.getTransDao().countSumOf(acquirer, ETransType.VOID, NORMAL);
        total.setVoidTotalNum(obj[0]);
        total.setVoidTotalAmt(obj[1]);

        // 退货
        obj = DbManager.getTransDao().countSumOf(acquirer, ETransType.REFUND, NORMAL);
        total.setRefundTotalNum(obj[0]);
        total.setRefundTotalAmt(obj[1]);

        //sale void total
        obj = DbManager.getTransDao().countSumOf(acquirer, ETransType.SALE, VOIDED);
        total.setSaleVoidTotalNum(obj[0]);
        total.setSaleVoidTotalAmt(obj[1]);
        //refund void total
        obj = DbManager.getTransDao().countSumOf(acquirer, ETransType.REFUND, VOIDED);
        total.setRefundVoidTotalNum(obj[0]);
        total.setRefundVoidTotalAmt(obj[1]);

        // 预授权
        obj = DbManager.getTransDao().countSumOf(acquirer, ETransType.PREAUTH, NORMAL);
        total.setAuthTotalNum(obj[0]);
        total.setAuthTotalAmt(obj[1]);

        // 脱机 AET-75
        obj = DbManager.getTransDao().countSumOf(acquirer, ETransType.OFFLINE_TRANS_SEND, filter);
        total.setOfflineTotalNum(obj[0]);
        total.setOfflineTotalAmt(obj[1]);

        return total;
    }

}
