package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;

import java.util.regex.Pattern;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 11:13 AM
 */
public class StringDataTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) {
        return (value == null) ? null : String.valueOf(value);
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        if (value == null) {
            return true; // a null value is a command to delete the field or set the field's value to null
        }
        if (value instanceof String) {
            final String val = (String) value;
            final Number minimumValue = spec.getMinimumValue();
            if (minimumValue == null || val.length() >= minimumValue.intValue()) {
                final Number maximumValue = spec.getMaximumValue();
                if (maximumValue == null || val.length() <= maximumValue.intValue()) {
                    final String regexp = spec.getValidationRegexp();
                    return (regexp == null) || Pattern.compile(regexp).matcher(val).matches(); // TODO cache Pattern
                }
            }
        }
        return false;
    }
}
