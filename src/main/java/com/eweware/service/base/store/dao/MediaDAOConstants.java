package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
/**
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:26 PM
 */
public interface MediaDAOConstants {

    /* All media assumed for now to be in blahguaimages bucket */

    static final String TYPE = "t"; // {i|v} image, video

    static final String BUCKET = "b"; // the bucket name

    static final String PATHNAME = "p"; // the pathname withint the bucket

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    TYPE, BUCKET, PATHNAME
            })
    };
}
