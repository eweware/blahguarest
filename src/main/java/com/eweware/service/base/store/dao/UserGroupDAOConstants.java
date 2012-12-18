package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:45 PM
 */
public interface UserGroupDAOConstants {

    static final String GROUP_ID = "groupId";
    static final String GROUP_DISPLAY_NAME = "gdn";
    static final String USER_ID = "userId";
    static final String STATE = "state";
    static final String VALIDATION_CODE = "code";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{GROUP_ID, GROUP_DISPLAY_NAME, USER_ID, STATE, VALIDATION_CODE})
    };
}
