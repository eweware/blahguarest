package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * <p>Constants for the badging authority dao field names.</p>
 * @author rk@post.harvard.edu
 *         Date: 3/8/13 Time: 3:49 PM
 */
public interface BadgeAuthorityDAOConstants {

    /**
     * <p> The name of the authority.</p>
     */
    static final String DISPLAY_NAME = "N";

    /**
     * <p> An English description of the authority.</p>
     */
    static final String DESCRIPTION = "D";

    /**
     * <p>The endpoint url for the authority. The format of the string is hostname:port.</p>
     */
    static final String ENDPOINT_URL = "E";


    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    DISPLAY_NAME, DESCRIPTION, ENDPOINT_URL}),
    };
}
