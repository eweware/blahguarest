package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Currently not in use. Should replace the large by-month tracker when time allows.</p>
 * <p>Reason to replace it is that: (1) it ties us to MongoDB bigtime, and (2) even
 * for Mongo, we don't want to detal with large objects that can potentially
 * consume RAM workspace: ideally, we would have smaller consecutively
 * placed chunks in disc... since we might not be able to control or predict how Mongo lays it out,
 * the benefits of having smaller chunks in mongo are unclear without testing.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 9/22/12 Time: 2:55 PM
 */
public interface TrackerDAOConstants {

    /**
     * Date/time tracker was created.
     */
    static final String CREATED = "c";

    /** Operation values: one of the TrackerOperation enum as a String */
    static final String OPERATION = "P";

    /** Was object voted on? -1 or 1 or null for down vote, up vote, or no vote, respectively.
     * If the operation is a comment creation, this is a vote for the blah.
     * If the operation is a comment update, this is a vote for the comment. **/
    static final String PROMOTION = "v";

    /** Number of views of object: an integer or null for no views */
    static final String VIEWS = "w";

    /** Number of opens of object: an integer or null for no opens */
    static final String OPENS = "o";

    /**
     * If the user has voted on a poll, this is the index of the poll
     * option on which the user voted.
     */
    static final String VOTED_POLL_INDEX = "i";

    /** A state: "A", "P", or "D" for active, pending, or removed/deleted, respectively */
    static final String STATE = "s";

    /** A group type id: string */
    static final String GROUP_TYPE_ID = "g";

    /** A group id: string */
    static final String GROUP_ID = "G";

    /** A user id: string.
     * If this a blah or comment creation operation, this is the user id of its author.
     * */
    static final String USER_ID = "U";

    /** A blah id: string */
    static final String BLAH_ID = "B";

    /** A comment id: string */
    static final String COMMENT_ID = "C";

    /** The user's gender */
    static final String USER_GENDER = "ug";

    /** The user's race */
    static final String USER_RACE = "ur";

    /** The user's income range */
    static final String USER_INCOME_RANGE = "ui";

    /** The user's date of birth */
    static final String USER_DATE_OF_BIRTH = "ub";



    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{OPERATION, GROUP_TYPE_ID, GROUP_ID, USER_ID, BLAH_ID, COMMENT_ID, STATE,
                    USER_GENDER, USER_RACE,  USER_INCOME_RANGE, USER_DATE_OF_BIRTH}),
            new SchemaDataTypeFieldMap(SchemaDataType.L, new String[]{PROMOTION, VIEWS, OPENS}),
    };
}
