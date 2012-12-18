package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BlahTypeDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 12:50 PM
 */
public class BlahTypeSchema extends BaseSchema implements BlahTypeDAOConstants {

    protected BlahTypeSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final BlahTypeSchema getSchema(LocaleId localeId) {

        BlahTypeSchema schema = (BlahTypeSchema) BlahTypeSchema.getCachedSchema(BlahTypeSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new BlahTypeSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(BlahTypeSchema.class, localeId, schema);

        return schema;
    }
}
