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
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.pax.pay.base.Acquirer;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTransData;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by chenzaoyang on 2017/5/5.
 */

public class MotoTransDataDb {

    private RuntimeExceptionDao<MotoTransData, Integer> transDao = null;

    private static final String countSumRaw = "COUNT(*), SUM(" + MotoTransData.AMOUNT_FIELD_NAME + ")";

    private MotoTransDataDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static MotoTransDataDb instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized MotoTransDataDb getInstance(Context context) {
        if (instance == null) {
            instance = new MotoTransDataDb(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<MotoTransData, Integer> getTransDao() {
        if (transDao == null) {
            transDao = dbHelper.getRuntimeExceptionDao(MotoTransData.class);
        }
        return transDao;
    }

    /***************************************Trans Data******************************************/
    /**
     * insert a MotoTransData
     *
     * @return
     */
    public boolean insertMotoTransData(MotoTransData motoTransData) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            dao.create(motoTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update a MotoTransData
     *
     * @return
     */
    public boolean updateMotoTransData(MotoTransData motoTransData) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            dao.update(motoTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find MotoTransData by id
     *
     * @param id
     * @return
     */
    public MotoTransData findMotoTransData(int id) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            return dao.queryForId(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find MotoTransData by trace No
     *
     * @param traceNo
     * @return
     */
    public MotoTransData findMotoTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(MotoTransData.TRACENO_FIELD_NAME, traceNo)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<MotoTransData> findMotoTransData(List<ETransType> types, List<MotoTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(MotoTransData.TYPE_FIELD_NAME, types)
                    .and().notIn(MotoTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * find the last MotoTransData
     *
     * @return
     */
    public MotoTransData findLastMotoTransData() {
        try {
            List<MotoTransData> list = findAllMotoTransData();
            if (list != null && list.size() > 0) {
                return list.get(list.size() - 1);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find MotoTransData by trace No
     *
     * @return
     */
    public List<MotoTransData> findAllMotoTransData() {
        return findAllMotoTransData(false);
    }

    /**
     * find MotoTransData by acquirer name
     *
     * @return
     */
    public List<MotoTransData> findAllMotoTransData(Acquirer acq) {
        try{
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            return dao.queryForEq(Acquirer.ID_FIELD_NAME, acq);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * find MotoTransData by trace No
     *
     * @return
     */
    private List<MotoTransData> findAllMotoTransData(boolean includeReversal) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            if (includeReversal)
                return dao.queryForAll();
            return dao.queryForEq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * delete MotoTransData by id
     */
    public boolean deleteMotoTransData(int id) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            dao.deleteById(id);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete MotoTransData by trace no
     */
    public boolean deleteMotoTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            DeleteBuilder<MotoTransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(MotoTransData.TRACENO_FIELD_NAME, traceNo);
            dao.delete(deleteBuilder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete MotoTransData by trace no
     */
    public boolean deleteAllMotoTransData() {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            dao.delete(findAllMotoTransData(true));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete MotoTransData by acquirer name
     */
    public boolean deleteAllMotoTransData(Acquirer acqname) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            dao.delete(findAllMotoTransData(acqname));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long countOf() {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
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
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTransData.TYPE_FIELD_NAME, type)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(ETransType type, MotoTransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTransData.TYPE_FIELD_NAME, type)
                    .and().eq(MotoTransData.STATE_FIELD_NAME, status)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(ETransType type, List<MotoTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTransData.TYPE_FIELD_NAME, type)
                    .and().in(MotoTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);

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
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTransData.TYPE_FIELD_NAME, type)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(Acquirer acquirer, ETransType type, MotoTransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTransData.TYPE_FIELD_NAME, type)
                    .and().eq(MotoTransData.STATE_FIELD_NAME, status)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(Acquirer acquirer, ETransType type, List<MotoTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTransData.TYPE_FIELD_NAME, type)
                    .and().in(MotoTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.NORMAL);

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
    public MotoTransData findFirstDupRecord() {
        try {
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.PENDING);
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
            RuntimeExceptionDao<MotoTransData, Integer> dao = getTransDao();
            DeleteBuilder<MotoTransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(MotoTransData.REVERSAL_FIELD_NAME, MotoTransData.ReversalStatus.PENDING);
            deleteBuilder.delete();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
