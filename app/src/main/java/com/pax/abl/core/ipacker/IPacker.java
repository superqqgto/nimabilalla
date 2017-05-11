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
 * 打包模块抽象接口
 *
 * @param <T>
 * @param <O>
 * @author Steven.W
 */
public interface IPacker<T, O> {
    /**
     * 打包接口
     *
     * @param t
     * @return
     */
    O pack(T t);

    /**
     * 解包接口
     *
     * @param t
     * @param o
     * @return
     */
    int unpack(T t, O o);
}
