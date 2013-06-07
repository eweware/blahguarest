package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import main.java.com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;
/**
 * <p>This is a metadata entity for media (e.g., audio, images, video).</p>
 * <p>Field names and value data types for media entities.</p>
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:26 PM
 */
public interface MediaDAOConstants {

    /* All media assumed for now to be in blahguaimages bucket */

    /**
     * <p>The media's type. A string representation of a MediaReferendType </p>
     * @see main.java.com.eweware.service.base.store.dao.type.MediaReferendType
     */
    static final String REFEREND_TYPE = "T";

    /**
     * <p>The media's type (e.g., image, video, etc.)</p>
     * @see main.java.com.eweware.service.base.store.dao.type.MediaType
     */
    static final String TYPE = "M";

    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{
                    REFEREND_TYPE, TYPE,
            })
    };
}
