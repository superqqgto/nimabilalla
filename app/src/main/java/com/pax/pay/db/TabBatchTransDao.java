package com.pax.pay.db;

import android.content.Context;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.pax.pay.base.Acquirer;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TabBatchTransData;

import java.sql.SQLException;
import java.util.List;

/**
 * created by Muhua Huang 2775
 */
public class TabBatchTransDao {

    private RuntimeExceptionDao<TabBatchTransData, Integer> transDao = null;

    private static final String countSumRaw = "COUNT(*), SUM(" + TabBatchTransData.AMOUNT_FIELD_NAME + ")";

    private TabBatchTransDao(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static TabBatchTransDao instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized TabBatchTransDao getInstance(Context context) {
        if (instance == null) {
            instance = new TabBatchTransDao(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<TabBatchTransData, Integer> getTransDao() {
        if (transDao == null) {
            transDao = dbHelper.getRuntimeExceptionDao(TabBatchTransData.class);
        }
        return transDao;
    }

    /***************************************Trans Data******************************************/
    /**
     * insert a TabBatchTransData
     *
     * @return
     */
    public boolean insertTabBatchTransData(TabBatchTransData tabBatchTransData) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            dao.create(tabBatchTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update a TabBatchTransData
     *
     * @return
     */
    public boolean updateTabBatchTransData(TabBatchTransData TabBatchTransData) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            dao.update(TabBatchTransData);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find TabBatchTransData by id
     *
     * @param id
     * @return
     */
    public TabBatchTransData findTabBatchTransData(int id) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            return dao.queryForId(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * find TabBatchTransData by trace No
     *
     * @param traceNo
     * @return
     */
    public TabBatchTransData findTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(TabBatchTransData.TRACENO_FIELD_NAME, traceNo)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 在Tab Batch中使用auth code查找preAuth的交易
     *
     * @param authCode 授权码
     * @return
     */
    public TabBatchTransData findTransDataByAuthCode(String authCode) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(TabBatchTransData.AUTHCODE_FIELD_NAME, authCode);
            return queryBuilder.queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<TabBatchTransData> findTabBatchTransData(List<ETransType> types, List<TabBatchTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(TabBatchTransData.TYPE_FIELD_NAME, types)
                    .and().notIn(TabBatchTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);
            return queryBuilder.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * find the last TabBatchTransData
     *
     * @return
     */
    public TabBatchTransData findLastTabBatchTransData() {
        try {
            List<TabBatchTransData> list = findAllTabBatchTransData();
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
    public List<TabBatchTransData> findAllTabBatchTransData() {
        return findAllTabBatchTransData(false);
    }

    /**
     * find TabBatchTransData by acquirer name
     *
     * @return
     */
    public List<TabBatchTransData> findAllTabBatchTransData(Acquirer acq) {
        try{
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
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
    private List<TabBatchTransData> findAllTabBatchTransData(boolean includeReversal) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            if (includeReversal)
                return dao.queryForAll();
            return dao.queryForEq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * delete TabBatchTransData by id
     */
    public boolean deleteTabBatchTransData(int id) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            dao.deleteById(id);
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete TabBatchTransData by trace no
     */
    public boolean deleteTabBatchTransDataByTraceNo(long traceNo) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            DeleteBuilder<TabBatchTransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(TabBatchTransData.TRACENO_FIELD_NAME, traceNo);
            dao.delete(deleteBuilder.prepare());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete TabBatchTransData by trace no
     */
    public boolean deleteAllTabBatchTransData() {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            dao.delete(findAllTabBatchTransData(true));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * delete TabBatchTransData by acquirer name
     */
    public boolean deleteAllTabBatchTransData(Acquirer acqname) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            dao.delete(findAllTabBatchTransData(acqname));
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long countOf() {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
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
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(ETransType type, TabBatchTransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(TabBatchTransData.STATE_FIELD_NAME, status)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(ETransType type, List<TabBatchTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().in(TabBatchTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);

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
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(Acquirer acquirer, ETransType type, TabBatchTransData.ETransStatus status) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().eq(TabBatchTransData.STATE_FIELD_NAME, status)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);

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
    public long[] countSumOf(Acquirer acquirer, ETransType type, List<TabBatchTransData.ETransStatus> statuses) {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder =
                    dao.queryBuilder().selectRaw(countSumRaw);
            queryBuilder.where().eq(TabBatchTransData.TYPE_FIELD_NAME, type)
                    .and().in(TabBatchTransData.STATE_FIELD_NAME, statuses)
                    .and().eq(Acquirer.ID_FIELD_NAME, acquirer)
                    .and().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.NORMAL);

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
    public TabBatchTransData findFirstDupRecord() {
        try {
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            QueryBuilder<TabBatchTransData, Integer> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.PENDING);
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
            RuntimeExceptionDao<TabBatchTransData, Integer> dao = getTransDao();
            DeleteBuilder<TabBatchTransData, Integer> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(TabBatchTransData.REVERSAL_FIELD_NAME, TabBatchTransData.ReversalStatus.PENDING);
            deleteBuilder.delete();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
