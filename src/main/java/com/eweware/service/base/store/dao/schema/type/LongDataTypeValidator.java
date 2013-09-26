package com.eweware.service.base.store.dao.schema.type;

import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.store.dao.schema.SchemaSpec;

/**
 * @author rk@post.harvard.edu
 *         Date: 5/24/13 Time: 1:03 PM
 */
public class LongDataTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) throws SystemErrorException {
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        if (value == null) {
            return true;
        }
        final Number maximumValue = spec.getMaximumValue();
        if (value instanceof Long) {
            long val = (Long) value;
            if (maximumValue == null || val <= maximumValue.longValue()) {
                final Number minimumValue = spec.getMinimumValue();
                return minimumValue == null || val >= minimumValue.longValue();
            }
        } else if (value instanceof Integer) {
            int val = (Integer) value;
            if (maximumValue == null || val <= maximumValue.intValue()) {
                final Number minimumValue = spec.getMinimumValue();
                return minimumValue == null || val >= minimumValue.intValue();
            }
        }
        return false;
    }
}
