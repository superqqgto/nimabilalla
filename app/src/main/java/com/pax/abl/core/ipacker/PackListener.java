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
package com.pax.abl.core.ipacker;

/**
 * 打包模块监听器
 *
 * @author Steven.W
 */
public interface PackListener {
    /**
     * 计算mac
     *
     * @param data
     * @return mac值
     */
    byte[] onCalcMac(byte[] data);

    /**
     * 磁道加密
     *
     * @param track
     * @return
     */
    byte[] onEncTrack(byte[] track);
}
