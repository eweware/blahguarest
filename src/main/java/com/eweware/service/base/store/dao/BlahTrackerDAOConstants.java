package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * Used internally by the tracker.
 *
 * @author rk@post.harvard.edu
 *         Date: 8/16/12 Time: 4:48 PM
 */
public interface BlahTrackerDAOConstants {

    public static final String BT_OBJECT_ID = "I"; // blah id TODO used by unit tests to delete related trackers
    public static final String BT_AUTHOR_ID = "A";   // blah author id

//    public static final String BT_USER_ID = "S";

    public static final String BT_COMMENTS = "C";
    public static final String BT_UP_VOTES = "U";
    public static final String BT_DOWN_VOTES = "D";
    public static final String BT_POLL_OPTION_INDEX = "P";
    public static final String BT_VIEWS = "V";
    public static final String BT_OPENS = "O";

    public static final String[] BT_FIELD_NAMES = new String[]{
            BT_COMMENTS,
            BT_UP_VOTES,
            BT_DOWN_VOTES,
            BT_POLL_OPTION_INDEX,
            BT_VIEWS,
            BT_OPENS,
    };

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{BT_OBJECT_ID, BT_AUTHOR_ID, BT_COMMENTS, BT_UP_VOTES, BT_DOWN_VOTES, BT_POLL_OPTION_INDEX, BT_VIEWS, BT_OPENS}),
    };
}
