package com.eweware.service.base.store.dao.schema;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.ChannelImportDAOConstants;

/**
 * Created by ultradad on 3/6/15.
 */
public class ChannelImportSchema extends BaseSchema implements SchemaConstants, ChannelImportDAOConstants {

    protected ChannelImportSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final ChannelImportSchema getSchema(LocaleId localeId) {

        ChannelImportSchema schema = (ChannelImportSchema) ChannelImportSchema.getCachedSchema(ChannelImportSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new ChannelImportSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(ChannelImportSchema.class, localeId, schema);

        return schema;
    }
}