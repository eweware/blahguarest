package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.BadgeAuthorityDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/19/13 Time: 3:27 PM
 */
public class BadgeAuthoritySchema extends BaseSchema implements SchemaConstants, BadgeAuthorityDAOConstants {

    protected BadgeAuthoritySchema(LocaleId localeId) {
        super(localeId);
    }

    public static final BadgeAuthoritySchema getSchema(LocaleId localeId) {

        BadgeAuthoritySchema schema = (BadgeAuthoritySchema) BadgeAuthoritySchema.getCachedSchema(BadgeAuthoritySchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new BadgeAuthoritySchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(BadgeAuthoritySchema.class, localeId, schema);

        return schema;
    }
}
