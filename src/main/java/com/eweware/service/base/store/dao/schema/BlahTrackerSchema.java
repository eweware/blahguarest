package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.BlahTrackerDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/22/13 Time: 6:21 PM
 */
public class BlahTrackerSchema extends BaseSchema implements BlahTrackerDAOConstants {

    protected BlahTrackerSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final BlahTrackerSchema getSchema(LocaleId localeId) {
        BlahTrackerSchema schema = (BlahTrackerSchema) BlahTrackerSchema.getCachedSchema(BlahTrackerSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new BlahTrackerSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(TrackerSchema.class, localeId, schema);

        return schema;
    }
}
