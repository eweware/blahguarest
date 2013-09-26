package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.InboxBlahDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:18 PM
 */
public class InboxBlahSchema extends BaseSchema implements InboxBlahDAOConstants {

    protected InboxBlahSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final InboxBlahSchema getSchema(LocaleId localeId) {

        InboxBlahSchema schema = (InboxBlahSchema) InboxBlahSchema.getCachedSchema(InboxBlahSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new InboxBlahSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(InboxBlahSchema.class, localeId, schema);

        return schema;
    }

}
