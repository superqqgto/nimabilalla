/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2016 - ? Pax Corporation. All rights reserved.
 * Module Date: 2016-12-20
 * Module Auth: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.db;

import android.content.Context;
import android.text.TextUtils;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.pax.pay.base.AcqIssuerRelation;
import com.pax.pay.base.Acquirer;
import com.pax.pay.base.CardRange;
import com.pax.pay.base.Issuer;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AcqDb {

    private PreparedQuery<Issuer> issuersForAcquirerQuery = null;
    private PreparedQuery<CardRange> cardRangeQuery = null;

    private RuntimeExceptionDao<Acquirer, Integer> acquirerDao = null;
    private RuntimeExceptionDao<Issuer, Integer> issuerDao = null;
    private RuntimeExceptionDao<CardRange, Integer> cardRangeDao = null;
    private RuntimeExceptionDao<AcqIssuerRelation, Integer> relationDao = null;

    private AcqDb(Context context) {
        dbHelper = BaseDbHelper.getInstance(context);
    }

    private final BaseDbHelper dbHelper;

    private static AcqDb instance;

    /**
     * get the Singleton of the DB Helper
     *
     * @param context the context object
     * @return the Singleton of DB helper
     */
    public static synchronized AcqDb getInstance(Context context) {
        if (instance == null) {
            instance = new AcqDb(context);
        }

        return instance;
    }

    /***************************************
     * Dao
     ******************************************/
    private RuntimeExceptionDao<Acquirer, Integer> getAcquirerDao() {
        if (acquirerDao == null) {
            acquirerDao = dbHelper.getRuntimeExceptionDao(Acquirer.class);
        }
        return acquirerDao;
    }


    private RuntimeExceptionDao<Issuer, Integer> getIssuerDao() {
        if (issuerDao == null) {
            issuerDao = dbHelper.getRuntimeExceptionDao(Issuer.class);
        }
        return issuerDao;
    }

    private RuntimeExceptionDao<CardRange, Integer> getCardRangeDao() {
        if (cardRangeDao == null) {
            cardRangeDao = dbHelper.getRuntimeExceptionDao(CardRange.class);
        }
        return cardRangeDao;
    }

    private RuntimeExceptionDao<AcqIssuerRelation, Integer> getRelationDao() {
        if (relationDao == null) {
            relationDao = dbHelper.getRuntimeExceptionDao(AcqIssuerRelation.class);
        }
        return relationDao;
    }


    /***************************************Acquirer******************************************/
    /**
     * insert an acquirer record
     *
     * @param acquirer the record
     */
    public boolean insertAcquirer(final Acquirer acquirer) {
        try {
            RuntimeExceptionDao<Acquirer, Integer> dao = getAcquirerDao();
            dao.create(acquirer); // ignore the return value from create
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find the unique acquirer record by name
     *
     * @param acquirerName acquirer name
     * @return the matched {@link Acquirer} or null
     */
    public Acquirer findAcquirer(final String acquirerName) {
        RuntimeExceptionDao<Acquirer, Integer> dao = getAcquirerDao();
        List<Acquirer> acq = dao.queryForEq(Acquirer.NAME_FIELD_NAME, acquirerName);
        if (acq != null && acq.size() > 0)
            return acq.get(0);
        return null;
    }

    /**
     * find records of all Acquirers
     *
     * @return List of {@link Acquirer}
     */
    public List<Acquirer> findAllAcquirers() {
        RuntimeExceptionDao<Acquirer, Integer> dao = getAcquirerDao();
        return dao.queryForAll();
    }

    /**
     * update the acquirer
     *
     * @param acquirer the target acquirer
     */
    public boolean updateAcquirer(final Acquirer acquirer) {
        try {
            RuntimeExceptionDao<Acquirer, Integer> dao = getAcquirerDao();
            dao.update(acquirer);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * delete the acquirer by id
     *
     * @param id acquire id
     */
    public boolean deleteAcquirer(int id) {
        try {
            RuntimeExceptionDao<Acquirer, Integer> dao = getAcquirerDao();
            dao.deleteById(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /***************************************Issuer******************************************/
    /**
     * insert an issuer record
     *
     * @param issuer the record
     */
    public boolean insertIssuer(final Issuer issuer) {
        try {
            RuntimeExceptionDao<Issuer, Integer> dao = getIssuerDao();
            dao.create(issuer); // ignore the return value from create
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * find the unique issuer record by name
     *
     * @param issuerName issuer name
     * @return the matched {@link Issuer} or null
     */
    public Issuer findIssuer(String issuerName) {
        RuntimeExceptionDao<Issuer, Integer> dao = getIssuerDao();
        List<Issuer> issuers = dao.queryForEq(Issuer.NAME_FIELD_NAME, issuerName);
        if (issuers != null && issuers.size() > 0)
            return issuers.get(0);
        return null;
    }

    /**
     * find records of all Issuers
     *
     * @return List of {@link Issuer}
     */
    public List<Issuer> findAllIssuers() {
        RuntimeExceptionDao<Issuer, Integer> dao = getIssuerDao();
        return dao.queryForAll();
    }

    /**
     * bind an acquirer with an issuer
     *
     * @param root   the acquirer
     * @param issuer the issuer
     */
    public boolean bind(final Acquirer root, final Issuer issuer) {
        try {
            RuntimeExceptionDao<AcqIssuerRelation, Integer> dao = getRelationDao();
            AcqIssuerRelation relation = findRelation(root, issuer);
            if (null == relation) {
                dao.create(new AcqIssuerRelation(root, issuer)); //ignore the return value from create
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * update the issuer
     *
     * @param issuer the target issuer
     */
    public boolean updateIssuer(final Issuer issuer) {
        try {
            RuntimeExceptionDao<Issuer, Integer> dao = getIssuerDao();
            dao.update(issuer);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * delete the issuer by id
     *
     * @param id issuer id
     */
    public boolean deleteIssuer(int id) {
        try {
            RuntimeExceptionDao<Issuer, Integer> dao = getIssuerDao();
            dao.deleteById(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /***************************************CardRange******************************************/
    /**
     * insert a cardRange record
     *
     * @param cardRange the card range
     */
    public boolean insertCardRange(final CardRange cardRange) {
        try {
            RuntimeExceptionDao<CardRange, Integer> dao = getCardRangeDao();
            dao.create(cardRange); //ignore the return value from create
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * update record
     *
     * @param cardRange the record need to be updated
     */
    public boolean updateCardRange(final CardRange cardRange) {
        try {
            RuntimeExceptionDao<CardRange, Integer> dao = getCardRangeDao();
            dao.update(cardRange); //ignore the return value
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * find the unique issuer record
     *
     * @param low  the lower limit
     * @param high the higher limit
     * @return the matched {@link CardRange} or null
     */
    public CardRange findCardRange(final long low, final long high) {
        RuntimeExceptionDao<CardRange, Integer> dao = getCardRangeDao();
        Map<String, Object> fields = new LinkedHashMap<String, Object>() {{
            put(CardRange.RANGE_LOW_FIELD_NAME, low);
            put(CardRange.RANGE_HIGH_FIELD_NAME, high);
        }};

        List<CardRange> crs = dao.queryForFieldValues(fields);
        if (crs != null && crs.size() > 0)
            return crs.get(0);
        return null;
    }

    /**
     * find the unique CardRange record
     *
     * @param pan the card no
     * @return the matched {@link CardRange} or null
     */
    public CardRange findCardRange(final String pan) {
        if (TextUtils.isEmpty(pan)) {
            return null;
        }
        try {
            RuntimeExceptionDao<CardRange, Integer> dao = getCardRangeDao();
            if (cardRangeQuery == null) {
                cardRangeQuery = makePostsForCardRangeQuery();
            }
            String subPan = pan.substring(0, 10);
            cardRangeQuery.setArgumentHolderValue(0, subPan);
            cardRangeQuery.setArgumentHolderValue(1, subPan);
            cardRangeQuery.setArgumentHolderValue(2, pan.length());
            return dao.queryForFirst(cardRangeQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * find the card ranges of the issuer
     *
     * @param issuer the issuer
     * @return List of {@link CardRange}
     */
    public List<CardRange> findCardRange(final Issuer issuer) {
        RuntimeExceptionDao<CardRange, Integer> dao = getCardRangeDao();
        return dao.queryForEq(Issuer.ID_FIELD_NAME, issuer);
    }

    /**
     * find records of all Card Ranges
     *
     * @return List of {@link CardRange}
     */
    public List<CardRange> findAllCardRanges() {
        RuntimeExceptionDao<CardRange, Integer> dao = getCardRangeDao();
        return dao.queryForAll();
    }


    /**
     * delete the cardRange by id
     *
     * @param id cardRange id
     */
    public boolean deleteCardRange(int id) {
        try {
            RuntimeExceptionDao<CardRange, Integer> dao = getCardRangeDao();
            dao.deleteById(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /****************Relation*********************/

    /**
     * find the relation of acquirer and issuer
     *
     * @param acquirer the acquirer
     * @param issuer   the issuer
     * @return the matched {@link AcqIssuerRelation} or null
     */
    private AcqIssuerRelation findRelation(final Acquirer acquirer, final Issuer issuer) {
        RuntimeExceptionDao<AcqIssuerRelation, Integer> dao = getRelationDao();
        Map<String, Object> fieldsMap = new HashMap<String, Object>() {{
            put(Acquirer.ID_FIELD_NAME, acquirer);
            put(Issuer.ID_FIELD_NAME, issuer);
        }};
        List<AcqIssuerRelation> relation = dao.queryForFieldValues(fieldsMap);
        if (relation != null && relation.size() > 0)
            return relation.get(0);
        return null;
    }

    /**
     * find all issuers accepted by the acquirer
     *
     * @param acquirer the acquirer
     * @return List of {@link Issuer}
     * @throws SQLException
     */
    public List<Issuer> lookupIssuersForAcquirer(final Acquirer acquirer) throws SQLException {
        RuntimeExceptionDao<Issuer, Integer> dao = getIssuerDao();
        if (issuersForAcquirerQuery == null) {
            issuersForAcquirerQuery = makePostsForAcquirerQuery();
        }
        issuersForAcquirerQuery.setArgumentHolderValue(0, acquirer);
        return dao.query(issuersForAcquirerQuery);
    }

    /**
     * generate the sql for finding the acquirer
     */
    private PreparedQuery<Issuer> makePostsForAcquirerQuery() throws SQLException {
        RuntimeExceptionDao<AcqIssuerRelation, Integer> relationDao = getRelationDao();
        RuntimeExceptionDao<Issuer, Integer> issuerDao = getIssuerDao();
        //create a query for find the relation
        QueryBuilder<AcqIssuerRelation, Integer> relation = relationDao.queryBuilder();
        // sql: select issuer_id from acq_issuer_relation
        relation.selectColumns(Issuer.ID_FIELD_NAME);
        // sql: where acquirer_id=?
        relation.where().eq(Acquirer.ID_FIELD_NAME, new SelectArg());
        // create a foreign query
        QueryBuilder<Issuer, Integer> postQb = issuerDao.queryBuilder();
        // sql: where issuer_id in()
        postQb.where().in(Issuer.ID_FIELD_NAME, relation);
        /**
         * the sql is
         * "SELECT * FROM `issuer`
         * 		WHERE `issuer_id` IN (
         * 			SELECT `issuer_id` FROM `acq_issuer_relation` WHERE `acquirer_id` = ?
         * 		) "
         */
        return postQb.prepare();
    }

    /**
     * generate the sql for finding the card range
     *
     * @throws SQLException
     */
    private PreparedQuery<CardRange> makePostsForCardRangeQuery() throws SQLException {
        RuntimeExceptionDao<CardRange, Integer> cardRangeDao = getCardRangeDao();
        QueryBuilder<CardRange, Integer> postQb = cardRangeDao.queryBuilder();
        Where where = postQb.where();
        //WHERE (low <= ? AND high >= ?) @1
        where.le(CardRange.RANGE_LOW_FIELD_NAME, new SelectArg()).and().ge(CardRange.RANGE_HIGH_FIELD_NAME, new SelectArg());
        //WHERE (length = 0 OR ? = length) @2
        where.eq(CardRange.LENGTH_FIELD_NAME, 0).or().eq(CardRange.LENGTH_FIELD_NAME, new SelectArg());
        //WHERE @1 AND @2
        where.and(2);
        // order by (high - low)
        postQb.orderByRaw(CardRange.RANGE_HIGH_FIELD_NAME + "-" + CardRange.RANGE_LOW_FIELD_NAME);
        return postQb.prepare();
    }

    /**
     * update the acqIssuerRelation
     *
     * @param acqIssuerRelation the relation object
     */
    public boolean updateRelation(final AcqIssuerRelation acqIssuerRelation) {
        try {
            RuntimeExceptionDao<AcqIssuerRelation, Integer> dao = getRelationDao();
            dao.update(acqIssuerRelation);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * delete the acqIssuerRelation by id
     *
     * @param id acqIssuerRelation id
     */
    public boolean deleteAcqIssuerRelation(int id) {
        try {
            RuntimeExceptionDao<AcqIssuerRelation, Integer> dao = getRelationDao();
            dao.deleteById(id);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
