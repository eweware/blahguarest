package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>This entity maintains the relationship between a user and a group.</p>
 * <p>Fields and corresponding data types for this entity.</p>
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:45 PM
 */
public interface UserGroupDAOConstants {

    /**
     * <p>The group's id. A string.</p>
     */
    static final String GROUP_ID = "G";

    /**
     * <p>The group's display name. A string</p>
     * <p>This is "denormalized" as it repeats the display name in the group entity.</p>
     */
    static final String GROUP_DISPLAY_NAME = "N";

    /**
     * <p>The user's id. A string.</p>
     */
    static final String USER_ID = "U";

    /**
     * <p>The authorization state of the user in relation to the group (e.g., is active in it, suspended).</p>
     *
     * @see com.eweware.service.base.payload.AuthorizedState
     */
    static final String STATE = "S";

    /**
     * <p>The number of the first active inbox of the group, an Integer.</p>
     * <p>Each inbox is a collection whose name is the concatenation
     * of the group id and the inbox number.</p>
     */
    static final String FIRST_INBOX_NUMBER = "F";

    /**
     * <p>The number of the last active inbox of the group, an Integer.</p>
     * <p>Each inbox is a collection whose name is the concatenation
     * of the group id and the inbox number.</p>
     */
    static final String LAST_INBOX_NUMBER = "L";



//    /**
//     * <p><b>Do not use.</b></p>
//     * <p>This was used during alpha but will be mostly replaced with badges. But it may make a comeback for some cases.</p>
//     */
//    static final String VALIDATION_CODE = "C";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{GROUP_ID, GROUP_DISPLAY_NAME, USER_ID, STATE}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{FIRST_INBOX_NUMBER, LAST_INBOX_NUMBER})
    };
}
