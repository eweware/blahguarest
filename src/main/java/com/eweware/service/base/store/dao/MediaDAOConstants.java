package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
/**
 * <p>This is a metadata entity for media (e.g., audio, images, video).</p>
 * <p>Field names and value data types for media entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:26 PM
 */
public interface MediaDAOConstants {

    /* All media assumed for now to be in blahguaimages bucket */

    /**
     * <p>The media's type. A string. </p>
     * <p>T</p>
     */
    static final String TYPE = "t"; // {i|v} image, video

    /**
     * <p>The S3 bucket where the media object is stored. A string.</p>
     */
    static final String BUCKET = "b"; // the bucket name

    /**
     * <p>A pathname, within the S3 bucket, where the media is stored. A string.</p>
     */
    static final String PATHNAME = "p";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    TYPE, BUCKET, PATHNAME
            })
    };
}
