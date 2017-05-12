/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-15
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.db;

import android.content.Context;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.pax.pay.base.Acquirer;
import com.pax.pay.trans.model.BaseTransData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

import java.sql.SQLException;
import java.util.List;

public class TransDataDb {

    private RuntimeExceptionDao<TransData, Integer> transDao = null;

    private static final String countSumRaw = "COUNT(*), SUM(" + TransData.AMOUNT_FIELD_NAME + ")";

    private TransDataDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static TransDataDb instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized TransDataDb getInstance(Context context) {
        if (instance == null) {
            instance = new TransDataDb(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<TransData, Integer> getTransDao() {
        if (transDao == null) {
            transDao = dbHelper.getRuntimeExceptionDao(TransData.class);
        }
        return transDao;
    }

    /***************************************Trans Data******************************************/
    /**
     * insert a transData
     *
     * @return
     */
    public boolean insertTransData(TransData transData) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            dao.create(transData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update a transData
     *
     * @return
     */
    public boolean updateTransData(TransData transData) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            dao.update(transData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find transData by id
     *
     * @param id
     * @return
     */
    public TransData findTransData(int id) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            return dao.queryForId(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find transData by trace No
     *
     * @param traceNo
     * @return
     */
    public TransData findTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(TransData.TRACENO_FIELD_NAME, traceNo)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 在MmotoTabBatch中使用auth code查找preAuth的交易
     *
     * @param authCode 授权码
     * @return
     */
    public TransData findTransDataByAuthCode(String authCode) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(TransData.AUTHCODE_FIELD_NAME, authCode);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public List<TransData> findTransData(List<ETransType> types, List<TransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(TransData.TYPE_FIELD_NAME, types)
                    .and().notIn(TransData.STATE_FIELD_NAME, statuses)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<TransData> findOfflineTransData(List<TransData.OfflineStatus> statuses) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(TransData.TYPE_FIELD_NAME, ETransType.OFFLINE_TRANS_SEND)
                    .and().in(BaseTransData.OFFLINE_STATE_FIELD_NAME, statuses)
                    .and().eq(BaseTransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    //AET-95
    public List<TransData> findTransData(List<ETransType> types, List<TransData.ETransStatus> statuses, Acquirer acq) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(TransData.TYPE_FIELD_NAME, types)
                    .and().notIn(TransData.STATE_FIELD_NAME, statuses)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL)
                    .and().eq(Acquirer.ID_FIELD_NAME, acq);
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find the last transData
     *
     * @return
     */
    public TransData findLastTransData() {
        try {
            List<TransData> list = findAllTransData();
            if (list != null && list.size() > 0) {
                return list.get(list.size() - 1);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find transData by trace No
     *
     * @return
     */
    public List<TransData> findAllTransData() {
        return findAllTransData(false);
    }

    /**
     * find transData by acquirer name
     *
     * @return
     */
    public List<TransData> findAllTransData(Acquirer acq) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            return dao.queryForEq(Acquirer.ID_FIELD_NAME, acq);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * find transData by trace No
     *
     * @return
     */
    private List<TransData> findAllTransData(boolean includeReversal) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            if (includeReversal)
                return dao.queryForAll();
            return dao.queryForEq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * delete transData by id
     */
    public boolean deleteTransData(int id) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            dao.deleteById(id);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete transData by trace no
     */
    public boolean deleteTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            DeleteBuilder<TransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(TransData.TRACENO_FIELD_NAME, traceNo);
            dao.delete(deleteBuilder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete transData by trace no
     */
    public boolean deleteTransDataByBatchNo(Acquirer acquirer, long batchNo) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            DeleteBuilder<TransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(TransData.BATCHNO_FIELD_NAME, batchNo).and().eq(Acquirer.ID_FIELD_NAME, acquirer);
            dao.delete(deleteBuilder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete transData by trace no
     */
    public boolean deleteAllTransData() {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            dao.delete(findAllTransData(true));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete transData by acquirer name
     */
    public boolean deleteAllTransData(Acquirer acqname) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            dao.delete(findAllTransData(acqname));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long countOf() {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            return dao.countOf();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long[] getRawResults(List<String[]> results) {
        long[] obj = new long[]{0, 0};
        if (results != null && results.size() > 0) {
            String[] value = results.get(0);
            obj[0] = value[0] == null ? 0 : Long.parseLong(value[0]);
            obj[1] = value[1] == null ? 0 : Long.parseLong(value[1]);
        }
        return obj;
    }

    /**
     * 读指定交易类型的交易记录
     *
     * @return
     */
    public long[] countSumOf(ETransType type) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TransData.TYPE_FIELD_NAME, type)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);

            GenericRawResults<String[]> rawResults = dao.queryRaw(queryBuilder.prepare().getStatement());
            return getRawResults(rawResults.getResults());
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{0, 0};
        }
    }

    /**
     * 读指定交易类型的交易记录
     *
     * @return
     */
    public long[] countSumOf(ETransType type, TransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TransData.TYPE_FIELD_NAME, type)
                    .and().eq(TransData.STATE_FIELD_NAME, status)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);

            GenericRawResults<String[]> rawResults = queryBuilder.queryRaw();
            return getRawResults(rawResults.getResults());
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{0, 0};
        }
    }

    /**
     * 读指定交易类型的交易记录
     *
     * @return
     */
    public long[] countSumOf(ETransType type, List<TransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TransData.TYPE_FIELD_NAME, type)
                    .and().in(TransData.STATE_FIELD_NAME, statuses)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);

            GenericRawResults<String[]> rawResults = dao.queryRaw(queryBuilder.prepare().getStatement());
            return getRawResults(rawResults.getResults());
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{0, 0};
        }
    }

    /**
     * 读指定交易类型的交易记录
     *
     * @return
     */
    public long[] countSumOf(Acquirer acquirer, ETransType type) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TransData.TYPE_FIELD_NAME, type)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);

            GenericRawResults<String[]> rawResults = queryBuilder.queryRaw();
            return getRawResults(rawResults.getResults());
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{0, 0};
        }
    }

    /**
     * 根据交易类型计算总计
     * <p>
     * 用于打单
     *
     * @param type     :交易类型
     * @param acquirer
     * @param status
     * @return obj[0] 笔数 obj[1] 金额
     */
    public long[] countSumOf(Acquirer acquirer, ETransType type, TransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TransData.TYPE_FIELD_NAME, type)
                    .and().eq(TransData.STATE_FIELD_NAME, status)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);

            GenericRawResults<String[]> rawResults = queryBuilder.queryRaw();
            return getRawResults(rawResults.getResults());
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{0, 0};
        }
    }

    /**
     * 根据交易类型计算总计
     * <p>
     * 用于打单
     *
     * @param type     :交易类型
     * @param acquirer
     * @param statuses
     * @return obj[0] 笔数 obj[1] 金额
     */
    public long[] countSumOf(Acquirer acquirer, ETransType type, List<TransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TransData.TYPE_FIELD_NAME, type)
                    .and().in(TransData.STATE_FIELD_NAME, statuses)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.NORMAL);

            GenericRawResults<String[]> rawResults = queryBuilder.queryRaw();
            return getRawResults(rawResults.getResults());
        } catch (SQLException e) {
            e.printStackTrace();
            return new long[]{0, 0};
        }
    }


    /**
     * 读冲正记录
     *
     * @return
     */
    public TransData findFirstDupRecord() {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            QueryBuilder<TransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.PENDING);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除交易记录
     */
    public boolean deleteDupRecord() {
        try {
            RuntimeExceptionDao<TransData, Integer> dao = getTransDao();
            DeleteBuilder<TransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(TransData.REVERSAL_FIELD_NAME, TransData.ReversalStatus.PENDING);
            deleteBuilder.delete();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
