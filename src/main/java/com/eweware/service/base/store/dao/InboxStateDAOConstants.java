package com.eweware.service.base.store.dao;

/**
 * <p>_id for this blah is a String composite of:
 *  "i:" + groupId + "-" + inboxNumber
 *  e.g., "i:506cd1480364e675a6571308-1 is inbox #1 for group id 506cd1480364e675a6571308
 *  The "i:" prefix is used as a "namespace" selector</p>
 * <p><b> TODO: needs schema</b></p>
 * @author rk@post.harvard.edu
 *         Date: 10/6/12 Time: 11:10 AM
 *
 */
public interface InboxStateDAOConstants {

    /**
     * A list of _ids of inbox item ids in the inbox collection.
     * These are the active items for this inbox. The stats
     * application maintains this and the inboxHandler prepends to it as
     * new blahs are created
     */
    static final String INBOX_ITEM_IDS = "I";

    /**
     * This is the high watermark of inbox numbers for this group.
     * It is redundantly stored in each record so that this information
     * can be accessed with the INBOX_ITEM_IDS without the need
     * of an additional query.
     *
     * The low is assumed to be inbox number 0.
     */
    static final String INBOX_NUMBER_TOP =  "H";
}
