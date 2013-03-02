package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>A tracker object for internal use.</p>
 * <p>This object will eventually be queued for offline, but real-time, processing.</p>
 * @author rk@post.harvard.edu
 *         Date: 9/22/12 Time: 2:55 PM
 */
public interface TrackerDAOConstants {

    /** Operation values: one of the TrackerOperation enum as a String */
    static final String OPERATION = "o";

    /** Was object voted on? -1 or 1 or null for down vote, up vote, or no vote, respectively.
     * If the operation is a comment creation, this is a vote for the blah.
     * If the operation is a comment update, this is a vote for the comment. **/
    static final String VOTE = "v";

    /** Number of views of object: an integer or null for no views */
    static final String VIEWS = "w";

    /** Number of opens of object: an integer or null for no opens */
    static final String OPENS = "o";

    /** A state: "A", "P", or "D" for active, pending, or removed/deleted, respectively */
    static final String STATE = "s";

    /** A group type id: string */
    static final String GROUP_TYPE_ID = "g";

    /** A group id: string */
    static final String GROUP_ID = "G";

    /** A user id: string */
    static final String USER_ID = "U";

    /** An author id: string [this is the author of a blah] */
    static final String BLAH_AUTHOR_ID = "A";

    /** An author id: string [this is the author of a comment] */
    static final String COMMENT_AUTHOR_ID = "A";

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
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{OPERATION, GROUP_TYPE_ID, GROUP_ID, USER_ID, BLAH_AUTHOR_ID, BLAH_ID, COMMENT_ID, STATE,
                    USER_GENDER, USER_RACE,  USER_INCOME_RANGE, USER_DATE_OF_BIRTH}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{VOTE, VIEWS, OPENS}),
    };
}
