package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.CommentDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 12:52 PM
 */
public class CommentSchema extends BaseSchema implements CommentDAOConstants {

    protected CommentSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final CommentSchema getSchema(LocaleId localeId) {

        CommentSchema schema = (CommentSchema) CommentSchema.getCachedSchema(CommentSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new CommentSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(CommentSchema.class, localeId, schema);

        return schema;
    }
}
