package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Entity carries basic information about a user.</p>
 * <p>Fields and corresponding data types for this entity.</p>
 * @author rk@post.harvard.edu
 *         Date: 8/31/12 Time: 4:21 PM
 */
public interface UserDAOConstants {

    /**
     * <p>The user's username. This is a unique name and it is part of the login authentication
     * requirements (along with a password). The username can be changed by the user. A string.</p>
     */
    static final String USERNAME = "N";

    /**
     * <p>An optional array of statistics that may be requested by the client.</p>
     * <p><b>TODO: separate this into a separate API as it is too much. Also, make it more column-oriented for forward
     * compatibility with HBase, etc.</b></p>
     */
    static final String STATS = "L";

    /**
     * <p>The user's overall strength. A float between 0 and 1, inclusive.</p>
     */
    static final String USER_STRENGTH = "S";

    /**
     * <p>The user's controvery strength. Measures how controversial the user is. A float between 0 and 1, inclusive.</p>
     */
    static final String USER_CONTROVERSY_STRENGTH = "K";

    /**
     * <p>List of badge ids for this user.</p>
     */
    static final String BADGE_IDS = "B";

    /**
     * <p>List of image ids for this user</p>
     */
    static final String IMAGE_IDS = "M";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{USERNAME}),
            new SchemaDataTypeFieldMap(SchemaDataType.R, new String[]{USER_STRENGTH, USER_CONTROVERSY_STRENGTH}),
            new SchemaDataTypeFieldMap(SchemaDataType.E, new String[]{STATS}),
            new SchemaDataTypeFieldMap(SchemaDataType.ILS, new String[]{BADGE_IDS, IMAGE_IDS}),
    };
}
