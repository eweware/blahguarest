package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 11:00 AM
 */
public class IndexedListValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) {
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        if (value instanceof String) { // value is a key
            final Map<String, Object> data = spec.getValidationMap();
            return data.containsKey(value);
        }
        if (value instanceof List<?>) {
            List<?> v = (List<?>) value;
            return (v.isEmpty() || v.get(0) instanceof String);
        }
        // null not allowed: at worst, the default index value should be set
        return false;
    }
}
