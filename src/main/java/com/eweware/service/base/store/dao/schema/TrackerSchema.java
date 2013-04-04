package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/25/12 Time: 1:48 PM
 */
public class TrackerSchema extends BaseSchema implements main.java.com.eweware.service.base.store.dao.TrackerDAOConstants {

    protected TrackerSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final TrackerSchema getSchema(LocaleId localeId) {
        TrackerSchema schema = (TrackerSchema) TrackerSchema.getCachedSchema(UserSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new TrackerSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(TrackerSchema.class, localeId, schema);

        return schema;
    }
}
