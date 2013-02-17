package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:42 PM
 */
public interface BlahTypeDAOConstants {

    static final String NAME = "name";
    static final String CATEGORY_ID = "c";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{NAME}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{CATEGORY_ID})
    };

}
