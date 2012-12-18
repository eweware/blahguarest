package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/31/12 Time: 4:23 PM
 */
public interface BaseDAOConstants {

    static final String ID = "_id";  // responsibility of _insert to create this via mongo
    static final String IS_DELETED = "d";  // TODO decide whether we'll really use soft deletes: this is used now by the indexing to signal deletes
    static final String CREATED = "created";  // responsibility of _insert to create this
    static final String UPDATED = "updated";  // responsibility of _insert to create this
}
