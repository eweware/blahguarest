package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:44 PM
 */
public interface GroupTypeDAOConstants {

    static final String DISPLAY_NAME = "displayName";
    static final String GROUP_COUNT = "gcount";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
          new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{DISPLAY_NAME}),
          new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{GROUP_COUNT})
    };
}
