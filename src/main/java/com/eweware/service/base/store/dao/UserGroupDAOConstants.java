package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

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
    static final String GROUP_ID = "groupId";

    /**
     * <p>The group's display name. A string</p>
     * <p>This is "denormalized" as it repeats the display name in the group entity.</p>
     */
    static final String GROUP_DISPLAY_NAME = "gdn";

    /**
     * <p>The user's id. A string.</p>
     */
    static final String USER_ID = "userId";

    /**
     * <p>The authorization state of the user in relation to the group (e.g., is active in it, suspended).</p>
     *
     * @see main.java.com.eweware.service.base.payload.AuthorizedState
     */
    static final String STATE = "state";

    /**
     * <p><b>Do not use.</b></p>
     * <p>This was used during alpha but will be mostly replaced with badges. But it may make a comeback for some cases.</p>
     */
    static final String VALIDATION_CODE = "code";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{GROUP_ID, GROUP_DISPLAY_NAME, USER_ID, STATE, VALIDATION_CODE})
    };
}
