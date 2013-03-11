package main.java.com.eweware.service.base.store.dao;

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
     * <p>Used as a soft delete. Currently used only be
     * search to delete a record from the index.
     * This is currently only used for dynamic index deletes.</p>
     */
    static final String IS_DELETED = "d";

    /**
     * <p>Datetime this object was created in UTC.</p>
     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_insert()
     */
    static final String CREATED = "created";  // responsibility of _insert to create this

    /**
     * <p>Datetime this object was last updated in UTC.</p>
     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_updateByPrimaryId(main.java.com.eweware.service.base.store.dao.type.DAOUpdateType)
     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_updateByCompoundId(main.java.com.eweware.service.base.store.dao.type.DAOUpdateType, String...)
     */
    static final String UPDATED = "updated";  // responsibility of _insert and _update to create this

    // TODO Add its own schema
}
