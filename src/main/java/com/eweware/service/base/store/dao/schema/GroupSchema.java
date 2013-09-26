package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.GroupDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:10 PM
 */
public class GroupSchema extends BaseSchema implements GroupDAOConstants {

    protected GroupSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final GroupSchema getSchema(LocaleId localeId) {

        GroupSchema schema = (GroupSchema) GroupSchema.getCachedSchema(GroupSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new GroupSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(GroupSchema.class, localeId, schema);

        return schema;
    }
}
