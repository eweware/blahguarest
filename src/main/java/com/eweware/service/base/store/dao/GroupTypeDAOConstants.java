package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 *   <p>Field names and value data types for group type entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:44 PM
 */
public interface GroupTypeDAOConstants {

    /**
     * <p>The display name for the group type. A string. <b>TODO: i18n</b></p>
     */
    static final String DISPLAY_NAME = "N";

    /**
     * <p>The number of groups of this type. An integer</p>
     */
    static final String GROUP_COUNT = "C";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
          new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{DISPLAY_NAME}),
          new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{GROUP_COUNT})
    };
}
