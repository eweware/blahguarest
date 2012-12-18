package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:43 PM
 */
public interface GroupDAOConstants {

    static final String DISPLAY_NAME = "displayName";
    static final String DESCRIPTION = "d";
    static final String GROUP_TYPE_ID = "groupTypeId";
    static final String USER_COUNT = "ucount";
    static final String BLAH_COUNT = "bcount";
    static final String USER_VALIDATION_METHOD = "vmeth";
    static final String USER_VALIDATION_PARAMETERS = "vp";
    static final String STATE = "state";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
        new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                DISPLAY_NAME, DESCRIPTION, GROUP_TYPE_ID, USER_VALIDATION_METHOD, USER_VALIDATION_PARAMETERS, STATE
        }),
        new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{USER_COUNT, BLAH_COUNT}),
    };
}
