package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.BlahDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 10:22 AM
 */
public class BlahSchema extends BaseSchema implements SchemaConstants, BlahDAOConstants {

    protected BlahSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final BlahSchema getSchema(LocaleId localeId) {

        BlahSchema schema = (BlahSchema) BlahSchema.getCachedSchema(BlahSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new BlahSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(BlahSchema.class, localeId, schema);

        return schema;
    }
}
