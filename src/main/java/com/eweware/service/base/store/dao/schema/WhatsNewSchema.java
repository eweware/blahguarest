package com.eweware.service.base.store.dao.schema;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.WhatsNewDAOConstants;

/**
 * Created by Dave on 1/25/14.
 */
public class WhatsNewSchema extends BaseSchema implements SchemaConstants, WhatsNewDAOConstants {

    protected WhatsNewSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final WhatsNewSchema getSchema(LocaleId localeId) {

        WhatsNewSchema schema = (WhatsNewSchema) WhatsNewSchema.getCachedSchema(WhatsNewSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new WhatsNewSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(WhatsNewSchema.class, localeId, schema);

        return schema;
    }
}
