package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserCommentInfoDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:26 PM
 */
public class UserCommentInfoSchema extends BaseSchema implements UserCommentInfoDAOConstants {
    
    protected UserCommentInfoSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final UserCommentInfoSchema getSchema(LocaleId localeId) {

        UserCommentInfoSchema schema = (UserCommentInfoSchema) UserCommentInfoSchema.getCachedSchema(UserCommentInfoSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new UserCommentInfoSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(UserCommentInfoSchema.class, localeId, schema);

        return schema;
    }

}
