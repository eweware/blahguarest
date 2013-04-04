package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Field names and value data types for blah type entities.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 9/1/12 Time: 4:42 PM
 */
public interface BlahTypeDAOConstants {

    /**
     * <p>The displayable name of the blah</p>
     * <p><b>TODO i18n</b></p>
     */
    static final String NAME = "N";

    /**
     * <p>The blah type's category. It's value is a category id, an integer.</p>
     *
     * @see main.java.com.eweware.service.base.store.dao.type.BlahTypeCategoryType
     */
    static final String CATEGORY_ID = "C";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{NAME}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{CATEGORY_ID})
    };

}
