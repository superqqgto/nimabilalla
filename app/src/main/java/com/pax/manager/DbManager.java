package com.pax.manager;

import android.content.Context;

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.db.*;

/**
 * Created by huangmuhua on 2017/3/27.
 */

public class DbManager {

    private TabBatchTransDao tabBatchTransDao;
    private MotoTabBatchTransDb motoTabBatchTransDao;
    private TransDataDb transDao;
    private MotoTransDataDb motoTransDao;
    private CardBinDb cardBinDao;
    private EmvDb emvDao;
    private AcqDb acqDao;
    private TornLogDb tornLogDao;
    private TransTotalDb transTotalDao;
    private DccTransDataDb dccTransDataDao;

    //加载外部类DbManager时才会加载内部类,比较安全的单例模式
    private static class LazyHolder {
        private static final DbManager INSTANCE = new DbManager();
    }

    private DbManager() {
        init();
    }

    private static DbManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private void init() {
        Context context = FinancialApplication.mApp;
        tabBatchTransDao = TabBatchTransDao.getInstance(context);
        motoTabBatchTransDao = MotoTabBatchTransDb.getInstance(context);
        transDao = TransDataDb.getInstance(context);
        motoTransDao = MotoTransDataDb.getInstance(context);
        cardBinDao = CardBinDb.getInstance(context);
        emvDao = EmvDb.getInstance(context);
        acqDao = AcqDb.getInstance(context);
        tornLogDao = TornLogDb.getInstance(context);
        transTotalDao = TransTotalDb.getInstance(context);
        dccTransDataDao=DccTransDataDb.getInstance(context);
    }

    /**
     * Tab Batch的DAO
     *
     * @return
     */
    public static TabBatchTransDao getTabBatchTransDao() {
        return getInstance().tabBatchTransDao;
    }

    /**
     * Moto Tab Batch的DAO
     *
     * @return
     */
    public static MotoTabBatchTransDb getMotoTabBatchTransDao() {
        return getInstance().motoTabBatchTransDao;
    }

    /**
     * 普通Terminal Capture Batch的DAO
     *
     * @return
     */
    public static TransDataDb getTransDao() {
        return getInstance().transDao;
    }

    /**
     * moto Batch的DAO
     *
     * @return
     */
    public static MotoTransDataDb getMotoTransDao() {
        return getInstance().motoTransDao;
    }


    /**
     * 获取Card Bin的DAO对象后可对Card Bin进行数据库操作
     *
     * @return
     */
    public static CardBinDb getCardBinDao() {
        return getInstance().cardBinDao;
    }

    /**
     * 获取后可进行EMV相关的数据库操作
     *
     * @return
     */
    public static EmvDb getEmvDao() {
        return getInstance().emvDao;
    }

    public static AcqDb getAcqDao() {
        return getInstance().acqDao;
    }

    public static TornLogDb getTornLogDao() {
        return getInstance().tornLogDao;
    }

    public static TransTotalDb getTransTotalDao() {
        return getInstance().transTotalDao;
    }

    /**
     * 获取后可进行Dcc相关的数据库操作
     *
     * @return
     */
    public static DccTransDataDb getDccTransDataDao() {
        return getInstance().dccTransDataDao;
    }
}
