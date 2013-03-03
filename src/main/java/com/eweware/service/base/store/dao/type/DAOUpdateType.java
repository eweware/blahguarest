package main.java.com.eweware.service.base.store.dao.type;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/20/12 Time: 8:53 PM
 */
public enum DAOUpdateType {

    INCREMENTAL_DAO_UPDATE, // numbers are incremented when positive or decremented when negative; other fields are simply set
    ABSOLUTE_UPDATE,    // all fields are set: if a field is set to null, it is deleted.
}
