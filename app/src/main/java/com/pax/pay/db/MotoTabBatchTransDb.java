package com.pax.pay.db;

import android.content.Context;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.pax.pay.base.Acquirer;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.MotoTabBatchTransData;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by zhouhong on 2017/5/5.
 */

public class MotoTabBatchTransDb {

    private RuntimeExceptionDao<MotoTabBatchTransData, Integer> transDao = null;

    private static final String countSumRaw = "COUNT(*), SUM(" + MotoTabBatchTransData.AMOUNT_FIELD_NAME + ")";

    private MotoTabBatchTransDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static MotoTabBatchTransDb instance;


    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized MotoTabBatchTransDb getInstance(Context context) {
        if (instance == null) {
            instance = new MotoTabBatchTransDb(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<MotoTabBatchTransData, Integer> getTransDao() {
        if (transDao == null) {
            transDao = dbHelper.getRuntimeExceptionDao(MotoTabBatchTransData.class);
        }
        return transDao;
    }

    /***************************************Trans Data******************************************/
    /**
     * insert a TransData
     *
     * @return
     */
    public boolean insertTransData(MotoTabBatchTransData motoTabBatchTransData) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            dao.create(motoTabBatchTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update a TransData
     *
     * @return
     */
    public boolean updateTransData(MotoTabBatchTransData motoTabBatchTransData) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            dao.update(motoTabBatchTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find TransData by id
     *
     * @param id
     * @return
     */
    public MotoTabBatchTransData findTransData(int id) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            return dao.queryForId(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find TransData by trace No
     *
     * @param traceNo
     * @return
     */
    public MotoTabBatchTransData findTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(MotoTabBatchTransData.TRACENO_FIELD_NAME, traceNo)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);
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
    public MotoTabBatchTransData findTransDataByAuthCode(String authCode) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(MotoTabBatchTransData.AUTHCODE_FIELD_NAME, authCode);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<MotoTabBatchTransData> findTransData(List<ETransType> types, List<MotoTabBatchTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(MotoTabBatchTransData.TYPE_FIELD_NAME, types)
                    .and().notIn(MotoTabBatchTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find the last MotoTabBatchTransData
     *
     * @return
     */
    public MotoTabBatchTransData findTransData() {
        try {
            List<MotoTabBatchTransData> list = findAllTransData();
            if (list != null && list.size() > 0) {
                return list.get(list.size() - 1);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find TabBatchTransData by trace No
     *
     * @return
     */
    public List<MotoTabBatchTransData> findAllTransData() {
        return findAllTransData(false);
    }

    /**
     * find TabBatchTransData by acquirer name
     *
     * @return
     */
    public List<MotoTabBatchTransData> findAllTransData(Acquirer acq) {
        try{
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            return dao.queryForEq(Acquirer.ID_FIELD_NAME, acq);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * find TabBatchTransData by trace No
     *
     * @return
     */
    private List<MotoTabBatchTransData> findAllTransData(boolean includeReversal) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            if (includeReversal)
                return dao.queryForAll();
            return dao.queryForEq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * delete TransData by trace no
     */
    public boolean deleteTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            DeleteBuilder<MotoTabBatchTransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(MotoTabBatchTransData.TRACENO_FIELD_NAME, traceNo);
            dao.delete(deleteBuilder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete TransData by trace no
     */
    public boolean deleteAllTransData() {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            dao.delete(findAllTransData(true));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete TransData by acquirer name
     */
    public boolean deleteAllTransData(Acquirer acqname) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            dao.delete(findAllTransData(acqname));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long countOf() {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
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
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(ETransType type, MotoTabBatchTransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(MotoTabBatchTransData.STATE_FIELD_NAME, status)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(ETransType type, List<MotoTabBatchTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().in(MotoTabBatchTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);

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
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(Acquirer acquirer, ETransType type, MotoTabBatchTransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(MotoTabBatchTransData.STATE_FIELD_NAME, status)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(Acquirer acquirer, ETransType type, List<MotoTabBatchTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(MotoTabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().in(MotoTabBatchTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.NORMAL);

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
    public MotoTabBatchTransData findFirstDupRecord() {
        try {
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<MotoTabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.PENDING);
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
            RuntimeExceptionDao<MotoTabBatchTransData, Integer> dao = getTransDao();
            DeleteBuilder<MotoTabBatchTransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(MotoTabBatchTransData.REVERSAL_FIELD_NAME, MotoTabBatchTransData.ReversalStatus.PENDING);
            deleteBuilder.delete();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
