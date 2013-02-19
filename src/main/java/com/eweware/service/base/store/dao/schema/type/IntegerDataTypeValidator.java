package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 11:12 AM
 */
public class IntegerDataTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) {
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        if (value == null) {
            return true;
        }
        final Number maximumValue = spec.getMaximumValue();
        if (value instanceof Integer) {
            int val = (Integer) value;
            if (maximumValue == null || val <= maximumValue.intValue()) {
                final Number minimumValue = spec.getMinimumValue();
                return minimumValue == null || val >= minimumValue.intValue();
            }
        } else if (value instanceof Long) {
            long val = (Long) value;
            if (maximumValue == null || val <= maximumValue.longValue()) {
                final Number minimumValue = spec.getMinimumValue();
                return minimumValue == null || val >= minimumValue.longValue();
            }
        }
        return false;
    }
}
