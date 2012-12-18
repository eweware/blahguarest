package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 11:13 AM
 */
public class RealDataTypeValidator implements FieldValidator {
    @Override
    public Object toValidValue(Object value, SchemaSpec spec) {
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        if (value == null) {
            return true; // a null value is a command to delete the field or set the field's value to null
        }
        final Number maximumValue = spec.getMaximumValue();
        if (value instanceof Double) {
            double val = (Double) value;
            if (maximumValue == null || val > maximumValue.doubleValue()) {
                final Number minimumValue = spec.getMinimumValue();
                return (minimumValue == null) || val >= minimumValue.doubleValue();
            }
        } else if (value instanceof Float) {
            double val = (Float) value;
            if (maximumValue == null || val <= maximumValue.floatValue()) {
                final Number minimumValue = spec.getMinimumValue();
                return (minimumValue == null || val >= minimumValue.floatValue());
            }
        }
        return false;
    }
}
