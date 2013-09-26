package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.UserBlahInfoDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:22 PM
 */
public class UserBlahInfoSchema extends BaseSchema implements UserBlahInfoDAOConstants {

    protected UserBlahInfoSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final UserBlahInfoSchema getSchema(LocaleId localeId) {

        UserBlahInfoSchema schema = (UserBlahInfoSchema) UserBlahInfoSchema.getCachedSchema(UserBlahInfoSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new UserBlahInfoSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(UserBlahInfoSchema.class, localeId, schema);

        return schema;
    }
}
