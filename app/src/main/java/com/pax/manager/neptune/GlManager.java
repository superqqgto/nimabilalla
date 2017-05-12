package com.pax.manager.neptune;

import com.pax.gl.*;
import com.pax.gl.algo.IAlgo;
import com.pax.gl.comm.ICommHelper;
import com.pax.gl.compress.ICompress;
import com.pax.gl.convert.IConvert;
import com.pax.gl.db.IDb;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.impl.*;
import com.pax.gl.lbs.ILbs;
import com.pax.gl.packer.IPacker;
import com.pax.gl.utils.IUtils;
import com.pax.pay.app.FinancialApplication;

/**
 * Created by huangmuhua on 2017/4/19.
 */

public class GlManager {
    private IGL gl;
    private IConvert convert;
    private IPacker packer;
    private IDb db;

    private static class LazyHolder {
        private static final GlManager INSTANCE = new GlManager();
    }

    private GlManager() {
        init();
    }

    public static GlManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private void init() {
        gl = GL.getInstance(FinancialApplication.mApp);
        convert = gl.getConvert();
        packer = gl.getPacker();
        db = gl.getDb();
    }

    private static IGL getGl() {
        return getInstance().gl;
    }

    public static String getVersion() {
        return getGl().getVersion();
    }

    /**
     * algorithm
     * @return
     */
    public static IAlgo getAlgo() {
        return getGl().getAlgo();
    }

    public static ICommHelper getCommHelper() {
        return getGl().getCommHelper();
    }

    public static ICompress getCompress() {
        return getGl().getCompress();
    }

    public static IConvert getConvert() {
        return getInstance().convert;
    }

    public static IDb getDb() {
        return getInstance().db;
    }

    public static IImgProcessing getImgProcessing() {
        return getGl().getImgProcessing();
    }

    public static ILbs getLbs() {
        return getGl().getLbs();
    }

    public static IPacker getPacker() {
        return getInstance().packer;
    }

    public static IUtils getUtils() {
        return getGl().getUtils();
    }

    public static byte[] strToBcdPaddingLeft(String str) {
        return getInstance().convert.strToBcd(str, IConvert.EPaddingPosition.PADDING_LEFT);
    }

    public static byte[] strToBcdPaddingRight(String str) {
        return getInstance().convert.strToBcd(str, IConvert.EPaddingPosition.PADDING_RIGHT);
    }

    public static String bcdToStr(byte[] bytes) {
        return getInstance().convert.bcdToStr(bytes);
    }
}
