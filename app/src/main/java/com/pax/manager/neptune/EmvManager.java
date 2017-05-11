package com.pax.manager.neptune;

import com.pax.eemv.*;
import com.pax.eemv.entity.*;
import com.pax.eemv.enums.*;
import com.pax.eemv.exception.EmvException;
import com.pax.pay.app.FinancialApplication;

import java.util.List;

/**
 * Created by huangmuhua on 2017/4/19.
 */

public class EmvManager {

    private IEmv emv;

    private static class LazyHolder {
        private static final EmvManager INSTANCE = new EmvManager();
    }

    private EmvManager() {
        init();
    }

    private static EmvManager getInstance() {
        return LazyHolder.INSTANCE;
    }


    private void init() {
        emv = EmvImpl.getInstance(FinancialApplication.mApp).getEmv();
    }

    private static IEmv getEmv() {
        return getInstance().emv;
    }

    public static int emvInit() {
        try {
            return getEmv().emvInit();
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void setConfig(Config config) {
        try {
            getEmv().setConfig(config);
        } catch (EmvException e) {
            e.printStackTrace();
        }
    }

    public static Config getConfig() {
        try {
            return getEmv().getConfig();
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getTlv(int i) {
        return getEmv().getTlv(i);
    }

    public static void setTlv(int i, byte[] bytes) {
        try {
            getEmv().setTlv(i, bytes);
        } catch (EmvException e) {
            e.printStackTrace();
        }
    }

    public static void emvBegin(InputParam inputParam) {
        try {
            getEmv().emvBegin(inputParam);
        } catch (EmvException e) {
            e.printStackTrace();
        }
    }

    public static EACType emvContinue() {
        try {
            return getEmv().emvContinue();
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ETransResult emvComplete(EOnlineResult onlineResult) {
        try {
            return getEmv().emvComplete(onlineResult);
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void contactlessPreProc(InputParam inputParam) {
        try {
            getEmv().contactlessPreProc(inputParam);
        } catch (EmvException e) {
            e.printStackTrace();
        }
    }

    public static ETransResult contactlessBegin() {
        try {
            return getEmv().contactlessBegin();
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void contactlessComplete(byte[] bytes1, byte[] bytes2) {
        try {
            getEmv().contactlessComplete(bytes1, bytes2);
        } catch (EmvException e) {
            e.printStackTrace();
        }
    }

    public static List<byte[]> readAllLogRecord(EChannelType channelType, ELogType logType) {
        try {
            return getEmv().readAllLogRecord(channelType, logType);
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long readEcBalance(EChannelType channelType) {
        try {
            return getEmv().readEcBalance(channelType);
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static ETransResult emvProcess(InputParam inputParam) throws EmvException {
//        try {
        return getEmv().emvProcess(inputParam);
//        } catch (EmvException e) {
//            e.printStackTrace();
//        }
//        return null;
    }

    public static void setListener(IEmvListener emvListener) {
        getEmv().setListener(emvListener);
    }

    public static void setCapkList(List<Capk> capkList) {
        getEmv().setCapkList(capkList);
    }

    public static void setAidParamList(List<AidParam> aidParamList) {
        getEmv().setAidParamList(aidParamList);
    }

    public static byte[] getDataCommand(int i) {
        try {
            return getEmv().getDataCommand(i);
        } catch (EmvException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setDeviceListener(IEmvDeviceListener emvDeviceListener) {
        getEmv().setDeviceListener(emvDeviceListener);
    }

    public static void setContactlessListener(IEmvContactlessListener emvContactlessListener) {
        getEmv().setContactlessListener(emvContactlessListener);
    }

    public static int contactlessParameterSetPBOC(InputPBOCParam inputPBOCParam) {
        return getEmv().contactlessParameterSetPBOC(inputPBOCParam);
    }

    public static int contactlessParameterSetWave(InputPayWaveParam inputPayWaveParam) {
        return getEmv().contactlessParameterSetWave(inputPayWaveParam);
    }

    public static int contactlessParameterSetPass(InputPayPassParam inputPayPassParam) {
        return getEmv().contactlessParameterSetPass(inputPayPassParam);
    }

    public static ReaderParam getReaderParam(EKernelType kernelType) {
        return getEmv().getReaderParam(kernelType);
    }

    public static int setReaderParam(EKernelType kernelType, ReaderParam readerParam) {
        return getEmv().setReaderParam(kernelType, readerParam);
    }

    public static byte[] getKernelTLV(EKernelType kernelType, int var2) {
        return getEmv().getKernelTLV(kernelType, var2);
    }

    public static int setKernelTLV(EKernelType kernelType, int var2, byte[] bytes) {
        return getEmv().setKernelTLV(kernelType, var2, bytes);
    }

    public static String getVersion() {
        return getEmv().getVersion();
    }

    public static List<Balance> readEcCurrencyBalance(EChannelType channelType) {
        return getEmv().readEcCurrencyBalance(channelType);
    }
}
