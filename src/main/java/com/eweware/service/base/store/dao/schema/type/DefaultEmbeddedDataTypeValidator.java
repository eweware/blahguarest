package main.java.com.eweware.service.base.store.dao.schema.type;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import main.java.com.eweware.service.base.store.impl.mongo.dao.PollOptionTextImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>Default validator for embedded data types.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 9/18/12 Time: 11:19 AM
 *
 */
public class DefaultEmbeddedDataTypeValidator implements FieldValidator {

    @Override
    public Object toValidValue(Object value, SchemaSpec spec) throws SystemErrorException {
        return value;
    }

    @Override
    public boolean isValid(Object value, SchemaSpec spec) {
        return true;
    }
}