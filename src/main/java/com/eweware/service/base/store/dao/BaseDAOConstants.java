package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/31/12 Time: 4:23 PM
 */
public interface BaseDAOConstants {

    /**
     * The MongoDB-generated unique id for this object
     */
    static final String ID = "_id";

    /**
     * Used as a soft delete. Currently used only be
     * search to delete a record from the index.
     * TODO do we really need this?
     */
    static final String IS_DELETED = "d";

    /**
     * Datetime this object was created.
     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_insert()
     */
    static final String CREATED = "created";  // responsibility of _insert to create this

    /**
     * Datetime this object was last updated
     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_updateByPrimaryId(DAOUpdateType)
     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#_updateByCompoundId(DAOUpdateType, String...)
     */
    static final String UPDATED = "updated";  // responsibility of _insert and _update to create this
}
