package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.store.dao.MediaDAOConstants;


/**
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:46 PM
 */
public class MediaSchema extends main.java.com.eweware.service.base.store.dao.schema.BaseSchema
        implements main.java.com.eweware.service.base.store.dao.schema.SchemaConstants, MediaDAOConstants  {

    protected MediaSchema(main.java.com.eweware.service.base.i18n.LocaleId localeId) {
        super(localeId);
    }


    public static final MediaSchema getSchema(main.java.com.eweware.service.base.i18n.LocaleId localeId) {

        MediaSchema schema = (MediaSchema) MediaSchema.getCachedSchema(MediaSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new MediaSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(MediaSchema.class, localeId, schema);

        return schema;
    }

}
