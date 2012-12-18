package main.java.com.eweware.service.base.store.dao.schema;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.GroupTypeDAOConstants;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 1:16 PM
 */
public class GroupTypeSchema extends BaseSchema implements GroupTypeDAOConstants {

    protected GroupTypeSchema(LocaleId localeId) {
        super(localeId);
    }

    public static final GroupTypeSchema getSchema(LocaleId localeId) {

        GroupTypeSchema schema = (GroupTypeSchema) GroupTypeSchema.getCachedSchema(GroupTypeSchema.class, localeId);
        if (schema != null) {
            return schema;
        }

        schema = new GroupTypeSchema(localeId);

        createSimpleFieldSpecs(schema, SIMPLE_FIELD_TYPES);

        cacheSchema(GroupTypeSchema.class, localeId, schema);

        return schema;
    }

}
