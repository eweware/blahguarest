package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.UserDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:27 PM
 */
public class UserSchema extends BaseSchema implements UserDAOConstants {
    
    protected UserSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final UserSchema getSchema(LocaleId localeId) {

        UserSchema schema = (UserSchema) UserSchema.getCachedSchema(UserSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new UserSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(UserSchema.class, localeId, schema);

        return schema;
    }

}
