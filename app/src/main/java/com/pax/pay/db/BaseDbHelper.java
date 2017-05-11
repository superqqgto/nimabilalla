/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-17
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.pax.pay.base.AcqIssuerRelation;
import com.pax.pay.base.Acquirer;
import com.pax.pay.base.CardRange;
import com.pax.pay.base.Issuer;
import com.pax.pay.emv.CardBin;
import com.pax.pay.emv.CardBinBlack;
import com.pax.pay.emv.EmvAid;
import com.pax.pay.emv.EmvCapk;
import com.pax.pay.base.DccTransData;
import com.pax.pay.trans.model.MotoTransData;
import com.pax.pay.trans.model.MotoTabBatchTransData;
import com.pax.pay.trans.model.TabBatchTransData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransTotal;

import java.sql.SQLException;

class BaseDbHelper extends OrmLiteSqliteOpenHelper {
    // DB Name
    private static final String DATABASE_NAME = "data.db";
    // DB version
    private static final int DATABASE_VERSION = 3;

    private BaseDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, AcqIssuerRelation.class);
            TableUtils.createTable(connectionSource, Acquirer.class);
            TableUtils.createTable(connectionSource, Issuer.class);
            TableUtils.createTable(connectionSource, CardRange.class);
            TableUtils.createTable(connectionSource, DccTransData.class);

            TableUtils.createTable(connectionSource, CardBin.class);
            TableUtils.createTable(connectionSource, CardBinBlack.class);

            TableUtils.createTable(connectionSource, EmvAid.class);
            TableUtils.createTable(connectionSource, EmvCapk.class);

            TableUtils.createTable(connectionSource, TransData.class);
            TableUtils.createTable(connectionSource, TransTotal.class);
            TableUtils.createTable(connectionSource, TabBatchTransData.class);

            TableUtils.createTable(connectionSource, MotoTransData.class);
            TableUtils.createTable(connectionSource, MotoTabBatchTransData.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO
     * this method will be called when you updated the app or the version of db,
     * which means you have to change the data here to the new version.
     * here it's an example by deleting the old version of db
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVersion,
                          int newVersion) {
        try {
            //delete old version of db
            TableUtils.dropTable(connectionSource, Acquirer.class, true);
            TableUtils.dropTable(connectionSource, Issuer.class, true);
            TableUtils.dropTable(connectionSource, CardRange.class, true);
            TableUtils.dropTable(connectionSource, AcqIssuerRelation.class, true);
            TableUtils.dropTable(connectionSource, DccTransData.class, true);

            TableUtils.dropTable(connectionSource, CardBin.class, true);
            TableUtils.dropTable(connectionSource, CardBinBlack.class, true);

            TableUtils.createTable(connectionSource, EmvAid.class);
            TableUtils.createTable(connectionSource, EmvCapk.class);

            TableUtils.dropTable(connectionSource, TransData.class, true);
            TableUtils.dropTable(connectionSource, TransTotal.class, true);
            TableUtils.dropTable(connectionSource, TabBatchTransData.class, true);
            TableUtils.dropTable(connectionSource, MotoTransData.class, true);
            TableUtils.dropTable(connectionSource, MotoTabBatchTransData.class, true);

            //create a new db
            onCreate(sqliteDatabase, connectionSource);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static BaseDbHelper instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized BaseDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BaseDbHelper(context);
        }

        return instance;
    }
}
