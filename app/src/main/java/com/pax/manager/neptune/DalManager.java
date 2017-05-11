package com.pax.manager.neptune;

import com.pax.dal.*;
import com.pax.dal.entity.*;
import com.pax.neptunelite.api.DALProxyClient;
import com.pax.pay.app.FinancialApplication;

/**
 * Created by huangmuhua on 2017/4/19.
 */

public class DalManager {
    private IDAL dal;

    private static class LazyHolder {
        private static final DalManager INSTANCE = new DalManager();
    }

    private DalManager() {
        init();
    }

    private static DalManager getInstance() {
        return LazyHolder.INSTANCE;
    }


    private void init() {
        try {
            dal = DALProxyClient.getInstance().getDal(FinancialApplication.mApp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static IDAL getDal() {
        return getInstance().dal;
    }

    public static IMag getMag() {
        return getDal().getMag();
    }

    public static IIcc getIcc() {
        return getDal().getIcc();
    }

    public static IPrinter getPrinter() {
        return getDal().getPrinter();
    }

    public static ISys getSys() {
        return getDal().getSys();
    }

    public static IKeyBoard getKeyBoard() {
        return getDal().getKeyBoard();
    }

    public static IDalCommManager getCommManager() {
        return getDal().getCommManager();
    }

    public static ISignPad getSignPad() {
        return getDal().getSignPad();
    }

    public static ICardReaderHelper getCardReaderHelper() {
        return getDal().getCardReaderHelper();
    }

    public static IPicc getPiccInternal() {
        return getDal().getPicc(EPiccType.INTERNAL);
    }

    public static IPicc getPiccExternal() {
        return getDal().getPicc(EPiccType.EXTERNAL);
    }

    public static IPed getPedInternal() {
        return getDal().getPed(EPedType.INTERNAL);
    }

    public static IPed getPedExternalA() {
        return getDal().getPed(EPedType.EXTERNAL_TYPEA);
    }

    public static IPed getPedExternalB() {
        return getDal().getPed(EPedType.EXTERNAL_TYPEB);
    }

    public static IPed getPedExternalC() {
        return getDal().getPed(EPedType.EXTERNAL_TYPEC);
    }

    public static IScanner getScannerFront() {
        return getDal().getScanner(EScannerType.FRONT);
    }

    public static IScanner getScannerRear() {
        return getDal().getScanner(EScannerType.REAR);
    }

    public static IScanner getScannerLeft() {
        return getDal().getScanner(EScannerType.LEFT);
    }

    public static IScanner getScannerRight() {
        return getDal().getScanner(EScannerType.RIGHT);
    }

    public static IScanner getScannerExternal() {
        return getDal().getScanner(EScannerType.EXTERNAL);
    }

    public static ISle4442 getISle4442() {
        return getDal().getISle4442();
    }

    public static IPuk getIPuk() {
        return getDal().getIPuk();
    }
}
