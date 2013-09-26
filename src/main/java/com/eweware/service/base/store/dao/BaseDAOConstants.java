package com.eweware.service.base.store.dao;

/**
 * <p>Field names and value data types for all entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 8/31/12 Time: 4:23 PM
 */
public interface BaseDAOConstants {

    /**
     * <p>The MongoDB-generated UUID for this object.</p>
     */
    static final String ID = "_id";

    /**
     * <p>Used as a soft delete marker. The semantics of the
     * deletion will vary from dao type to dao type (e.g.,
     * for media daos, associated images may also need to
     * be deleted from s3 buckets)</p>
     */
    static final String IS_DELETED = "d";

    /**
     * <p>Datetime this object was created in UTC.</p>
     * @see com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_insert()
     */
    static final String CREATED = "c";  // responsibility of _insert to create this

    /**
     * <p>Datetime this object was last updated in UTC.</p>
     * @see com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_updateByPrimaryId(com.eweware.service.base.store.dao.type.DAOUpdateType)
     * @see com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_updateByCompoundId(com.eweware.service.base.store.dao.type.DAOUpdateType, String...)
     */
    static final String UPDATED = "u";  // responsibility of _insert and _update to create this

    // TODO Add its own schema
}
