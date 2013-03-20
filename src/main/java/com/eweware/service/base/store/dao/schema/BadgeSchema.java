package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BadgeDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/19/13 Time: 3:29 PM
 */
public class BadgeSchema extends BaseSchema implements SchemaConstants, BadgeDAOConstants {

    protected BadgeSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final BadgeSchema getSchema(LocaleId localeId) {

        BadgeSchema schema = (BadgeSchema) BadgeSchema.getCachedSchema(BadgeSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new BadgeSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(BadgeSchema.class, localeId, schema);

        return schema;
    }
}
