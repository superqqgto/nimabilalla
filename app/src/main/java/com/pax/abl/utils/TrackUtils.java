/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-11-25
 * Module Author: Steven.W
 * Description:
 *
 * ============================================================================
 */
package com.pax.abl.utils;

public class TrackUtils {

    /**
     * 从磁道2数据中获取主帐号
     *
     * @param track
     * @return
     * @date 2015年5月22日下午3:28:14
     * @example
     */
    public static String getPan(String track) {
        if (track == null)
            return null;

        int len = track.indexOf('=');
        if (len < 0) {
            len = track.indexOf('D');
            if (len < 0)
                return null;
        }

        if ((len < 13) || (len > 19))
            return null;
        return track.substring(0, len);
    }

    public static String getServiceCode(String track2) {
        int idx = track2.indexOf("=");
        if (idx == -1) {
            return null;
        } else {
            return track2.substring(idx + 5, idx + 8);
        }
    }

    /**
     * 判定是否为IC卡
     *
     * @param track
     * @return
     */
    public static boolean isIcCard(String track) {
        if (track == null)
            return false;

        int index = track.indexOf('=');
        if (index < 0) {
            index = track.indexOf('D');
            if (index < 0)
                return false;
        }

        if (index + 6 > track.length())
            return false;

        return "2".equals(track.substring(index + 5, index + 6)) || "6".equals(track.substring(index + 5, index + 6));
    }

    /**
     * 获取有效期
     *
     * @param track
     * @return
     */
    public static String getExpDate(String track) {
        if (track == null)
            return null;

        int index = track.indexOf('=');
        if (index < 0) {
            index = track.indexOf('D');
            if (index < 0)
                return null;
        }

        if (index + 5 > track.length())
            return null;
        return track.substring(index + 1, index + 5);
    }

    /**
     * 获取持卡人姓名
     *
     * @param track
     * @return
     */
    public static String getHolderName(String track) {
        if (track == null) {
            return null;
        }

        int index1 = track.indexOf('^');
        if (index1 < 0) {
            return null;
        }

        int index2 = track.lastIndexOf('^');
        if (index2 < 0) {
            return null;
        }

        return track.substring(index1 + 1, index2);
    }

    /**
     * 获取磁条卡MC的TAG
     *
     * @param tag
     * @return
     */
    public static byte[] getMcTag(short tag){
        byte[] shortBuf;
        if (tag > 255){
            shortBuf = new byte[2];
            for(int i=0;i<2;i++) {
                int offset = (shortBuf.length - 1 -i)*8;
                shortBuf[i] = (byte)((tag>>>offset)&0xff);
            }
        }else{
            shortBuf = new byte[1];
            shortBuf[0] = (byte)(tag&0xff);
        }

        return shortBuf;
    }
}
