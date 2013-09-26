package com.eweware.service.base.store.dao.schema.type;

import com.eweware.service.base.store.dao.schema.SchemaSpec;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/16/12 Time: 11:12 AM
 */
public class BooleanDataTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) {
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        return (value == null) || (value instanceof Boolean); // Either null (means remove), or Boolean is valid
    }
}
