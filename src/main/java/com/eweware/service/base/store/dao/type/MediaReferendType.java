package com.eweware.service.base.store.dao.type;

/**
 * <p>The media referend type specifies whether the image is used for objects such as blahs,
 * comments, users, or when it has not yet been associated
 * with any type of object.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 5/20/13 Time: 4:55 PM
 */
public enum MediaReferendType {

    B, /* Used for blahs */
    C, /* Used for comments */
    U, /* Used for users */
    X, /* When the image is not currently associated with any object */

}
