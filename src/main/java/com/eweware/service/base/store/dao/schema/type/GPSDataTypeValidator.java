package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 11:14 AM
 */
public class GPSDataTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) {
        return value; // TODO implement gps converter
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        if (value == null) {
            return true; // a null value is a command to delete the field or set the field's value to null
        } else return true; // TODO implement gps validator
    }
}
