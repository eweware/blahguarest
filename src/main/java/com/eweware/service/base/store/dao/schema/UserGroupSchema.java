package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserGroupDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:31 PM
 */
public class UserGroupSchema extends BaseSchema implements UserGroupDAOConstants {
    
    protected UserGroupSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final UserGroupSchema getSchema(LocaleId localeId) {

        UserGroupSchema schema = (UserGroupSchema) UserGroupSchema.getCachedSchema(UserGroupSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new UserGroupSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(UserGroupSchema.class, localeId, schema);

        return schema;
    }

}
